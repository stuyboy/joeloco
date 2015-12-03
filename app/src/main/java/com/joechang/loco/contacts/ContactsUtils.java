package com.joechang.loco.contacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import com.joechang.loco.R;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author:  joechang
 * Date:    2/9/15
 * Purpose: A quick class to hold functionality when we need to query the addess book.
 */
public class ContactsUtils {
    public static final String CONTACT_ID = "_CONTACT_ID";
    //Different actions identifiers for Contacts.
    public static final int ADD_CONTACT_TO_GROUP = 1002;
    public static final int ADD_CONTACT_TO_SHARENOW_TXT = 2001;
    public final static int ACTION_CONFIGURE_WIDGET_CONTACT = 2201;

    /**
     * Typically the first call within a fragment or activity to launch the Contacts application.
     * Then we call one of the "retrieveX" methods below within the onActivityResult.
     *
     * @return
     */
    public static Intent intentForPicker() {
        return new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    }

    public static void showContactPicker(Activity activity) {
        Intent contactPickerIntent = ContactsUtils.intentForPicker();
        activity.startActivityForResult(contactPickerIntent, ContactsUtils.ACTION_CONFIGURE_WIDGET_CONTACT);
    }

    /**
     * Typically used within an Activity or Fragment in the onActivityResult, after contacts are accessed,
     * and we want to pull out the email address of the chosen entry.
     *
     * @param cxt
     * @param data
     * @return
     */
    public static String[] retrieveEmails(Context cxt, Intent data) {

        Cursor c = cxt.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[]{retrieveContactId(data).toString()},
                null);

        List<String> ret = new ArrayList<String>();

        int nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int emailIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);

        if (c.getCount() > 0) {
            c.moveToFirst();
            do {
                String name = c.getString(nameIdx);
                String email = c.getString(emailIdx);
                ret.add(email);
            } while (c.moveToNext());
            c.close();
        }

        return ret.toArray(new String[ret.size()]);
    }

    public static Long retrieveContactId(Intent data) {
        return Long.parseLong(data.getData().getLastPathSegment());
    }

    public static String[] retrievePhoneNumbers(Context cxt, Intent data) {
        Cursor c = cxt.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                new String[]{retrieveContactId(data).toString()},
                null);

        List<String> phoneNumbers = new ArrayList<String>();

        int nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int numberIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);

        if (c.getCount() > 0) {
            c.moveToFirst();
            do {
                String name = c.getString(nameIdx);
                String number = c.getString(numberIdx);
                phoneNumbers.add(number);
            } while (c.moveToNext());
            c.close();
        }

        return phoneNumbers.toArray(new String[phoneNumbers.size()]);
    }

    public static Map<String, Type> retrieveAllContactPoints(Context cxt, Intent data) {
        String[] phones = ContactsUtils.retrievePhoneNumbers(cxt, data);
        String[] emails = ContactsUtils.retrieveEmails(cxt, data);

        Map<String, Type> returnMap = new LinkedHashMap<String, Type>();

        for (String p : phones) {
            returnMap.put(p, Type.TEXT);
        }

        for (String e : emails) {
            returnMap.put(e, Type.EMAIL);
        }

        return returnMap;
    }

    public static Bitmap retrieveContactThumbnail(Context cxt, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = cxt.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    Bitmap r = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                    return r;
                }
            }
        } finally {
            cursor.close();
        }
        return BitmapFactory.decodeResource(cxt.getResources(), R.drawable.joeloco_logo);
    }

    public static Long contactIdByNumber(Context cxt, String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Long contactId = null;

        ContentResolver contentResolver = cxt.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri,
                new String[]{
                        BaseColumns._ID,
                        ContactsContract.PhoneLookup.DISPLAY_NAME
                }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                //name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                contactId = contactLookup.getLong(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return contactId;
    }


    public enum Type {
        TEXT,
        EMAIL,
        INAPP;

        public String key() {
            return "_" + this.toString() + "_DESTINATION";
        }
    }

}
