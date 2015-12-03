package com.joechang.loco.fragment;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.joechang.loco.R;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.firebase.FirebaseMessageAdapter;
import com.joechang.loco.model.ChatSession;
import com.joechang.loco.model.Message;
import com.joechang.loco.utils.UserInfoStore;

/**
 * Author:    joechang
 * Created:   5/26/15 11:44 AM
 * Purpose:
 */
public class ChatFragment extends Fragment implements View.OnClickListener {

    private Firebase mFirebase;
    private ListView mMessageListView;
    private FrameLayout mBottomBuffer;
    private boolean mShowFlingBar = true;

    public static ChatFragment newInstance() {
        ChatFragment cf = new ChatFragment();
        return cf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        Button b = (Button) v.findViewById(R.id.chatSendButton);
        b.setOnClickListener(this);

        View flingBar = v.findViewById(R.id.flingBar);
        flingBar.setVisibility(mShowFlingBar ? View.VISIBLE : View.GONE);

        EditText et = (EditText) v.findViewById(R.id.chatMessageBox);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        mMessageListView = (ListView) v.findViewById(R.id.messageListView);
        mBottomBuffer = (FrameLayout) v.findViewById(R.id.fillFrameHack);
        refresh();

        return v;
    }

    public boolean refresh() {
        String chatId = findChatId();
        if (chatId != null) {
            mFirebase = FirebaseManager.getInstance().getChatMessagesFirebase(chatId);
            mMessageListView.setAdapter(getAdapter());
            return true;
        }

        return false;
    }

    public void doBottomFillerHack(boolean b) {
        if (mBottomBuffer == null) {
            return;
        }

        if (b) {
            mBottomBuffer.setVisibility(View.VISIBLE);
        } else {
            mBottomBuffer.setVisibility(View.GONE);
        }
    }

    protected String findChatId() {
        /*
        if (savedInstanceState != null) {
            String sisChatId = savedInstanceState.getString(ChatSession.ID);
            if (sisChatId != null) {
                return sisChatId;
            }
        }
        */

        Bundle arguments = getArguments();

        if (arguments != null) {
            String argChatId = arguments.getString(ChatSession.ID);
            if (argChatId != null) {
                return argChatId;
            }
        }

        return null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.chatSendButton) {
            sendMessage();
        }
    }

    protected String getMessage() {
        EditText et = (EditText) getView().findViewById(R.id.chatMessageBox);
        String msg = et.getText().toString();
        return msg;
    }

    protected void resetMessageBox() {
        EditText et = (EditText) getView().findViewById(R.id.chatMessageBox);
        et.setText("");
    }

    protected void sendMessage() {
        String msg = getMessage();
        UserInfoStore uis = UserInfoStore.getInstance(getActivity());

        if (!msg.equals("")) {
            persistMessage(uis.getUserId(), uis.getName(), msg);
            resetMessageBox();
        }
    }

    protected void persistMessage(String userId, String name, String message) {
        if (mFirebase != null) {
            Message m = new Message(
                    userId,
                    name,
                    message
            );
            mFirebase.push().setValue(m);
        }
    }

    protected ListAdapter getAdapter() {
        if (mFirebase == null) {
            throw new IllegalArgumentException("Must initialize reference before getting adapter.");
        }

        final FirebaseMessageAdapter fka = new FirebaseMessageAdapter(
                mFirebase,
                R.layout.message_item_simple,
                getActivity(),
                getActivity().getLayoutInflater()
        );

        fka.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mMessageListView.setSelection(fka.getCount() - 1);
            }
        });

        return fka;
    }

    public void setShowFlingBar(boolean mShowFlingBar) {
        this.mShowFlingBar = mShowFlingBar;
    }

    public void clearMessages() {
        if (mFirebase == null) {
            return;
        }

        mFirebase.removeValue(new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                refresh();
            }
        });
    }

}
