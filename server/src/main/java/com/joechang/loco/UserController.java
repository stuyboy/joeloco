package com.joechang.loco;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.joechang.loco.config.Routes;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.listener.DeferredResultValueListener;
import com.joechang.loco.listener.DeferredSetResultValueListener;
import com.joechang.loco.logging.LogLocationEntry;
import com.joechang.loco.logging.StatusResponse;
import com.joechang.loco.model.PostQueryAction;
import com.joechang.loco.model.User;
import com.joechang.loco.response.ResourceNotFoundException;
import com.joechang.loco.response.ServerException;
import com.joechang.loco.response.UserResult;
import com.joechang.loco.service.EventMonitoringService;
import com.joechang.loco.service.GitkitClientService;
import com.joechang.loco.service.Stagnancy;
import com.joechang.loco.utils.ImageUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Date;

/**
 * Author:    joechang
 * Created:   5/22/15 5:40 PM
 * Purpose:   Primary controller for user API
 */
@RestController
public class UserController {
    private static final String LOGFILENAME = "userId";
    private static final ObjectMapper om = new ObjectMapper();
    private static Logger userLogger = LoggerFactory.getLogger(LogLocationEntry.class);
    private static Logger log = LoggerFactory.getLogger(UserController.class);

    private EventMonitoringService mEMS;
    private FirebaseManager firebaseManager;

    private final String DEFAULT_AVATAR_FILE = "public/img/webuseravatar.png";
    private final BufferedImage DEFAULT_AVATAR = ImageUtils.fileStringToImage(DEFAULT_AVATAR_FILE);

    @RequestMapping(Routes.USER_BY_ID)
    public UserResult getUserById(
            @PathVariable(value = User.ID) final String userId
    ) {
        UserResult ur = new UserResult();
        firebaseManager.getUserFirebase(userId)
                .addListenerForSingleValueEvent(DeferredResultValueListener.instance(ur, userId));
        return ur;
    }

    //Dangerous method, for debugging mostly.
    @RequestMapping(Routes.USERS_ALL)
    public UserResult.Set getAllUsers() {
        UserResult.Set urs = new UserResult().set();
        firebaseManager.getUserFirebase()
                .addListenerForSingleValueEvent(DeferredSetResultValueListener.instance(urs));
        return urs;
    }

    @RequestMapping(Routes.USER_LOCATION)
    public StatusResponse logLocation(@PathVariable(value = User.ID) final String userId,
                                      @RequestBody LogLocationEntry lle) {
        StatusResponse sr = doLogLocationEntry(lle);

        if (lle.getUserId() != null) {
            //Also check is we're in the middle of an EVENT, because stagnancy is then REALTIME.
            sr.setActiveEvents(mEMS.getActiveEvents(userId, new Date()));
            if (sr.getActiveEvents().size() > 0) {
                sr.setRequestedStagnancy(Stagnancy.REALTIME);
            }
        }

        return sr;
    }

    /**
     * Find a user using the phoneNumber, return when ready.
     * @param phoneNumber
     * @return
     */
    @RequestMapping(Routes.USER_RESOURCE)
    public UserResult findUser(
            @RequestParam(value = User.PHONE_NUMBER) final String phoneNumber
    ) {
        UserResult ur = new UserResult();

        firebaseManager.getUserFirebase()
                .orderByChild(User.PHONE_NUMBER)
                //.orderByChild("createdTime")
                .equalTo(phoneNumber)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(
                        DeferredResultValueListener.instance(ur, phoneNumber));

        return ur;
    }

