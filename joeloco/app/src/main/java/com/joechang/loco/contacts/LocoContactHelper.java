package com.joechang.loco.contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

/**
 * Author:    joechang
 * Created:   5/29/15 9:42 AM
 * Purpose:   Primary class for determining if the Loco Contact is within the address book.  The Loco contact holds
 * the joelo.co email address as well as the SMS number, for quick integration into messages and texts.
 */
public enum LocoContactHelper {
    INSTANCE;

    private volatile Activity mActivity;

    public static final String LOCO_NUMBER = "9135626563";

    public static LocoContactHelper newInstance(Activity cxt) {
        INSTANCE.mActivity = cxt;
        return INSTANCE;
    }

    public boolean hasLocoContact() {
        return (ContactsUtils.contactIdByNumber(mActivity, LOCO_NUMBER) != null);
    }

    public boolean insertLocoContact() {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, "me@joelo.co")
                .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .putExtra(ContactsContract.Intents.Insert.PHONE, LOCO_NUMBER)
                .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

        mActivity.startActivity(intent);
        return true;
    }

    /**
     * Show the user a dismissable screen that describes what we will be doing, inserting a contact into their addressbook.
     */
    public void demonstrateLocoContact(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("To help you use joelo.co, a contact can be created with the joelo.co email address and SMS number")
                .setTitle("Create joelo.co contact?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void doContactCheck() {
        if (!hasLocoContact()) {
            demonstrateLocoContact(mActivity);
        }
    }
}
