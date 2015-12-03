package com.joechang.loco.service;

import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Notification;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.PostWriteAction;
import com.joechang.loco.model.User;
import com.joechang.loco.utils.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   10/2/15 10:20 AM
 * Purpose:   Simple little class that runs periodically, such as a check to see if we should send a welcome message.
 * This then puts the message into a queue within the database, which in our current implementation, is picked up
 * by the phone, and sent out.
 */
@Service
public class ScheduledJobService {
    private Logger logger = Logger.getLogger(ScheduledJobService.class.getName());
    private FirebaseManager fb = FirebaseManager.getInstance();

    //Once an hour, see if we should send a welcome message to someone.
    @Scheduled(cron = "0 0 * * * *")
    public void sendWelcomeMessage() {
        final List<User> messageCandidates = new ArrayList<User>();

        fb.findUsers(new PostQueryAction<List<User>>() {
            @Override
            public void doAction(List<User> p) {
                if (p != null) {
                    for (User u : p) {
                        if (!u.getMilestones().keySet().contains(Notification.WELCOME_MESSAGE)) {
                            messageCandidates.add(u);
                        }
                    }
                    addNewMessage(messageCandidates, getMessage());
                }
            }

            @Override
            public void onError(List<User> p) {

            }
        });
    }

    protected void addNewMessage(List<User> p, String message) {
        for (User u : p) {
            Notification n = new Notification();
            n.setUserId(u.getUserId());
            n.setMessage(message);
            n.setType(Notification.WELCOME_MESSAGE);
            n.setStatus(Notification.Status.QUEUED);

            //If no phone number, set to error.
            String ph = u.getPhoneNumber();
            if (StringUtils.isEmpty(ph)) {
                n.setStatus(Notification.Status.ERROR);
            }

            n.setPhoneNumber(ph);

            final User updateUser = u;
            logger.info("Adding " + Notification.WELCOME_MESSAGE + " for distribution to " + n.getPhoneNumber());
            fb.addNotification(n, new PostWriteAction<Notification>() {
                @Override
                public void doAction(Notification objectWritten) {
                    updateUser.getMilestones().put(Notification.WELCOME_MESSAGE, System.currentTimeMillis());
                    fb.updateUser(updateUser);
                }

                @Override
                public void onError(Notification objectNotWritten) {

                }
            });
        }
    }

    //TODO: Place in a resource bundle somewhere.
    protected String getMessage() {
        String msg = new String(
         "Hello from Kursor!\n" +
                 "\n" +
                 "Kursor is a smart assistant that helps you quickly find information directly within your text messages. " +
                 "All you need to do is include the following phone number in your messages for Kursor to help out: 415-312-2379.\n" +
                 "\n" +
                 "Tap and hold the number to add it to your contact list!\n" +
                 "\n" +
                 "You can do things like get yelp reviews.\n" +
                 "@yelp pizza in SOMA\n" +
                 "@yelp delfina\n" +
                 "\n" +
                 "Text @help for a full list of what you can do. Give it a try!"
        );
        return msg;
    }

}