    /**
     * Possibly to be moved out to a separate process, generated at first user creation, and then referenced.
     * Not exactly the most efficient, as we're going out to google and facebook, and then resizing.
     * In the future, either cache this locally, or produce file, etc.  For now, izzok.
     * @param id
     * @param pixels
     * @param shape
     * @return image, configured for you
     */
    @RequestMapping(Routes.USER_AVATAR)
    public DeferredResult<HttpEntity<byte[]>> getAvatarImage(
            @PathVariable(value = User.ID) final String id,
            @RequestParam(value = "sz", required = false) final Integer pixels,
            @RequestParam(value = "sp", required = false, defaultValue = "CIRCLE") final ImageUtils.Shape shape
    ) {
        final DeferredResult<HttpEntity<byte[]>> imgReturn = new DeferredResult<HttpEntity<byte[]>>();

        firebaseManager.findUserById(id, new PostQueryAction<User>() {
            @Override
            public void doAction(User p) {
                BufferedImage newImage = DEFAULT_AVATAR;

                if (p != null && p.getPhotoUrl() != null) {
                    try {
                        newImage = ImageUtils.urlStringToImage(p.getPhotoUrl());
                        //newImage = ImageUtils.mapPointer(ImageUtils.urlStringToImage(p.getPhotoUrl()));
                    } catch (Exception e) {
                        //no-op
                    }
                }

                if (pixels != null) {
                    double scale = ImageUtils.findRoughScale(newImage, pixels);
                    if (scale > 20) {
                        imgReturn.setErrorResult(new ServerException("Scaling limited to 20x"));
                    }

                    newImage = ImageUtils.scaleImage(newImage, scale, shape);
                }

                byte[] ret = ImageUtils.imageToByteArray(newImage, "png");
                imgReturn.setResult(new HttpEntity<>(ret, pngHeader(ret)));
            }

            @Override
            public void onError(User p) {

            }
        });

        return imgReturn;
    }

    @RequestMapping(Routes.USER_MAP_POINTER)
    public DeferredResult<HttpEntity<byte[]>> getMapPointerImage(
            @PathVariable(value = User.ID) final String id,
            @RequestParam(value = "sz", required = false, defaultValue = "50") final Integer height
    ) {
        final DeferredResult<HttpEntity<byte[]>> imgReturn = new DeferredResult<HttpEntity<byte[]>>();

        firebaseManager.findUserById(id, new PostQueryAction<User>() {
            @Override
            public void doAction(User p) {
                BufferedImage newImage = DEFAULT_AVATAR;

                if (p != null && p.getPhotoUrl() != null) {
                    try {
                        newImage = ImageUtils.mapPointer(ImageUtils.urlStringToImage(p.getPhotoUrl()), height);
                    } catch (Exception e) {
                        //no-op
                    }
                }

                byte[] ret = ImageUtils.imageToByteArray(newImage, "png");
                imgReturn.setResult(new HttpEntity<>(ret, pngHeader(ret)));
            }

            @Override
            public void onError(User p) {

            }
        });

        return imgReturn;
    }

    @RequestMapping(Routes.USER_AUTH)
    public DeferredResult<GitkitUser> authenticateGitkitToken(
            @RequestBody final String token
    ) {
        DeferredResult<GitkitUser> dr = new DeferredResult<>();
        GitkitClientService gcs = new GitkitClientService();

        try {
            GitkitUser gu = gcs.getClient().validateToken(token);
            dr.setResult(gu);
        } catch (GitkitClientException gkce) {
            dr.setErrorResult("No good.");
        }

        return dr;
    }

    private HttpHeaders pngHeader(byte[] imgByteArray) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.IMAGE_PNG);
        header.setContentLength(imgByteArray.length);
        return header;
    }

    private StatusResponse doLogLocationEntry(LogLocationEntry lle) {
        StatusResponse ret = new StatusResponse(StatusResponse.OK);

        if (validateEntry(lle)) {
            try {
                logLocationEntry(lle);
            } catch (Exception ee) {
                log.error("Error in logLocation", ee);
            }
        } else {
            log.error("Error in request: %s, %s, %s", lle.getUserId(), lle.getLatitude(), lle.getLongitude());
            ret.setResponse(StatusResponse.MALFORMED);
        }

        return ret;
    }

    private boolean validateEntry(LogLocationEntry lle) {
        if (lle != null) {
            if ((lle.getUserId() != null && !lle.getUserId().isEmpty()) &&
                    (lle.getLongitude() != 0.0) &&
                    (lle.getLatitude() != 0.0)) {
                return true;
            }
        }
        return false;
    }

    private void logLocationEntry(LogLocationEntry lle) throws JsonProcessingException {
        MDC.put(LOGFILENAME, lle.getUserId());
        userLogger.info(om.writeValueAsString(lle));
        MDC.remove(LOGFILENAME);
    }

    @Autowired
    public void setEventMonitoringService(EventMonitoringService ems) {
        this.mEMS = ems;
    }

    @Autowired
    public void setFirebaseManager(FirebaseManager fm) {
        this.firebaseManager = fm;
    }
}
