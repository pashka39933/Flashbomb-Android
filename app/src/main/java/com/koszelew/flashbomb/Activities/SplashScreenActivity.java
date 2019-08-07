package com.koszelew.flashbomb.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.koszelew.flashbomb.Listeners.UploadStateListener;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Networking.ApiParser;
import com.koszelew.flashbomb.Utils.Networking.NotificationProcessingHandler;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.FlashbombToast;
import com.koszelew.flashbomb.Utils.Other.ServerTime;

public class SplashScreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        boolean fromNotification = getIntent().getBooleanExtra("FromNotification", false);
        if (fromNotification) {
            editor.putBoolean(getString(R.string.PREFERENCES_NOTIFICATION_OPENED_FLAG), true);
        }

        editor.putBoolean(getString(R.string.PREFERENCES_APP_LAUNCHED_KEY), true);
        editor.apply();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                /* If nick and pass present, logging in otherwise switching to login/register activity */
                if (sharedPref.contains(getString(R.string.PREFERENCES_LOGIN_KEY)) && sharedPref.contains(getString(R.string.PREFERENCES_PASSWORD_KEY))) {

                    /* Handling URL schemes */
                    editor.remove(getString(R.string.PREFERENCES_URL_OBSERVATION)).apply();
                    if (getIntent().getData() != null && getIntent().getData().getHost() != null) {

                        if (getIntent().getData().getHost().equals("start")) {

                            Log.d("Flashbomb-Log", "Opened with URL host start");

                        } else if (getIntent().getData().getHost().equals("observe")) {

                            if (getIntent().getData().getQueryParameterNames().contains("observation") && getIntent().getData().getQueryParameter("observation").length() > 3) {
                                Log.d("Flashbomb-Log", "Opened with URL host observe?observation=" + getIntent().getData().getQueryParameter("observation"));
                                editor.putString(getString(R.string.PREFERENCES_URL_OBSERVATION), getIntent().getData().getQueryParameter("observation")).apply();
                            }

                        }

                    }

                    String nick = sharedPref.getString(getString(R.string.PREFERENCES_LOGIN_KEY), "");
                    String password = sharedPref.getString(getString(R.string.PREFERENCES_PASSWORD_KEY), "");
                    SingletonNetwork.getInstance(SplashScreenActivity.this).LogIn(nick, password);

                } else {

                    Intent myIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    myIntent.putExtra("caller", this.getClass().getName());
                    SplashScreenActivity.this.startActivity(myIntent);
                    finish();

                }

            }
        }, 50);

    }

    /* Request permissions result callback */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == getResources().getInteger(R.integer.CAMERA_PERMISSIONS_REQUEST_CODE)) {

            boolean cameraGranted = true, audioGranted = true;

            for(int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.CAMERA:
                        cameraGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                        break;
                    case Manifest.permission.RECORD_AUDIO:
                        audioGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                        break;
                    default:
                        break;
                }
            }

            Intent myIntent = new Intent(this, MainActivity.class);

            if(cameraGranted && audioGranted) {

                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                long session_end = Long.parseLong(sharedPref.getString(getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1"));
                long timeleft = session_end - ServerTime.getCurrentTimestamp();
                timeleft = (timeleft > 0) ? timeleft : 0;
                timeleft = (timeleft < getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS)) ? timeleft : getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS);
                myIntent = new Intent(this, CameraActivity.class);
                myIntent.putExtra("type", "default");
                myIntent.putExtra("timeleft", timeleft);
                myIntent.putExtra("session_id", sharedPref.getString(getResources().getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), ""));
                myIntent.putExtra("total_time", getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS));

            } else {

                FlashbombToast.ShowError(this, getString(R.string.camera_permissions_fail), 1000);

            }

            myIntent.putExtra("caller", this.getClass().getSimpleName());
            this.startActivity(myIntent);
            if(cameraGranted && audioGranted)
                System.exit(0);
            else
                this.finish();

        }

    }

    @Override
    protected void onResume() {

        super.onResume();

        /* Setting parser */
        ApiParser.getInstance(getApplicationContext()).SetCurrentActivity(this);

        /* Setting upload status receiver */
        UploadStateListener.currentActivity = this;

        /* Setting notification handler */
        NotificationProcessingHandler.activity = this;

        /* Anim override */
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

}