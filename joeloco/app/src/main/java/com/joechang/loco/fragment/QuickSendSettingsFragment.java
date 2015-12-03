package com.joechang.loco.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.joechang.loco.R;
import com.joechang.loco.utils.UserInfoStore;

/**
 * Created by joechang on 5/22/15.
 * Fragment encapsulating the settings that go into the quick send widget, or command.
 * ie, Manipulate the message and duration, and possibly other things.
 * Can use in the configuration of widgets, as well as the default settings tab.
 */
public class QuickSendSettingsFragment extends Fragment implements View.OnClickListener {

    private UserInfoStore uis;

    public static QuickSendSettingsFragment newInstance() {
        QuickSendSettingsFragment qs = new QuickSendSettingsFragment();
        return qs;
    }

    public QuickSendSettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        uis = UserInfoStore.getInstance(getActivity());

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_quicksendsettings, container, false);

        EditText etMessage = (EditText)v.findViewById(R.id.quickSendMessage);
        EditText etDuration = (EditText)v.findViewById(R.id.quickSendDuration);
        CheckBox enableChat = (CheckBox)v.findViewById(R.id.enableChat);

        // Populate existing values into the fields
        if (savedInstanceState == null) {
            etMessage.setText(uis.getQuickSendMessage());
            etDuration.setText(Integer.toString(uis.getQuickSendDuration()));
            enableChat.setChecked(uis.isChatEnabled());
        } else {
            etMessage.setText(savedInstanceState.getString(UserInfoStore.QUICKSEND_MESSAGE));
            etDuration.setText(Integer.toString(savedInstanceState.getInt(UserInfoStore.QUICKSEND_DURATION)));
            enableChat.setChecked(savedInstanceState.getBoolean(UserInfoStore.CHAT_ENABLED));
        }

        Button cancel = (Button) v.findViewById(R.id.cancelButton);
        Button done = (Button) v.findViewById(R.id.doneButton);
        cancel.setOnClickListener(this);
        done.setOnClickListener(this);

        return v;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.doneButton:
                saveSettings();
            case R.id.cancelButton:
                getActivity().finish();
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Shouldnt we get the values from the widgets here?
        outState.putString(UserInfoStore.QUICKSEND_MESSAGE, getEditedQuickSendMessage());
        outState.putInt(UserInfoStore.QUICKSEND_DURATION, getEditedQuickSendDuration());
        outState.putBoolean(UserInfoStore.CHAT_ENABLED, getChatEnabledValue());

        super.onSaveInstanceState(outState);
    }

    protected String getEditedQuickSendMessage() {
        return ((EditText)getView().findViewById(R.id.quickSendMessage)).getText().toString();
    }

    protected int getEditedQuickSendDuration() {
        return Integer.parseInt(((EditText) getView().findViewById(R.id.quickSendDuration)).getText().toString());
    }

    protected boolean getChatEnabledValue() {
        return ((CheckBox)getView().findViewById(R.id.enableChat)).isChecked();
    }

    public void saveSettings() {
        uis.saveQuickSendSettings(
                getEditedQuickSendMessage(),
                getEditedQuickSendDuration(),
                getChatEnabledValue()
        );
    }
}
