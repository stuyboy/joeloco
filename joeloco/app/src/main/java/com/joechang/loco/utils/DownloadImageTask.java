package com.joechang.loco.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import com.joechang.loco.client.AuthInterceptor;
import com.squareup.okhttp.Credentials;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Author:  joechang
 * Date:    12/30/14
 * Purpose:
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    private static LruCache<String, Bitmap> cacheRef = new LruCache<>(50);
    String url;
    PostDownloadActor pda;

    public DownloadImageTask(PostDownloadActor pda) {
        this.pda = pda;
    }

    public DownloadImageTask(final ImageView bmImage) {
        this(new PostDownloadActor() {
            @Override
            public void doOnFinish(Bitmap result) {
                if (bmImage != null) {
                    bmImage.setImageBitmap(result);
                }
            }
        });
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = cacheRef.get(urldisplay);

        this.url = urldisplay;

        if (mIcon11 == null) {
            InputStream in = null;
            try {
                //InputStream in = new java.net.URL(urldisplay).openStream();
                in = createInputStream(urldisplay);
                mIcon11 = BitmapFactory.decodeStream(in);
                cacheRef.put(urldisplay, mIcon11);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                        //nothing.
                    }
                }
            }
        }

        return mIcon11;
    }

    private InputStream createInputStream(String url) throws MalformedURLException, IOException {
        URL u = new URL(url);
        URLConnection uc = u.openConnection();

        //Need to clean this up somehow.
        uc.setRequestProperty(AuthInterceptor.AUTH_HEADER_KEY, AuthInterceptor.getCredentials());

        return (InputStream)uc.getContent();
    }

    protected void onPostExecute(Bitmap result) {
        if (pda != null) {
            pda.doOnFinish(result);
        }
    }

    public interface PostDownloadActor {
        public void doOnFinish(Bitmap result);
    }
}
