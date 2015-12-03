/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.transaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Looper;
import android.provider.Telephony;
import com.klinker.android.logger.Log;
import com.android.mms.util.DownloadManager;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduPersister;
import com.klinker.android.send_message.R;

public class RetryScheduler implements Observer {
    private static final String TAG = "RetryScheduler";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private final Context mContext;
    private final ContentResolver mContentResolver;

    private RetryScheduler(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    private static RetryScheduler sInstance;
    public static RetryScheduler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RetryScheduler(context);
        }
        return sInstance;
    }

    private boolean isConnected() {
        ConnectivityManager mConnMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        return (ni == null ? false : ni.isConnected());
    }

    public void update(Observable observable) {
        try {
            Transaction t = (Transaction) observable;

                Log.v(TAG, "[RetryScheduler] update " + observable);

            // We are only supposed to handle M-Notification.ind, M-Send.req
            // and M-ReadRec.ind.
            if ((t instanceof NotificationTransaction)
                    || (t instanceof RetrieveTransaction)
                    || (t instanceof ReadRecTransaction)
                    || (t instanceof SendTransaction)) {
                try {
                    TransactionState state = t.getState();
                    if (state.getState() == TransactionState.FAILED) {
                        Uri uri = state.getContentUri();
                        if (uri != null) {
                            scheduleRetry(uri);
                        }
                    }
                } finally {
                    t.detach(this);
                }
            }
        } finally {
            if (isConnected()) {
                setRetryAlarm(mContext);
            }
        }
    }

    private void scheduleRetry(Uri uri) {
        long msgId = ContentUris.parseId(uri);

        Uri.Builder uriBuilder = Telephony.MmsSms.PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));

        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                uriBuilder.build(), null, null, null, null);

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    int msgType = cursor.getInt(cursor.getColumnIndexOrThrow(
                            "msg_type"));

                    int retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow(
                            "retry_index")) + 1; // Count this time.

                    // TODO Should exactly understand what was happened.
                    // TODO don't use the sdk > 19 apis here
                    int errorType = 1;

                    DefaultRetryScheme scheme = new DefaultRetryScheme(mContext, retryIndex);

                    ContentValues values = new ContentValues(4);
                    long current = System.currentTimeMillis();
                    boolean isRetryDownloading =
                            (msgType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND);
                    boolean retry = true;
                    int respStatus = getResponseStatus(msgId);
                    int errorString = 0;
                    if (!isRetryDownloading) {
                        // Send Transaction case
                        switch (respStatus) {
                            case PduHeaders.RESPONSE_STATUS_ERROR_SENDING_ADDRESS_UNRESOLVED:
                                errorString = R.string.invalid_destination;
                                break;
                            case PduHeaders.RESPONSE_STATUS_ERROR_SERVICE_DENIED:
                            case PduHeaders.RESPONSE_STATUS_ERROR_PERMANENT_SERVICE_DENIED:
                                errorString = R.string.service_not_activated;
                                break;
                            case PduHeaders.RESPONSE_STATUS_ERROR_NETWORK_PROBLEM:
                                errorString = R.string.service_network_problem;
                                break;
                            case PduHeaders.RESPONSE_STATUS_ERROR_TRANSIENT_MESSAGE_NOT_FOUND:
                            case PduHeaders.RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND:
                                errorString = R.string.service_message_not_found;
                                break;
                        }
                        if (errorString != 0) {
                            DownloadManager.getInstance().showErrorCodeToast(errorString);
                            retry = false;
                        }
                    } else {
                        // apply R880 IOT issue (Conformance 11.6 Retrieve Invalid Message)
                        // Notification Transaction case
                        respStatus = getRetrieveStatus(msgId);
                        if (respStatus ==
                                PduHeaders.RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND) {
                            DownloadManager.getInstance().showErrorCodeToast(
                                    R.string.service_message_not_found);
                            SqliteWrapper.delete(mContext, mContext.getContentResolver(), uri,
                                    null, null);
                            retry = false;
                            return;
                        }
                    }
                    if ((retryIndex < scheme.getRetryLimit()) && retry) {
                        long retryAt = current + scheme.getWaitingInterval();

                            Log.v(TAG, "scheduleRetry: retry for " + uri + " is scheduled at "
                                    + (retryAt - System.currentTimeMillis()) + "ms from now");

                        values.put("due_time", retryAt);

                        if (isRetryDownloading) {
                            // Downloading process is transiently failed.
                            try { Looper.prepare(); } catch (Exception e) { }
                            DownloadManager.init(mContext);
                            DownloadManager.getInstance().markState(
                                    uri, DownloadManager.STATE_TRANSIENT_FAILURE);
                        }
                    } else {
                        // TODO don't use the sdk > 19 apis here
                        errorType = 10;
                        if (isRetryDownloading) {
                            Cursor c = SqliteWrapper.query(mContext, mContext.getContentResolver(), uri,
                                    new String[] { "thread_id" }, null, null, null);

                            long threadId = -1;
                            if (c != null) {
                                try {
                                    if (c.moveToFirst()) {
                                        threadId = c.getLong(0);
                                    }
                                } finally {
                                    c.close();
                                }
                            }

                            if (threadId != -1) {
                                // Downloading process is permanently failed.
                                markMmsFailed(mContext);
                            }

                            DownloadManager.getInstance().markState(
                                    uri, DownloadManager.STATE_PERMANENT_FAILURE);
                        } else {
                            // Mark the failed message as unread.
                            ContentValues readValues = new ContentValues(1);
                            readValues.put("read", 0);
                            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                    uri, readValues, null, null);
                            markMmsFailed(mContext);
                        }
                    }

                    values.put("err_type",  errorType);
                    values.put("retry_index", retryIndex);
                    values.put("last_try",    current);

                    int columnIndex = cursor.getColumnIndexOrThrow(
                            "_id");
                    long id = cursor.getLong(columnIndex);
                    SqliteWrapper.update(mContext, mContentResolver,
                            Uri.withAppendedPath(
                                    Uri.parse("content://mms-sms/"), "pending"),
                            values, "_id" + "=" + id, null);
                } else if (LOCAL_LOGV) {
                    Log.v(TAG, "Cannot found correct pending status for: " + msgId);
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void markMmsFailed(final Context context) {
        Cursor query = context.getContentResolver().query(Uri.parse("content://mms"), new String[]{"_id"}, null, null, "date desc");
        query.moveToFirst();
        String id = query.getString(query.getColumnIndex("_id"));
        query.close();

        // mark message as failed
        ContentValues values = new ContentValues();
        values.put("msg_box", 5);
        String where = "_id" + " = '" + id + "'";
        context.getContentResolver().update(Uri.parse("content://mms"), values, where, null);

        context.sendBroadcast(new Intent(com.klinker.android.send_message.Transaction.REFRESH));
        context.sendBroadcast(new Intent(com.klinker.android.send_message.Transaction.NOTIFY_SMS_FAILURE));

        // broadcast that mms has failed and you can notify user from there if you would like
        context.sendBroadcast(new Intent(com.klinker.android.send_message.Transaction.MMS_ERROR));
    }

    private int getResponseStatus(long msgID) {
        int respStatus = 0;
        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                Uri.parse("content://mms/outbox"), null, "_id" + "=" + msgID, null, null);
        try {
            if (cursor.moveToFirst()) {
                respStatus = cursor.getInt(cursor.getColumnIndexOrThrow("resp_st"));
            }
        } finally {
            cursor.close();
        }
        if (respStatus != 0) {
            Log.e(TAG, "Response status is: " + respStatus);
        }
        return respStatus;
    }

    // apply R880 IOT issue (Conformance 11.6 Retrieve Invalid Message)
    private int getRetrieveStatus(long msgID) {
        int retrieveStatus = 0;
        Cursor cursor = SqliteWrapper.query(mContext, mContentResolver,
                Uri.parse("content://mms/inbox"), null, "_id" + "=" + msgID, null, null);
        try {
            if (cursor.moveToFirst()) {
                retrieveStatus = cursor.getInt(cursor.getColumnIndexOrThrow(
                        "resp_st"));
            }
        } finally {
            cursor.close();
        }
        if (retrieveStatus != 0) {
                Log.v(TAG, "Retrieve status is: " + retrieveStatus);
        }
        return retrieveStatus;
    }

    public static void setRetryAlarm(Context context) {
        Cursor cursor = PduPersister.getPduPersister(context).getPendingMessages(
                Long.MAX_VALUE);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // The result of getPendingMessages() is order by due time.
                    long retryAt = cursor.getLong(cursor.getColumnIndexOrThrow(
                            "due_time"));

                    Intent service = new Intent(TransactionService.ACTION_ONALARM,
                                        null, context, TransactionService.class);
                    PendingIntent operation = PendingIntent.getService(
                            context, 0, service, PendingIntent.FLAG_ONE_SHOT);
                    AlarmManager am = (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE);
                    am.set(AlarmManager.RTC, retryAt, operation);

                        Log.v(TAG, "Next retry is scheduled at"
                                + (retryAt - System.currentTimeMillis()) + "ms from now");
                }
            } finally {
                cursor.close();
            }
        }
    }
}
