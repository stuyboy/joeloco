package com.joechang.loco.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;
import com.joechang.loco.Configuration;
import com.joechang.loco.R;
import com.joechang.loco.client.GroupClient;
import com.joechang.loco.client.RestClientFactory;
import com.joechang.loco.contacts.ContactsUtils;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.joechang.loco.utils.EmailUtils;
import com.joechang.loco.utils.UserInfoStore;
import retrofit.RetrofitError;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by joechang on 5/19/15.
 * Activity that can be called with an Intent to send the current location outbound.
 *
 * BE CAREFUL with any other threads that may handle these methods (such as firebase callbacks)
 * because all the passed variables are saved in ThreadLocals.
 *
 */
public class SendLocationService extends IntentService implements ServiceConnection {
    private Logger log = Logger.getLogger(SendLocationService.class.getName());

    //A link to the locationPublishService, which does all the GPS Work
    private LocationPublishService locationPublishService;

    //Send String arguments in on the Intent Extras using these keys.
    public final static String DESTINATION_TYPE = "_DESTINATION_TYPE";
    public final static String DESTINATION_ADDRESS = "_DESTINATION_ADDRESS";
    public final static String DURATION = "_EVENT_DURATION";
    public final static String MESSAGE = "_EVENT_MESSAGE";

    //Thread Local (hopefully safe) working variables so we don't need to keep passing.
    private ThreadLocal<ContactsUtils.Type> sendType    = new ThreadLocal<ContactsUtils.Type>();
    private ThreadLocal<String> sendAddress             = new ThreadLocal<String>();
    private ThreadLocal<Event> workingEvent             = new ThreadLocal<Event>();
    private ThreadLocal<Integer> eventDurationInMinutes = new ThreadLocal<Integer>();
    private ThreadLocal<String> message                 = new ThreadLocal<String>();

    public SendLocationService() {
        super(SendLocationService.class.getSimpleName());

        //Should this be somewhere else?
        Intent lpsIntent = new Intent(this, LocationPublishService.class);
        bindService(lpsIntent, this, BIND_AUTO_CREATE);
    }

    //Not a big fan of this.  Able to be configured elsewhere?
    //Create an event, use a group where user is the only member, then send.
    public static String getLocationShareLink(Context cxt, String eventId) {
        return Configuration.getProdServerAddress() +
                cxt.getString(R.string.realtimeMapURL) +
                String.format("?%s=%s", Event.ID, eventId);
    }

    @Override
    protected void onHandleIntent(Intent i) {
        sendAddress.set(i.getStringExtra(DESTINATION_ADDRESS));
        sendType.set(ContactsUtils.Type.valueOf(i.getStringExtra(DESTINATION_TYPE)));
        eventDurationInMinutes.set(i.getIntExtra(DURATION, -1));
        message.set(i.getStringExtra(MESSAGE));

        try {
            ensureGroupAndEvent();
            doSend();
        } catch (RetrofitError re) {
            //Connection error.  For now, fail gracefully, but in future, maybe retry connection.
            log.warning("SendLocationService experiencing network errors: " + re.getMessage());
            showToast(getString(R.string.network_unavailable_error));
        }
    }

    public void ensureGroupAndEvent() {
        String thisUser = UserInfoStore.getInstance(this).getUserId();

        Set<String> selfie = new HashSet<String>();
        selfie.add(thisUser);

        GroupClient gc = RestClientFactory.getInstance().getGroupClient();
        Group[] gg = gc.getGroupByUserIds(selfie);

        //If no group found, let's create it!
        if (gg.length <= 0) {
            Group newGroup = new Group(getString(R.string.selfGroupName));
            newGroup.getMembers().put(thisUser, thisUser);
            gg = new Group[] { gc.createGroup(newGroup) };
        }

        Event temp = Event.newTempInstance(gg[0].getGroupId());
        temp.setRelativeDateEnd(resolveQuickSendDuration());
        workingEvent.set(temp);
    }

    /**
     * If we were passed an argument, use that, otherwise use the user preferences.
     * @return
     */
    public int resolveQuickSendDuration() {
        Integer arg = eventDurationInMinutes.get();
        if (arg != null && arg > 0) {
            return arg;
        }
        return UserInfoStore.getInstance(this).getQuickSendDuration();
    }

