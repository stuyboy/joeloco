package com.joechang.loco.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.google.identitytoolkit.GitkitUser;
import com.joechang.loco.model.ImageUpload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by joechang on 11/30/14.
 */
public class CameraUtils {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1433;

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Utility method so you don't need to remember the MediaStore action.
     * @return
     */
    public static Intent getCameraIntent() {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    /**
     * To be called typically by onActivityResult, passing in resultCode and data, get back
     * an ImageUpload ready for Firebase or otherwise.
     * @param c
     * @param resultCode
     * @param data
     * @return
     */
    public static ImageUpload doCameraIntentResult(Context c, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bb = (Bitmap)data.getExtras().get("data");
            Location ll = LocationUtils.getInstance(c).getCurrentLocation();
            GitkitUser user = UserInfoStore.getInstance(c).getSavedGitkitUser();
            ImageUpload iu = BitmapUtils.createImageUpload(
                    user.getLocalId(),
                    user.getEmail(),
                    null,
                    ll,
                    bb
            );
            return iu;
        }

        return null;
    }

}
