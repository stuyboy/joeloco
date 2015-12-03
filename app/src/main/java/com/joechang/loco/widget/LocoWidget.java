package com.joechang.loco.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;
import com.joechang.loco.R;
import com.joechang.loco.service.SendLocationService;
import com.joechang.loco.contacts.ContactsUtils;
import com.joechang.loco.utils.BitmapUtils;

/**
 * Created by joechang on 5/18/15.
 */
public class LocoWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            //See if we're a lock screen or home screen widget
            Bundle myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);

            // Get the value of OPTION_APPWIDGET_HOST_CATEGORY
            int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);

            // If the value is WIDGET_CATEGORY_KEYGUARD, it's a lockscreen widget
            boolean isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

            // Get the shared preferences for this widget instance, including the target name, number, etc.
            SharedPreferences sp = context.getSharedPreferences(getClass().getName() + appWidgetId, Context.MODE_PRIVATE);
            Long cid = sp.getLong(ContactsUtils.CONTACT_ID, 0);
            String type = sp.getString(SendLocationService.DESTINATION_TYPE, null);
            String addr = sp.getString(SendLocationService.DESTINATION_ADDRESS, null);

            if (type != null && addr != null) {
                this.setupRemoteViews(context, appWidgetId, cid, addr, ContactsUtils.Type.valueOf(type));
            }
        }
    }

    public void setupRemoteViews(Context cxt, int widgetId, Long contactId, String address, ContactsUtils.Type sendType) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(cxt);

        // Create an Intent to launch ExampleActivity
        Intent launchIntent = new Intent(cxt, SendLocationService.class);
        launchIntent.putExtra(SendLocationService.DESTINATION_TYPE, sendType.toString());
        launchIntent.putExtra(SendLocationService.DESTINATION_ADDRESS, address);
        PendingIntent pendingIntent = PendingIntent.getService(cxt, widgetId, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(cxt.getPackageName(), R.layout.widget_loco);
        views.setOnClickPendingIntent(R.id.textNowButton, pendingIntent);
        views.setTextViewText(R.id.textNowDescription, address);

        // If picture, crop to circle.
        Bitmap bb = ContactsUtils.retrieveContactThumbnail(cxt, contactId);
        Bitmap bbC = BitmapUtils.cropToCircle(bb);
        views.setBitmap(R.id.contactImageView, "setImageBitmap", bbC);

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(widgetId, views);
    }

}