    public String resolveQuickSendMessage() {
        String msg = message.get();
        if (msg != null && msg.length() > 0) {
            return msg;
        }
        return UserInfoStore.getInstance(this).getQuickSendMessage();
    }

    protected void doSend() {
        switch (sendType.get()) {
            case EMAIL:
                saveEvent();
                doSendEmail(sendAddress.get());
                startLocationPublish();
                return;
            case TEXT:
                saveEvent();
                doSendText(sendAddress.get());
                startLocationPublish();
                return;
            default:
                return;
        }
    }

    private void saveEvent() {
        if (workingEvent.get() != null) {
            FirebaseManager.getInstance().updateEvent(workingEvent.get());
        }
    }

    private void deleteEvent() {
        if (workingEvent.get() != null) {
            FirebaseManager.getInstance().deleteEvent(workingEvent.get());
            workingEvent.set(null);
        }
    }

    private void startLocationPublish() {
        this.locationPublishService.overrideRealtimeMode();
    }

    private String getEventId() {
        if (workingEvent.get() != null) {
            return workingEvent.get().getEventId();
        }

        throw new IllegalArgumentException("Event does not exist.");
    }

    public void doSendText(String number) {
        MMSSenderService.sendMMS(getApplicationContext(), number, smsMessageText());
        showToast(getString(R.string.text_sent_confirmation));
    }

    public String smsMessageText() {
        return resolveQuickSendMessage() + " " + getLocationShareLink(this, getEventId());
    }

    public void doSendEmail(String emailAddress) {
        EmailUtils.launchEmailDialog(
                this,
                emailAddress,
                resolveQuickSendMessage(),
                SendLocationService.getLocationShareLink(this, getEventId())
        );
    }

    private void showToast(final String message) {
        Handler h = new Handler(this.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SendLocationService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * The easiest way to communicate with this service.  Just wrap a context, and pass the data along.
     * In this case, this is opimized for the ChooseContactActivity, which will populate these intent data elements
     * automatically.
     * @param cxt
     * @param data
     */
    public static void launchIntent(Context cxt, Intent data) {
        String address = data.getStringExtra(SendLocationService.DESTINATION_ADDRESS);
        ContactsUtils.Type type = ContactsUtils.Type.valueOf(data.getStringExtra(SendLocationService.DESTINATION_TYPE));
        launchIntent(cxt, type, address, null);
    }

    /**
     * This is analogous to launchIntent, except that it uses an intent to launch the SMS application of choice,
     * prepopulating the message with a #loco, to show the user how the SMS functionality works.
     * @param cxt
     * @param data
     */
    public static void launchManualSMSIntent(Context cxt, Intent data) {
        String address = data.getStringExtra(SendLocationService.DESTINATION_ADDRESS);
        Intent ii = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", address, null));
        ii.putExtra("sms_body", cxt.getString(R.string.text_quickSendMessageExample));
        cxt.startActivity(ii);
    }

    /**
     * Easier way to send, via string, by passing just context, the type of communique, and the address.
     * @param cxt
     * @param type
     * @param address
     */
    public static void launchIntent(Context cxt, ContactsUtils.Type type, String address, Integer duration) {
        Intent launchIntent = new Intent(cxt, SendLocationService.class);
        launchIntent.putExtra(SendLocationService.DESTINATION_TYPE, type.toString());
        launchIntent.putExtra(SendLocationService.DESTINATION_ADDRESS, address);
        launchIntent.putExtra(SendLocationService.DURATION, duration);

        //Maybe this should live somewhere else..
        if (duration != null) {
            launchIntent.putExtra(
                    SendLocationService.MESSAGE,
                    String.format(cxt.getString(R.string.text_quickSendDuration), duration));
        }

        cxt.startService(launchIntent);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LocationPublishService.Binder binder = (LocationPublishService.Binder) service;
        this.locationPublishService = binder.getService();
        log.log(Level.INFO, "Bound to LocationPublishService " + name);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        log.log(Level.INFO, "Unbound from LocationPublishService " + name);
    }

}
