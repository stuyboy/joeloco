package com.joechang.loco.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.ImageUpload;
import com.joechang.loco.model.LocTime;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A quick class to contain all the image slogging stuff.
 */
public class BitmapUtils {
    private static String LOGTAG = BitmapUtils.class.getSimpleName();

    public static Bitmap decodeByteArray(byte[] bytes) {
        return decodeByteArray(bytes, null);
    }

    public static Bitmap decodeByteArray(byte[] bytes, BitmapFactory.Options options) {
        return decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static Bitmap decodeByteArray(byte[] bytes, int offset, int length) {
        return decodeByteArray(bytes, offset, length, null);
    }

    public static Bitmap decodeByteArray(byte[] bytes, int offset, int length, BitmapFactory.Options options) {
        if (bytes.length <= 0) {
            throw new IllegalArgumentException("bytes.length " + bytes.length
                    + " must be a positive number");
        }

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(bytes, offset, length, options);
        } catch (OutOfMemoryError e) {
            Log.e(LOGTAG, "decodeByteArray(bytes.length=" + bytes.length
                    + ", options= " + options + ") OOM!", e);
            return null;
        }

        if (bitmap == null) {
            Log.w(LOGTAG, "decodeByteArray() returning null because BitmapFactory returned null");
            return null;
        }

        if (bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            Log.w(LOGTAG, "decodeByteArray() returning null because BitmapFactory returned "
                    + "a bitmap with dimensions " + bitmap.getWidth()
                    + "x" + bitmap.getHeight());
            return null;
        }

        return bitmap;
    }

    /**
     * Create a base64 encoded data uri from a bitmap image.
      * @param bb
     * @return the base64 data uri format
     */
    public static String bitmapToDataUri(Bitmap bb) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bb.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Decodes a bitmap from a Base64 data URI.
     *
     * @param dataURI a Base64-encoded data URI string
     * @return        the decoded bitmap, or null if the data URI is invalid
     */
    public static Bitmap getBitmapFromDataURI(String dataURI) {
        if (dataURI == null) {
            return null;
        }

        final String base64 = dataURI.substring(dataURI.indexOf(',') + 1);
        try {
            byte[] raw = Base64.decode(base64, Base64.DEFAULT);
            return decodeByteArray(raw);
        } catch (Exception e) {
            Log.e(FirebaseManager.class.toString(), "exception decoding bitmap from data URI: " + dataURI, e);
        }
        return null;
    }

    public static Bitmap getBitmapfromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(input);
            return bm;
        } catch (IOException e) {
            Log.e(BitmapUtils.class.toString(), e.getMessage(), e);
        }

        return null;
    }

    /**
     * Generic method to download and populate any imageview.  Also uses a global cache to make
     * these images ready to go.
     * @param iv
     * @param url
     */
    public static void cacheAsyncImage(ImageView iv, String url) {
        DownloadImageTask dit = new DownloadImageTask(iv);
        dit.execute(url);
    }

    public static Bitmap cropToCircle(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float)(width / 2)
                , (float)(height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    /**
     * Convenient method to turn an imageUpload object into the image.
     *
     * @return
     */
    public static Bitmap toBitmap(ImageUpload iu) {
        return BitmapUtils.getBitmapFromDataURI(iu.getFilePayload());
    }

    public static ImageUpload createImageUpload(
            String userId,
            String username,
            String groupId,
            Location location,
            Bitmap bb) {
        ImageUpload iu = new ImageUpload();
        iu.setUserId(userId);
        iu.setUsername(username);
        iu.setGroupId(groupId);
        iu.setLoctime(new LocTime(location.getLatitude(), location.getLongitude()));
        iu.setUploadTime(System.currentTimeMillis());

        String filePayload = BitmapUtils.bitmapToDataUri(bb);
        iu.setFilePayload(filePayload);

        return iu;
    }

    public static Bitmap toBitmap(Context cxt, String mediaLocation) {
        Bitmap bmp = null;
        FileInputStream is = null;
        try {
            is = cxt.openFileInput(mediaLocation);
            bmp = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            Log.e(LOGTAG, "Could not handle media: " + mediaLocation);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException i) {
                //no-op
            }
        }
        return bmp;
    }
}
