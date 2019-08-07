package com.koszelew.flashbomb.Listeners;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koszelew.flashbomb.Activities.CameraActivity;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.PhotoFilters;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/* ImagePreview after capture touch listener (filters managements) */
public class FiltersManagementListener implements View.OnTouchListener {

    /* Activity */
    CameraActivity activity;

    /* Context */
    Context ctx;

    /* Source ImageView */
    private ImageView previewImage1;
    private ImageView previewImage2;

    /* Filters tutorial */
    private LinearLayout filtersTutorial;

    /* Image index */
    private int currentImageIndex;

    /* Latest tap time */
    private long latestClickTime = 0;

    /* Asynchronous loading */
    FiltersLoader currentFiltersLoader;

    /* Firebase analytics */
    private FirebaseAnalytics analytics;

    /* Original bitmap */
    private Bitmap originalBitmap = null;

    /* Filtered bitmaps */
    private ArrayList<Bitmap> filteredBitmaps = new ArrayList<>();

    /* Fade help flag */
    private boolean ImageView1Visible = true;

    /* Tutorial help flag */
    private boolean TutorialVisible = true;

    /* Read screen width */
    int screenWidth = 0;

    /* Constructor */
    public FiltersManagementListener(Context ctx, CameraActivity activity, ImageView previewImage1, ImageView previewImage2, LinearLayout filtersTutorial, FirebaseAnalytics analytics) {

        this.ctx = ctx;
        this.activity = activity;
        this.previewImage1 = previewImage1;
        this.previewImage2 = previewImage2;
        this.filtersTutorial = filtersTutorial;
        this.analytics = analytics;

        Point size = InitApp.screenSize;
        screenWidth = size.x;

    }

    /* Reset images loaded */
    public void Reset() {

        if(currentFiltersLoader != null)
            currentFiltersLoader.cancel(true);
        currentImageIndex = 0;
        ImageView1Visible = true;
        if(this.previewImage1 != null) {
            this.previewImage1.clearAnimation();
            this.previewImage1.setImageBitmap(null);
        }
        if(this.previewImage2 != null) {
            this.previewImage2.setImageBitmap(null);
        }
        if(this.originalBitmap != null) {
            this.originalBitmap.recycle();
            this.originalBitmap = null;
        }
        if(this.filteredBitmaps != null) {
            for(int i = 0; i < this.filteredBitmaps.size(); i++) {
                if(this.filteredBitmaps.get(i) != null) {
                    this.filteredBitmaps.get(i).recycle();
                    this.filteredBitmaps.set(i, null);
                }
            }
            this.filteredBitmaps.clear();
        }
        System.gc();

    }

    /* Update preview ImageView method */
    public void UpdateImage(Bitmap bitmap) {

        /* Reset */
        Reset();

        /* Context */
        this.ctx = ctx;
        this.activity = activity;

        /* Saving original bitmap */
        this.originalBitmap = bitmap;

        /* Resizing filtered bitmap */
        Point size = InitApp.screenSize;
        float scale = ((float) size.x) / ((float) bitmap.getWidth());
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, Math.round(scale * bitmap.getWidth()), Math.round(scale * bitmap.getHeight()), true);

        /* ImageViews setter */
        this.previewImage1 = previewImage1;
        this.previewImage2 = previewImage2;
        this.previewImage1.setImageBitmap(resizedBitmap);
        this.filtersTutorial = filtersTutorial;

        /* Asynchronously loading filters */
        currentFiltersLoader = new FiltersLoader();
        currentFiltersLoader.execute(resizedBitmap);

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

