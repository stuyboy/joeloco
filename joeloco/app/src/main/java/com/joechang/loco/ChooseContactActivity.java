package com.joechang.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.joechang.loco.contacts.ContactsUtils;
import com.joechang.loco.service.SendLocationService;

import java.util.Map;

/**
 * Created by joechang on 5/22/15.
 * Launches the contact chooser, combines access points, asks user to choose.
 * Subclass used for the widget configuration!
 */
public class ChooseContactActivity extends Activity {
    public final static int CHOOSE_CONTACT = 213213;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContactsUtils.showContactPicker(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (ContactsUtils.ACTION_CONFIGURE_WIDGET_CONTACT == requestCode) {
                selectContactPreference(data);
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (ChooseContactActivity.CHOOSE_CONTACT == requestCode) {
                SendLocationService.launchIntent(this, data);
            }
        }
    }

    protected void selectContactPreference(Intent data) {
        final Map<String, ContactsUtils.Type> combined = ContactsUtils.retrieveAllContactPoints(this, data);

        //If there is NO contact information, then message and reselect.
        if (combined.size() == 0) {
            Toast.makeText(this, getString(R.string.contact_no_details), Toast.LENGTH_SHORT).show();
            ContactsUtils.showContactPicker(this);
            return;
        }

        final Long contactId = ContactsUtils.retrieveContactId(data);
        final String[] addresses = combined.keySet().toArray(new String[combined.size()]);

        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_contact_detail_title))
                .setItems(addresses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = addresses[which];
                        ContactsUtils.Type sendType = combined.get(key);

                        onContactAddressPicked(contactId, sendType, key);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create();
        ad.show();
    }

    //What to do once contact picked?!
    protected void onContactAddressPicked(Long contactId, ContactsUtils.Type sendType, String address) {
        //In this instance, startActivityForResult results!
        Intent result = new Intent();
        result.putExtra(SendLocationService.DESTINATION_ADDRESS, address);
        result.putExtra(SendLocationService.DESTINATION_TYPE, sendType.toString());
        result.putExtra(ContactsUtils.CONTACT_ID, contactId);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
