package com.koszelew.flashbomb.Utils.EntitiesProcessing;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FlashbombEntityLoader {

    /* Class instance */
    private static FlashbombEntityLoader mInstance;

    /* Context variable */
    private static Context mCtx;

    /* VideosLoader async task's instances */
    private ArrayList<VideosLoader> activeVideoLoadersBuffer = new ArrayList<>();

    /* Maximum number of active VideoLoaders working parallel */
    private int maxActiveVideoLoaders = 3;

    /* Constructor */
    private FlashbombEntityLoader(Context context) {
        mCtx = context;
    }

    /* GetInstance method */
    public static synchronized FlashbombEntityLoader getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FlashbombEntityLoader(context);
        }
        return mInstance;
    }

    public void LoadImageFromUrl(final String url, final ImageView image) {
        Picasso.with(mCtx)
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso
                                .with(mCtx)
                                .load(url)
                                .into(image);
                    }
                });
    }

    public void LoadImageFromPath(final String path, final ImageView image) {
        Picasso.with(mCtx)
                .load(path)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso
                                .with(mCtx)
                                .load(path)
                                .into(image);
                    }
                });
    }

    public void FetchImageFromPath(final String path) {
        Picasso.with(mCtx)
                .load(path)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fetch(new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso
                                .with(mCtx)
                                .load(path);
                    }
                });
    }

    public void LoadImageFromFile(final File file, final ImageView image) {
        Picasso.with(mCtx)
                .load(file)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso
                                .with(mCtx)
                                .load(file)
                                .into(image);
                    }
                });
    }

    public void LoadImageFromResource(final int resId, final ImageView image) {
        Picasso.with(mCtx)
                .load(resId)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso
                                .with(mCtx)
                                .load(resId)
                                .into(image);
                    }
                });
    }

    public void FetchImageFromUrl(final String url) {
        Picasso.with(mCtx)
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fetch(new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso
                                .with(mCtx)
                                .load(url)
                                .fetch();
                    }
                });
    }

    public void FetchAndLoadImageFromFile(final File file, final ImageView image) {
        Picasso.with(mCtx)
                .load(file)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fetch(new Callback() {
                    @Override
                    public void onSuccess() {
                        LoadImageFromFile(file, image);
                    }

                    @Override
                    public void onError() {
                        Picasso
                                .with(mCtx)
                                .load(file)
                                .fetch(new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        LoadImageFromFile(file, image);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }
                });
    }

    public void LoadVideo(final String url, final MediaPlayer mediaPlayer) {

        String proxyUrl = InitApp.getProxy(mCtx).getProxyUrl(url);

        try {

            mediaPlayer.setDataSource(mCtx, Uri.parse(proxyUrl));

        } catch (IOException e) {
            Crashlytics.logException(e);
        }

    }

    public void FetchVideo(final String url) {

        try {

            VideosLoader vl = new VideosLoader();
            vl.execute(new URL(InitApp.getProxy(mCtx).getProxyUrl(url)));
            activeVideoLoadersBuffer.add(vl);

            if(activeVideoLoadersBuffer.size() > maxActiveVideoLoaders) {
                activeVideoLoadersBuffer.get(0).cancel(true);
                activeVideoLoadersBuffer.remove(0);
            }

        } catch (MalformedURLException e) {
            Crashlytics.logException(e);
        }

    }

    /* AsyncTask to prefetch videos */
    private class VideosLoader extends AsyncTask<URL, Void, Void> {

        @Override
        protected Void doInBackground(URL... params) {

            try {

                URL fetchUrl = params[0];
                InputStream inputStream = fetchUrl.openStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                while ((inputStream.read(buffer)) != -1) {}
                inputStream.close();

            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
