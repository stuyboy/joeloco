package com.joechang.loco.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.joechang.loco.R;

/**
 * Author:  joechang
 * Date:    2/9/15
 * Purpose: A generic dialog box for when we need to enter in some value, such as the name of a new group.
 */
public class NewEntryDialogFragment extends DialogFragment {

    private NewEntryDialogListener mListener;

    public interface NewEntryDialogListener {
        public void onDialogSave(DialogFragment df);
        public void onDialogCancel(DialogFragment df);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (NewEntryDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(getCustomView(savedInstanceState, R.layout.fragment_new_entry_dialog))
                .setMessage(R.string.dialog_enter_new)
                .setTitle(R.string.action_new)
                .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogSave(NewEntryDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogCancel(NewEntryDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private View getCustomView(Bundle savedInstanceState, int id) {
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View addView=inflater.inflate(id, null);
        return addView;
    }
}
