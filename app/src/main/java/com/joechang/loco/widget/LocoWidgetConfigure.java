package com.joechang.loco.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.joechang.loco.ChooseContactActivity;
import com.joechang.loco.service.SendLocationService;
import com.joechang.loco.contacts.ContactsUtils;

/**
 * Created by joechang on 5/18/15.
 */
public class LocoWidgetConfigure extends ChooseContactActivity {

    private int mAppWidgetId;
    private String mProviderClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        AppWidgetProviderInfo api = AppWidgetManager.getInstance(this).getAppWidgetInfo(mAppWidgetId);
        mProviderClassName = api.provider.getClassName();

        //Called last to launch the contact picker.
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onContactAddressPicked(Long contactId, ContactsUtils.Type sendType, String address) {
        savePreferences(LocoWidgetConfigure.this, contactId, address, sendType, mAppWidgetId);
        setupRemoteViews(contactId, address, sendType);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);

        finish();
    }

    protected void savePreferences(Context cxt, Long id, String address, ContactsUtils.Type sendType, int widgetId) {
        SharedPreferences sp = cxt.getSharedPreferences(mProviderClassName + widgetId, MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString(SendLocationService.DESTINATION_TYPE, sendType.toString());
        e.putString(SendLocationService.DESTINATION_ADDRESS, address);
        e.putLong(ContactsUtils.CONTACT_ID, id);
        e.apply();
    }

    protected void setupRemoteViews(Long contactId, String number, ContactsUtils.Type sendType) {
        new LocoWidget().setupRemoteViews(this, mAppWidgetId, contactId, number, sendType);
    }
}