            if (filteredBitmaps.size() > 0 && System.currentTimeMillis() - latestClickTime > 350) {

                currentImageIndex = (currentImageIndex + ((motionEvent.getX() < screenWidth / 2) ? -1 : 1)) % filteredBitmaps.size();
                if(currentImageIndex < 0)
                    currentImageIndex = filteredBitmaps.size() - 1;

                if (ImageView1Visible)
                    previewImage2.setImageBitmap(filteredBitmaps.get(currentImageIndex));
                else
                    previewImage1.setImageBitmap(filteredBitmaps.get(currentImageIndex));

                previewImage1.clearAnimation();

                Animation anim = AnimationUtils.loadAnimation(ctx, ImageView1Visible ? R.anim.fade_out : R.anim.fade_in);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) { previewImage1.setLayerType(View.LAYER_TYPE_NONE, null); }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                previewImage1.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                anim.setDuration(300);
                anim.setInterpolator(new DecelerateInterpolator());
                previewImage1.startAnimation(anim);

                ImageView1Visible = !ImageView1Visible;
                latestClickTime = System.currentTimeMillis();

            }

        /* Hiding filters tutorial */
            if (TutorialVisible) {
                SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                if (sharedPref.getString(ctx.getResources().getString(R.string.PREFERENCES_CAMERA_FILTERS_SHOW), "1").equals("1")) {

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(ctx.getResources().getString(R.string.PREFERENCES_CAMERA_FILTERS_SHOW), "0");
                    editor.apply();
                    Animation fadeOut = AnimationUtils.loadAnimation(ctx, R.anim.fade_out);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }
                        @Override
                        public void onAnimationEnd(Animation animation) { filtersTutorial.setLayerType(View.LAYER_TYPE_NONE, null); }
                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    filtersTutorial.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    filtersTutorial.startAnimation(fadeOut);

                }
                TutorialVisible = false;
            }
            return true;

        }
        return  false;

    }

    /* AsyncTask to prepare filtered bitmaps */
    private class FiltersLoader extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... params) {

            try {

                filteredBitmaps.add(params[0]);
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter1(params[0]));
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter2(params[0]));
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter3(params[0]));
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter4(params[0]));
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter5(params[0]));
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter6(params[0]));
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter7(params[0]));
                if (!params[0].isRecycled())
                    filteredBitmaps.add(PhotoFilters.applyFilter8(params[0]));

            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }

            System.gc();

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

    /* Method to send currently presented image */
    public void SendCurrentImage(String comment) {
        new ImageSender().execute(comment);
    }

    /* AsyncTask to send current image */
    private class ImageSender extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {

                if(currentImageIndex == 0)
                    originalBitmap = originalBitmap;
                else if (currentImageIndex == 1)
                    originalBitmap = PhotoFilters.applyFilter1(originalBitmap);
                else if (currentImageIndex == 2)
                    originalBitmap = PhotoFilters.applyFilter2(originalBitmap);
                else if (currentImageIndex == 3)
                    originalBitmap = PhotoFilters.applyFilter3(originalBitmap);
                else if (currentImageIndex == 4)
                    originalBitmap = PhotoFilters.applyFilter4(originalBitmap);
                else if (currentImageIndex == 5)
                    originalBitmap = PhotoFilters.applyFilter5(originalBitmap);
                else if (currentImageIndex == 6)
                    originalBitmap = PhotoFilters.applyFilter6(originalBitmap);
                else if (currentImageIndex == 7)
                    originalBitmap = PhotoFilters.applyFilter7(originalBitmap);
                else if (currentImageIndex == 8)
                    originalBitmap = PhotoFilters.applyFilter8(originalBitmap);

                File output = new File(ctx.getFilesDir() + "/Flashbomb/image.jpg");
                FileOutputStream out = new FileOutputStream(output);
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, ctx.getResources().getInteger(R.integer.JPG_QUALITY), out);
                out.flush();
                out.close();
                originalBitmap.recycle();
                System.gc();

                SingletonNetwork.getInstance(ctx).UploadPhoto(output.getAbsolutePath(), params[0], new String[]{}, activity.getIntent().getExtras().getString("session_id"));

                /* Session ends for this user */
                SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(ctx.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1");
                editor.apply();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.CloseActivity();
                    }
                });

            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }

            System.gc();

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
