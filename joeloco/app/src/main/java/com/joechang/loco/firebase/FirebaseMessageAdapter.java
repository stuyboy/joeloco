package com.joechang.loco.firebase;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import com.firebase.client.Query;
import com.joechang.loco.R;
import com.joechang.loco.avatar.AvatarUrlBuilder;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.model.Message;
import com.joechang.loco.utils.BitmapUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   5/26/15 10:32 AM
 * Purpose:
 */
public class FirebaseMessageAdapter extends FirebaseAdapter<Message> {

    // The mUserId for this client. We use this to indicate which messages originated from this user
    private String mUserId;

    public FirebaseMessageAdapter(Query ref, int layout, Context context, LayoutInflater inflater) {
        super(ref, Message.class, layout, context, inflater);
    }

    /**
     * Bind an instance of the <code>Message</code> class to our view. This method is called by <code>FirebaseAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Message</code> instance that represents the current data to bind.
     *
     * @param v        A view instance corresponding to the layout we passed to the constructor.
     * @param message  An instance representing the current state of a chat message
     * @param position Which message in the chat we're talking about.
     */
    @Override
    protected void populateView(View v, Message message, int position) {
        // Map a Chat object to an entry in our listview
        String id = message.getUserId();
        String author = message.getName();

        LinearLayout messageInfo = (LinearLayout)v.findViewById(R.id.messageInfo);
        Space space = (Space)v.findViewById(R.id.messageSpace);
        TextView authorText = (TextView)v.findViewById(R.id.author);
        TextView messageTxt = (TextView)v.findViewById(R.id.message);
        TextView timeTxt = (TextView)v.findViewById(R.id.messageTime);
        ImageView avatar = (ImageView) v.findViewById(R.id.avatar);

        String avatarUrl = AvatarUrlBuilder.squareUrl(id, 100);
        BitmapUtils.cacheAsyncImage(avatar, avatarUrl);

        authorText.setText(author);
        messageTxt.setText(message.getMessage());

        if (message.getSendTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd h:mma", Locale.US);
            timeTxt.setText(sdf.format(new Date(message.getSendTime())));
        } else {
            timeTxt.setText("");
        }

        boolean sameAuthor = false;
        if (position > 0) {
            sameAuthor = messagesBySame(message, getItem(position - 1));
        }

        int visible = sameAuthor ? View.GONE : View.VISIBLE;

        messageInfo.setVisibility(visible);
        avatar.setVisibility(visible);
        space.setVisibility(visible);
    }

    private boolean messagesBySame(Message n, Message o) {

        if (n.getUserId() != null && o.getUserId() != null) {
            return (n.getUserId().equals(o.getUserId()));
        }

        if (n.getUserId() == null && o.getUserId() == null) {
            return (n.getName().equals(o.getName()));
        }

        return false;
    }
}
