package com.koszelew.flashbomb.Utils.Other;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.koszelew.flashbomb.BuildConfig;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Networking.NotificationProcessingHandler;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.ArrayList;

public class InitApp extends Application {

    public static NotificationProcessingHandler notificationProcessingHandler;
    public static Typeface contentsTypeface;
    public static Typeface clickablesTypeface;
    public static Typeface headersTypeface;
    public static Typeface timeTypeface;
    public static Point screenSize;
    public static float screenAspect;
    public static boolean gyroscopeAvailable;
    public static boolean isSessionActive;

    @Override
    public void onCreate() {

        super.onCreate();

        /* Enabling logging only in release */
        FirebaseApp.initializeApp(this);
        FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        FirebasePerformance.getInstance().setPerformanceCollectionEnabled(!BuildConfig.DEBUG);

        /* Dependency for image processing */
        System.loadLibrary("NativeImageProcessor");

        /* Used for upload lib */
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.HTTP_STACK = new OkHttpStack();

        /* Used for picasso */
        okhttp3.OkHttpClient okHttp3Client = new okhttp3.OkHttpClient();
        new Picasso.Builder(this).downloader(new OkHttp3Downloader(okHttp3Client)).build();

        /* Used for OneSignal */
        notificationProcessingHandler = new NotificationProcessingHandler();
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        final SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        OneSignal.setSubscription(
            sharedPref.contains(getString(R.string.PREFERENCES_LOGIN_KEY)) &&
            sharedPref.contains(getString(R.string.PREFERENCES_PASSWORD_KEY)) &&
            sharedPref.getBoolean(getString(R.string.PREFERENCES_ACCOUNT_VERIFIED), false)
        );

        /* Fonts */
        contentsTypeface = Typeface.createFromAsset(getAssets(), "fonts/contents.ttf");
        clickablesTypeface = Typeface.createFromAsset(getAssets(), "fonts/clickables.ttf");
        headersTypeface = Typeface.createFromAsset(getAssets(), "fonts/headers.ttf");
        timeTypeface = Typeface.createFromAsset(getAssets(), "fonts/time.ttf");

        /* Screen Size */
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        screenSize = new Point();
        wm.getDefaultDisplay().getSize(screenSize);
        screenAspect = ((float)screenSize.y / (float)screenSize.x);

        /* Gyroscope */
        gyroscopeAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);

    }

    /* Used for video caching */
    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        InitApp app = (InitApp) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }

    /* Permissions Management - returns true if something requested */
    public static boolean RequestLackingPermissions(Activity activity, int RequestCode) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if(RequestCode == activity.getResources().getInteger(R.integer.CAMERA_PERMISSIONS_REQUEST_CODE)) {

                boolean cameraPermitted = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
                boolean audioPermitted = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
                boolean locationPermitted = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

                ArrayList<String> lackingPermissions = new ArrayList<>();

                if (!cameraPermitted)
                    lackingPermissions.add(Manifest.permission.CAMERA);
                if (!audioPermitted)
                    lackingPermissions.add(Manifest.permission.RECORD_AUDIO);
                if (!locationPermitted)
                    lackingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

                if (!cameraPermitted || !audioPermitted) {

                    ActivityCompat.requestPermissions(activity, lackingPermissions.toArray(new String[0]), RequestCode);
                    return true;

                }

            } else if(RequestCode == activity.getResources().getInteger(R.integer.EXTERNAL_SAVE_PERMISSIONS_REQUEST_CODE)) {

                boolean writePermitted = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

                ArrayList<String> lackingPermissions = new ArrayList<>();

                if(!writePermitted)
                    lackingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(!writePermitted) {

                    ActivityCompat.requestPermissions(activity, lackingPermissions.toArray(new String[0]), RequestCode);
                    return true;

                }

            }

        }

        return false;

    }

    /* Check if running on emulator */
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

}
