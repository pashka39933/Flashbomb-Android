package com.koszelew.flashbomb.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koszelew.flashbomb.Listeners.CameraVideoListener;
import com.koszelew.flashbomb.Listeners.FiltersManagementListener;
import com.koszelew.flashbomb.Listeners.UploadStateListener;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.UIComponents.PanoramaImageView;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntity;
import com.koszelew.flashbomb.Utils.Networking.ApiParser;
import com.koszelew.flashbomb.Utils.Networking.NotificationProcessingHandler;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.FlashbombToast;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    /* Camera View */
    private TextureView cameraTextureView;

    /* Focus mark */
    private ImageView focusMark;

    /* Camera capture layout */
    private RelativeLayout cameraCaptureLayout;

    /* Media recorder */
    private MediaRecorder recorder;

    /* Camera instance */
    private Camera camera;

    /* Help variables */
    boolean recording = false;

    /* Capture layout */
    private LinearLayout captureLayout;

    /* Timeleft / message layouts */
    private RelativeLayout timeleftProgressLayout, timeleftTextLayout;
    private LinearLayout messageLayout;

    /* Message EditText */
    private EditText messageEditText;

    /* Capture animation */
    private LottieAnimationView captureAnimation;

    /* Preview layout */
    private RelativeLayout previewLayout;

    /* Preview image */
    private PanoramaImageView previewImage1;
    private PanoramaImageView previewImage2;

    /* Preview ImageView touch listener */
    private FiltersManagementListener previewImageListener;

    /* Preview video */
    private TextureView previewVideo;

    /* Preview video surface listener */
    private CameraVideoListener previewVideoListener;

    /* Current camera facing */
    private int currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    /* Current camera id */
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    /* Video output file */
    File videoOutput;

    /* Help structure determining upload type */
    private enum UploadType {
        VIDEO,
        PHOTO
    }

    /* Current upload type */
    UploadType currentUploadType = null;

    /* Help flag to determine upload process */
    private boolean duringUpload = false;

    /* Timeleft view */
    TickerView timeleftView;
    RelativeLayout timeleftProgress;

    /* Help flag */
    private boolean capturePossible = true;

    /* Location provider */
    private FusedLocationProviderClient mFusedLocationClient;

    /* Analytics instance */
    private FirebaseAnalytics analytics;

    /* Tutorial layouts */
    private LinearLayout captureTutorial;
    private LinearLayout filtersTutorial;

    /* Orientation change listener */
    OrientationEventListener orientationListener;
    int displayRotTmp = 0, deviceRotTmp = 0, cameraOrientationTmp = 0;

    /* InfoComment tag */
    String comment = "";

    /* Country info */
    TextView countryTextView;
    ImageView countryEmojiImageView;

    /* Initialization */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        analytics = FirebaseAnalytics.getInstance(this);

        /* Setting texts fonts */
        ((TextView) findViewById(R.id.captureTutorialText)).setTypeface(InitApp.contentsTypeface);
        ((TextView) findViewById(R.id.filtersTutorialText)).setTypeface(InitApp.contentsTypeface);
        ((TextView) findViewById(R.id.cameraPreviewCountryText)).setTypeface(InitApp.contentsTypeface);

        /* Screen size */
        final Point size = InitApp.screenSize;

        /* Getting geo data */
        final String UnknownCountryCode = "XX";

        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getApplicationContext().getString(R.string.PREFERENCES_COUNTRYCODE_KEY), UnknownCountryCode);
        editor.remove(getApplicationContext().getString(R.string.PREFERENCES_LATITUDE_KEY));
        editor.remove(getApplicationContext().getString(R.string.PREFERENCES_LOCALITY_KEY));
        editor.remove(getApplicationContext().getString(R.string.PREFERENCES_LONGITUDE_KEY));
        editor.remove(getString(R.string.PREFERENCES_NOTIFICATION_OPENED_FLAG));
        editor.apply();

        /* Getting views */
        focusMark = findViewById(R.id.focusMark);
        cameraCaptureLayout = findViewById(R.id.cameraCaptureLayout);
        captureLayout = findViewById(R.id.captureLayout);
        captureAnimation = findViewById(R.id.cameraCaptureButtonLottie);
        previewLayout = findViewById(R.id.cameraPreviewLayout);
        previewImage1 = findViewById(R.id.cameraPreviewImage1);
        previewImage2 = findViewById(R.id.cameraPreviewImage2);
        previewVideo = findViewById(R.id.cameraPreviewVideo);
        timeleftView = findViewById(R.id.timeleftView);
        timeleftProgress = findViewById(R.id.timeleftProgress);
        timeleftProgressLayout = findViewById(R.id.cameraTimeleftProgressLayout);
        timeleftTextLayout = findViewById(R.id.cameraTimeleftTextLayout);
        messageLayout = findViewById(R.id.cameraInfluencerMessageLayout);
        messageEditText = findViewById(R.id.cameraInfluencerMessageEditText);
        messageEditText.setTypeface(InitApp.clickablesTypeface);
        captureTutorial = findViewById(R.id.captureTutorial);
        filtersTutorial = findViewById(R.id.filtersTutorial);
        countryTextView = findViewById(R.id.cameraPreviewCountryText);
        countryEmojiImageView = findViewById(R.id.cameraPreviewCountryEmoji);

        try {
            /* Setting initial country info */
            countryTextView.setText(getResources().getString(R.string.unknown_country));
            countryEmojiImageView.setImageDrawable(Drawable.createFromStream(getAssets().open(FlashbombEntity.getFlagEmoji(UnknownCountryCode)), null));


            /* Trying from telephony (just country code) */
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {

                String countryCode = tm.getNetworkCountryIso();
                if (countryCode != null && countryCode.length() == 2) {

                    editor.putString(getApplicationContext().getString(R.string.PREFERENCES_COUNTRYCODE_KEY), countryCode.toUpperCase());
                    editor.apply();

                    String country = new Locale("", countryCode).getDisplayCountry();
                    String emoji = FlashbombEntity.getFlagEmoji(countryCode);
                    countryTextView.setText(country);
                    countryEmojiImageView.setImageDrawable(Drawable.createFromStream(getAssets().open(emoji), null));

                }
            }
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

        /* Trying from location */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    String countryCode = "";
                    String locality = "";
                    String latitude = "";
                    String longitude = "";

                    if (location != null) {

                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {

                                countryCode = addresses.get(0).getCountryCode();
                                locality = addresses.get(0).getLocality();
                                latitude = String.valueOf(addresses.get(0).getLatitude());
                                longitude = String.valueOf(addresses.get(0).getLongitude());
                            }
                        } catch (Exception e) {
                            Log.e("Flashbomb-Log", "catch", e);
                            Crashlytics.logException(e);
                        }

                    }

                    if (countryCode != null && countryCode.length() > 0)
                        editor.putString(getApplicationContext().getString(R.string.PREFERENCES_COUNTRYCODE_KEY), countryCode);
                    if (locality != null && locality.length() > 0)
                        editor.putString(getApplicationContext().getString(R.string.PREFERENCES_LOCALITY_KEY), locality);
                    editor.putString(getApplicationContext().getString(R.string.PREFERENCES_LATITUDE_KEY), latitude);
                    editor.putString(getApplicationContext().getString(R.string.PREFERENCES_LONGITUDE_KEY), longitude);
                    editor.apply();

                    /* Setting preview country info */
                    try {
                        if (countryCode != null && countryCode.length() > 0) {
                            String country = countryCode.equals("XX") ? getResources().getString(R.string.unknown_country) : new Locale("", countryCode).getDisplayCountry();
                            String emoji = FlashbombEntity.getFlagEmoji(countryCode);
                            countryTextView.setText((locality != null && locality.length() > 0) ? locality : country);
                            countryEmojiImageView.setImageDrawable(Drawable.createFromStream(getAssets().open(emoji), null));
                        }
                    } catch (Exception e) {
                        Log.e("Flashbomb-Log", "catch", e);
                        Crashlytics.logException(e);
                    }


                }
            });

        }

        /* Setting camera focus on click */
        cameraTextureView = findViewById(R.id.cameraView);
        cameraTextureView.setSurfaceTextureListener(this);
        cameraTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    final Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    final Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                    fadeIn.setDuration(400);
                    fadeOut.setDuration(250);
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            focusMark.setVisibility(View.INVISIBLE);
                            focusMark.setColorFilter(Color.WHITE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            focusMark.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            focusMark.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                            focusMark.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    focusMark.clearAnimation();
                    focusMark.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    focusMark.startAnimation(fadeIn);
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean b, Camera camera) {
                            focusMark.setColorFilter(b ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    focusMark.clearAnimation();
                                    focusMark.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                                    focusMark.startAnimation(fadeOut);
                                }
                            }, 250);
                        }
                    });
                } catch (Exception e) {
                    Log.e("Flashbomb-Log", "catch", e);
                    Crashlytics.logException(e);
                }

            }
        });

        /* Setting proper image margins */
        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) cameraTextureView.getLayoutParams();
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) previewImage1.getLayoutParams();
        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) previewImage2.getLayoutParams();
        RelativeLayout.LayoutParams params4 = (RelativeLayout.LayoutParams) previewVideo.getLayoutParams();
        params1.topMargin = Math.round(0.0222f * size.y);
        params2.topMargin = Math.round(0.0222f * size.y);
        params3.topMargin = Math.round(0.0222f * size.y);
        params4.topMargin = Math.round(0.0222f * size.y);
        cameraTextureView.setLayoutParams(params1);
        previewImage1.setLayoutParams(params2);
        previewImage2.setLayoutParams(params3);
        previewVideo.setLayoutParams(params4);

        /* Showing capture tutorial if needed */
        if (sharedPref.getString(getResources().getString(R.string.PREFERENCES_CAMERA_CAPTURE_TUTORIAL_SHOW), "1").equals("1")) {

            captureTutorial.setVisibility(View.VISIBLE);
            captureTutorial.clearAnimation();
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    captureTutorial.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            captureTutorial.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            captureTutorial.startAnimation(fadeIn);

        }

        /* Getting fade animation */
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                captureAnimation.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /* Setting countdown timer */
        final long total_time = getIntent().getExtras().getLong("total_time", getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS));
        long timeleft = getIntent().getExtras().getLong("timeleft", 0);
        String type = getIntent().getExtras().getString("type", "default");

        if(type.equals("default") || type.equals("fan")) {
            timeleft = (timeleft > 0) ? timeleft : 0;
            timeleft = (timeleft < total_time) ? timeleft : total_time;

            timeleftProgressLayout.setVisibility(View.VISIBLE);
            timeleftTextLayout.setVisibility(View.VISIBLE);
            messageLayout.setVisibility(View.GONE);

            timeleftView.setTypeface(InitApp.headersTypeface);
            timeleftView.setAnimationInterpolator(new OvershootInterpolator());
            timeleftView.setCharacterList(TickerUtils.getDefaultNumberList());

            new CountDownTimer(timeleft * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    timeleftView.setText((millisUntilFinished / 1000) + "");
                    timeleftProgress.setMinimumWidth(Math.round((float) size.x * (float) (total_time - (millisUntilFinished / 1000)) / (float) total_time));
                }

                public void onFinish() {
                    timeleftView.setText("0");
                    timeleftProgress.setMinimumWidth(size.x);
                    captureLayout.setClickable(false);
                    cameraCaptureLayout.setClickable(false);
                    captureAnimation.setClickable(false);
                    captureAnimation.clearAnimation();
                    captureAnimation.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    captureAnimation.startAnimation(fadeOut);
                    capturePossible = false;
                }

            }.start();
        } else {
            timeleftProgressLayout.setVisibility(View.GONE);
            timeleftTextLayout.setVisibility(View.GONE);
            messageLayout.setVisibility(View.VISIBLE);
            messageEditText.clearFocus();
        }

        /* Creating video output file */
        File folder = new File(this.getFilesDir() + "/Flashbomb");
        if (!folder.exists()) {
            folder.mkdir();
        } else if (folder.isDirectory()) {
            String[] files = folder.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    new File(folder.getAbsolutePath() + "/" + files[i]).delete();
                }
            }
        }
        videoOutput = new File(this.getFilesDir() + "/Flashbomb/video.mp4");

        /* Setting camera preview video listener */
        previewVideoListener = new CameraVideoListener(previewVideo, videoOutput, analytics);
        previewVideo.setSurfaceTextureListener(previewVideoListener);

        /* Setting camera preview image listener */
        previewImageListener = new FiltersManagementListener(getApplicationContext(), this, previewImage1, previewImage2, filtersTutorial, analytics);
        previewImage1.setOnTouchListener(previewImageListener);

        /* Capture button listener */
        captureAnimation.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (!captureAnimation.isClickable())
                    return false;

                HandleInteraction(motionEvent.getAction());

                return true;

            }

        });

        /* Setting orientation listener */
        orientationListener = new OrientationEventListener(this) {

            @Override
            public void onOrientationChanged(int orientation) {

                Display mDisplay = getWindowManager().getDefaultDisplay();

                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || mDisplay == null) {
                    return;
                }

                int displayRotation = mDisplay.getRotation();
                int deviceOrientation;
                if (orientation >= 60 && orientation <= 140) {
                    // the mDisplay.getRotation stuff is flipped for 90 & 270 vs. deviceOrientation here. This keeps it consistent.
                    deviceOrientation = 270;
                } else if (orientation >= 140 && orientation <= 220) {
                    deviceOrientation = 180;
                } else if (orientation >= 220 && orientation <= 300) {
                    // the mDisplay.getRotation stuff is flipped for 90 & 270 vs. deviceOrientation here. This keeps it consistent.
                    deviceOrientation = 90;
                } else {
                    deviceOrientation = 0;
                }

                if (camera == null)
                    return;

                try {
                    if (displayRotation != displayRotTmp || deviceOrientation != deviceRotTmp) {

                        displayRotTmp = displayRotation;
                        deviceRotTmp = deviceOrientation;
                        Camera.Parameters params = camera.getParameters();
                        params.setRotation(calculateCaptureRotation(displayRotation, deviceOrientation));
                        camera.setParameters(params);

                        Bundle bundle = new Bundle();
                        bundle.putInt("display_rotation", displayRotation);
                        bundle.putInt("device_rotation", deviceOrientation);
                        bundle.putInt("camera_rotation", calculateCaptureRotation(displayRotation, deviceOrientation));
                        analytics.logEvent("CameraOrientation", bundle);

                    }
                } catch (Exception e) {
                    Log.e("Flashbomb-Log", "catch", e);
                    Crashlytics.logException(e);
                }

            }

        };
        orientationListener.enable();

    }

    /* Help variable to remember time */
    long FingerDownTime = -1;
    /* Method handling touch / volume buttons events */
    private void HandleInteraction(int action) {

        if(!capturePossible)
            return;

        /* Photo/video touch duration difference */
        int touchDurationDiff = 250;

        /* Hiding capture tutorial */
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        if(sharedPref.getString(getResources().getString(R.string.PREFERENCES_CAMERA_CAPTURE_TUTORIAL_SHOW), "1").equals("1")) {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getResources().getString(R.string.PREFERENCES_CAMERA_CAPTURE_TUTORIAL_SHOW), "0");
            editor.apply();
            captureTutorial.clearAnimation();
            Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
                @Override
                public void onAnimationEnd(Animation animation) { captureTutorial.setLayerType(View.LAYER_TYPE_NONE, null); }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            captureTutorial.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            captureTutorial.startAnimation(fadeOut);

        }

        if (action == MotionEvent.ACTION_DOWN) {

            FingerDownTime = System.currentTimeMillis();

        } else if (action == MotionEvent.ACTION_UP) {

            if (System.currentTimeMillis() - FingerDownTime > touchDurationDiff) {
                if (recording) {

                    captureAnimation.setClickable(false);
                    captureAnimation.pauseAnimation();
                    ToggleRecording();

                }
            } else {

                final Animation shutterUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shutter_up);
                shutterUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) { captureAnimation.setLayerType(View.LAYER_TYPE_NONE, null); }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                final Animation shutterDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shutter_down);
                shutterDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        captureAnimation.clearAnimation();
                        captureAnimation.startAnimation(shutterUp);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                captureAnimation.clearAnimation();
                captureAnimation.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                captureAnimation.startAnimation(shutterDown);
                captureAnimation.setClickable(false);
                TakePhoto();

            }
            FingerDownTime = -1;

        } else {

            if (System.currentTimeMillis() - FingerDownTime > touchDurationDiff) {

                if (!recording) {

                    Camera.Parameters params = camera.getParameters();
                    if(params.getSupportedFocusModes() != null && params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                        camera.setParameters(params);
                    }

                    captureAnimation.setProgress(0);
                    captureAnimation.playAnimation();
                    ToggleRecording();

                } else {

                    if(System.currentTimeMillis() - FingerDownTime > captureAnimation.getDuration() + touchDurationDiff) {

                        captureAnimation.setClickable(false);
                        captureAnimation.pauseAnimation();
                        ToggleRecording();

                    }

                }
            }

        }

    }

    /* Handling volume buttons capture */
    int volumeButtonsActionHelper = MotionEvent.ACTION_DOWN;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(!captureAnimation.isClickable() || previewLayout.getVisibility() == View.VISIBLE)
            return false;

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){

            HandleInteraction(volumeButtonsActionHelper);
            volumeButtonsActionHelper = MotionEvent.ACTION_MASK;
            return true;

        }

        return false;

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {

            onBackPressed();
            return false;

        }

        if(!captureAnimation.isClickable() || previewLayout.getVisibility() == View.VISIBLE)
            return false;

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){

            HandleInteraction(MotionEvent.ACTION_UP);
            volumeButtonsActionHelper = MotionEvent.ACTION_DOWN;
            return true;

        }

        return false;

    }

    /* Send button callback */
    public void SendButton(final View v) {

        if (getIntent().getExtras().getString("type", "default").equals("influencer")) {
            // TODO: Handling influencer sending
            FlashbombToast.ShowInfo(this, "Flashtime start with message: " + messageEditText.getText(), 5);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.send_confirmation_title))
                    .setMessage(getString(R.string.send_confirmation_message))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            v.setClickable(false);
                            v.setVisibility(View.GONE);
                            SendEntity();

                        }

                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }

    }

    /* Send entity method */
    private void SendEntity() {

        if (currentUploadType == null)
            return;

        duringUpload = true;

        if (currentUploadType == UploadType.PHOTO) {

            previewImageListener.SendCurrentImage(comment);

        } else if (currentUploadType == UploadType.VIDEO) {

            String savedVideoPath = videoOutput.getAbsolutePath();
            SingletonNetwork.getInstance(getApplicationContext()).UploadVideo(savedVideoPath, comment, new String[]{}, getIntent().getExtras().getString("session_id"));

            /* Session ends for this user */
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1");
            editor.apply();

            CloseActivity();

        }

    }

    /* Photoshoot method */
    public void TakePhoto() {

        final CameraActivity activity = this;

        try {

            camera.takePicture(null, null, new Camera.PictureCallback() {

                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {

                    /* Setting cloudinary infocomment */
                    comment =
                                    "[displayRot = " + displayRotTmp + "]" +
                                    "[deviceRot = " + deviceRotTmp + "]" +
                                    "[cameraRot = " + calculateCaptureRotation(displayRotTmp, deviceRotTmp) + "]" +
                                    "[cameraOrientation = " + cameraOrientationTmp + "]" +
                                    "[cameraId = " + currentCameraId + "]" +
                                    "[cameraFacing = " + currentCameraFacing + "]" +
                                    "[deviceManufacturer = " + android.os.Build.MANUFACTURER + "]" +
                                    "[deviceModel = " + android.os.Build.MODEL + "]";

                    /* Setting preview bitmap and filter switch listener */
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap rotatedBitmap = null;
                    if(bitmap.getWidth() > bitmap.getHeight()) {

                        Matrix matrix = new Matrix();
                        matrix.postRotate((currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) ? 270 : 90);
                        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        bitmap.recycle();
                        previewImageListener.UpdateImage(rotatedBitmap);

                    } else {

                        previewImageListener.UpdateImage(bitmap);

                    }

                    float translationX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()) / 2;
                    countryEmojiImageView.setTranslationX (translationX);
                    countryTextView.setTranslationX (translationX);
                    previewLayout.setVisibility(View.VISIBLE);
                    previewImage1.setVisibility(View.VISIBLE);
                    previewImage2.setVisibility(View.VISIBLE);
                    previewImage1.setClickable(true);
                    previewLayout.bringToFront();
                    previewLayout.clearAnimation();

                    Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }
                        @Override
                        public void onAnimationEnd(Animation animation) { previewLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    previewLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    previewLayout.startAnimation(fadeIn);

                    currentUploadType = UploadType.PHOTO;

                    /* Showing filters tutorial if needed */
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                    if (sharedPref.getString(getResources().getString(R.string.PREFERENCES_CAMERA_FILTERS_SHOW), "1").equals("1")) {

                        filtersTutorial.setVisibility(View.VISIBLE);
                        filtersTutorial.clearAnimation();
                        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        fadeIn.setAnimationListener(new Animation.AnimationListener() {
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
                        filtersTutorial.startAnimation(fadeIn);

                    }

                }

            });

        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    /* Record toggle method */
    public void ToggleRecording() {

        if (recording) {

            /* Stopping record and setting preview */
            try {

                recorder.stop();

            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
                e.printStackTrace();
                PrepareForCapture();
                captureAnimation.setProgress(0);
                currentUploadType = null;
                captureAnimation.setClickable(true);
                cameraCaptureLayout.bringToFront();
                cameraCaptureLayout.clearAnimation();
                Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) { cameraCaptureLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                cameraCaptureLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                cameraCaptureLayout.startAnimation(fadeIn);
                recording = false;
                return;
            }
            camera.stopPreview();
            camera.lock();

            float translationX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()) / 2;
            countryEmojiImageView.setTranslationX (translationX);
            countryTextView.setTranslationX (translationX);
            previewVideo.setVisibility(View.VISIBLE);
            previewLayout.setVisibility(View.VISIBLE);
            previewImage1.setClickable(false);
            previewLayout.bringToFront();
            previewLayout.clearAnimation();
            Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
                @Override
                public void onAnimationEnd(Animation animation) { previewLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            previewLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            previewLayout.startAnimation(fadeIn);
            currentUploadType = UploadType.VIDEO;

            if(previewVideoListener.surfaceAvailable)
                previewVideoListener.PlayVideo();

        } else {

            try {

                /* Setting cloudinary info comment */
                comment =
                        "[displayRot = " + displayRotTmp + "]" +
                                "[deviceRot = " + deviceRotTmp + "]" +
                                "[cameraRot = " + calculateCaptureRotation(displayRotTmp, deviceRotTmp) + "]" +
                                "[cameraOrientation = " + cameraOrientationTmp + "]" +
                                "[cameraId = " + currentCameraId + "]" +
                                "[cameraFacing = " + currentCameraFacing + "]" +
                                "[deviceManufacturer = " + android.os.Build.MANUFACTURER + "]" +
                                "[deviceModel = " + android.os.Build.MODEL + "]";

                /* Starting record */
                camera.unlock();

                recorder.start();

            } catch (Exception e) {

                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
                PrepareForCapture();

            }

        }

        recording = !recording;

    }

    /* Facing toggle method */
    public void ToggleFacing(final View v) {

        if(camera.getNumberOfCameras() > 1) {

            try {
                camera.stopPreview();
                camera.release();
            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }

            if(currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK)
                currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
            else
                currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int camIdx = 0; camIdx < camera.getNumberOfCameras(); camIdx++) {

                Camera.getCameraInfo(camIdx, cameraInfo);

                if (cameraInfo.facing == currentCameraFacing) {
                    try {
                        currentCameraId = camIdx;
                        camera = Camera.open(camIdx);
                    } catch (Exception e) {
                        Log.e("Flashbomb-Log", "catch", e);
                        Crashlytics.logException(e);
                    }
                }

            }

            PrepareForCapture();

            Animation rotateAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_camera_toggle);
            rotateAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setLayerType(View.LAYER_TYPE_NONE, null);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            v.clearAnimation();
            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            v.startAnimation(rotateAnim);

        }

    }

    /* Flash toggle method */
    public void ToggleFlash(View v) {

        Camera.Parameters params = camera.getParameters();

        if(params.getSupportedFlashModes() == null || params.getFlashMode() == null)
            return;

        if(params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
                ((ImageView) v).setImageDrawable(this.getResources().getDrawable(R.drawable.ic_camera_flash));
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            } else if(params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                ((ImageView) v).setImageDrawable(this.getResources().getDrawable(R.drawable.ic_camera_autoflash));
                params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
        } else if(params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_ON)) {
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                ((ImageView) v).setImageDrawable(this.getResources().getDrawable(R.drawable.ic_camera_autoflash));
                params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            } else if(params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_OFF)) {
                ((ImageView) v).setImageDrawable(this.getResources().getDrawable(R.drawable.ic_camera_noflash));
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        } else if(params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_AUTO)) {
            if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_OFF)) {
                ((ImageView) v).setImageDrawable(this.getResources().getDrawable(R.drawable.ic_camera_noflash));
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else if(params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
                ((ImageView) v).setImageDrawable(this.getResources().getDrawable(R.drawable.ic_camera_flash));
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
        }

        camera.setParameters(params);

    }

    /* Preparing recorder */
    private void PrepareForCapture() {

        try {

            /* Video output */
            if(videoOutput.exists())
                videoOutput.delete();
            videoOutput = new File(this.getFilesDir() + "/Flashbomb/video.mp4");

            /* Camcoder profile, quality */
            CamcorderProfile camcoderProfile = CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_HIGH);

            /* Getting camera info */
            android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(currentCameraId, cameraInfo);

            /* Camera params */
            Camera.Parameters p = camera.getParameters();
            p.setPreviewFrameRate(camcoderProfile.videoFrameRate);

            /* Getting best camera sizes */
            Camera.Size[] sizes = getOptimalCameraSizes(p.getSupportedPreviewSizes(), p.getSupportedPictureSizes(), (p.getSupportedVideoSizes() == null ? p.getSupportedPreviewSizes() : p.getSupportedVideoSizes()));

            /* Analytics report event */
            Bundle bundle = new Bundle();
            if(sizes[0] != null)
                bundle.putString("camera_preview_dims", sizes[0].width + " x " + sizes[0].height + " (" + ((float)sizes[0].width / (sizes[0].width + sizes[0].height)) + ")");
            if(sizes[1] != null)
                bundle.putString("camera_picture_dims", sizes[1].width + " x " + sizes[1].height + " (" + ((float)sizes[1].width / (sizes[1].width + sizes[1].height)) + ")");
            if(sizes[2] != null)
                bundle.putString("camera_video_dims", sizes[2].width + " x " + sizes[2].height + " (" + ((float)sizes[2].width / (sizes[2].width + sizes[2].height)) + ")");
            bundle.putString("camera_screen_dims", cameraTextureView.getHeight() + " x " + cameraTextureView.getWidth() + " (" + ((float)cameraTextureView.getHeight() / (cameraTextureView.getWidth() + cameraTextureView.getHeight())) + ")");
            bundle.putInt("camera_orientation", cameraInfo.orientation);
            bundle.putInt("camera_id", currentCameraId);
            bundle.putInt("camera_facing", currentCameraFacing);
            bundle.putInt("camera_picture_sizes_count", p.getSupportedPictureSizes().size());
            bundle.putInt("camera_preview_sizes_count", p.getSupportedPreviewSizes().size());
            bundle.putInt("camera_video_sizes_count", (p.getSupportedVideoSizes() == null ? p.getSupportedPreviewSizes().size() : p.getSupportedVideoSizes().size()));
            analytics.logEvent("CameraSpecification", bundle);

            cameraOrientationTmp = cameraInfo.orientation;

            if(sizes[0] != null)
                p.setPreviewSize(sizes[0].width, sizes[0].height);
            if(sizes[1] != null)
                p.setPictureSize(sizes[1].width, sizes[1].height);
            if(p.getSupportedFocusModes() != null && p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            p.set("orientation", "portrait");
            p.setRotation(cameraInfo.orientation);
            camera.setParameters(p);
            camera.setPreviewTexture(cameraTextureView.getSurfaceTexture());
            camera.setDisplayOrientation(90);
            camera.startPreview();

            /* Recorder params */
            recorder = new MediaRecorder();
            recorder.setPreviewDisplay(new Surface(cameraTextureView.getSurfaceTexture()));
            recorder.setCamera(camera);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOrientationHint(cameraInfo.orientation);
            if(sizes[2] != null) {
                camcoderProfile.videoFrameHeight = sizes[2].height;
                camcoderProfile.videoFrameWidth = sizes[2].width;
            }
            camcoderProfile.videoBitRate = 7500000;
            camcoderProfile.audioBitRate = 512000;
            camcoderProfile.audioChannels = 2;
            camcoderProfile.audioSampleRate = 48000;
            camcoderProfile.audioCodec = MediaRecorder.AudioEncoder.AAC;
            camcoderProfile.videoCodec = MediaRecorder.VideoEncoder.H264;
            camcoderProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
            recorder.setProfile(camcoderProfile);
            recorder.setOutputFile(videoOutput.getAbsolutePath());
            recorder.prepare();

            /* TextureView centercrop */
            if(sizes[0] != null) {
                float previewWidth = sizes[0].width < sizes[0].height ? sizes[0].width : sizes[0].height;
                float previewHeight = sizes[0].width < sizes[0].height ? sizes[0].height : sizes[0].width;
                float previewAspect = previewWidth / (previewHeight + previewWidth);
                float textureAspect = (float) cameraTextureView.getWidth() / ((float) cameraTextureView.getHeight() + (float) cameraTextureView.getWidth());
                float xScale = 1;
                float yScale = 1;
                if (previewAspect > textureAspect) {

                    float yScaleDone = (float) cameraTextureView.getHeight() / previewHeight;
                    float targetVideoWidth = yScaleDone * previewWidth;
                    xScale = targetVideoWidth / (float) cameraTextureView.getWidth();

                } else if (previewAspect < textureAspect) {

                    float xScaleDone = (float) cameraTextureView.getWidth() / previewWidth;
                    float targetVideoHeight = xScaleDone * previewHeight;
                    yScale = targetVideoHeight / (float) cameraTextureView.getHeight();

                }
                Matrix matrix = new Matrix();
                matrix.setScale(xScale, yScale, (float) cameraTextureView.getWidth() / 2, (float) cameraTextureView.getHeight() / 2);
                cameraTextureView.setTransform(matrix);
            }

        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    /* Method returning optimal sizes for camera {preview, picture, video} */
    private Camera.Size[] getOptimalCameraSizes(List<Camera.Size> previewSizes, List<Camera.Size> pictureSizes, List<Camera.Size> videoSizes) {

        /* Variables initializing */
        Camera.Size[] sizes = new Camera.Size[3];
        float maximumWidth = getResources().getInteger(R.integer.PICTURE_VIDEO_WIDTH_MAX);
        float screenAspect = (float) cameraTextureView.getWidth() / (cameraTextureView.getWidth() + cameraTextureView.getHeight());
        float aspectTolerance = 0.1f;

        /* Preview size finding */
        float maxWidth = Float.MIN_VALUE;
        if(previewSizes != null) {
            for (Camera.Size previewSize : previewSizes) {
                float width = (previewSize.width < previewSize.height) ? previewSize.width : previewSize.height;
                float aspect = width / (previewSize.width + previewSize.height);
                if (width <= maximumWidth && Math.abs(aspect - screenAspect) < aspectTolerance && width > maxWidth) {
                    sizes[0] = previewSize;
                    maxWidth = width;
                }
            }
        }

        float previewWidth = sizes[0].width < sizes[0].height ? sizes[0].width : sizes[0].height;
        float previewAspect = previewWidth / (sizes[0].width + sizes[0].height);

        /* Picture size finding */
        maxWidth = Float.MIN_VALUE;
        if(pictureSizes != null) {
            for (Camera.Size pictureSize : pictureSizes) {
                float width = (pictureSize.width < pictureSize.height) ? pictureSize.width : pictureSize.height;
                float aspect = width / (pictureSize.width + pictureSize.height);
                if (width <= maximumWidth && Math.abs(aspect - previewAspect) < aspectTolerance / 10 && width > maxWidth) {
                    sizes[1] = pictureSize;
                    maxWidth = width;
                }
            }
        }

        /* Video size finding */
        maxWidth = Float.MIN_VALUE;
        if(videoSizes != null) {
            for (Camera.Size videoSize : videoSizes) {
                float width = (videoSize.width < videoSize.height) ? videoSize.width : videoSize.height;
                float aspect = width / (videoSize.width + videoSize.height);
                if (width <= maximumWidth && Math.abs(aspect - previewAspect) < (aspectTolerance / 10) && width > maxWidth) {
                    sizes[2] = videoSize;
                    maxWidth = width;
                }
            }
        }

        return sizes;

    }

    /* Method returning proper capture rotation */
    private int calculateCaptureRotation(int mDisplayOrientation, int mDeviceOrientation) {

        Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
        camera.getCameraInfo(currentCameraId, mCameraInfo);

        int captureRotation = 0;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            captureRotation = (mCameraInfo.orientation + mDisplayOrientation) % 360;
        } else {  // back-facing camera
            captureRotation = (mCameraInfo.orientation - mDisplayOrientation + 360) % 360;
        }

        // Accommodate for any extra device rotation relative to fixed screen orientations
        // (e.g. activity fixed in portrait, but user took photo/video in landscape)
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            captureRotation = ((captureRotation - (mDisplayOrientation - mDeviceOrientation)) + 360) % 360;
        } else {  // back-facing camera
            captureRotation = (captureRotation + (mDisplayOrientation - mDeviceOrientation) + 360) % 360;
        }

        if(!android.os.Build.MANUFACTURER.toLowerCase().contains("samsung".toLowerCase())) {
            captureRotation = (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) ? 270 : 90;
        }

        return captureRotation;

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        /* When surface is created, opening camera, preparing capture */
        try {
            camera = Camera.open();
            PrepareForCapture();
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        captureAnimation.setOnTouchListener(null);

        /* Stopping recording and releasing all resources */
        if (recording) {

            if (recorder != null) {
                try {
                    recorder.stop();
                } catch (Exception e) {
                    Log.e("Flashbomb-Log", "catch", e);
                    Crashlytics.logException(e);
                }
                recording = false;
            }

        }

        if (orientationListener != null)
            orientationListener.disable();
        if (recorder != null)
            recorder.release();
        if (camera != null)
            camera.release();

        return true;

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    /* Back button callback */
    long lastBackTime = 0;
    @Override
    public void onBackPressed() {

        if(System.currentTimeMillis() - lastBackTime > 1000) {

            lastBackTime = System.currentTimeMillis();

            if (duringUpload)
                return;

            /* If in preview screen, prepare camera and switch from preview */
            if (previewLayout.getVisibility() == View.VISIBLE) {

                PrepareForCapture();

                captureAnimation.setProgress(0);
                currentUploadType = null;
                previewLayout.setVisibility(View.GONE);
                previewVideo.setVisibility(View.GONE);
                previewVideoListener.Reset();
                previewImageListener.Reset();
                previewImage1.setVisibility(View.GONE);
                previewImage1.setImageBitmap(null);
                previewImage2.setVisibility(View.GONE);
                previewImage2.setImageBitmap(null);
                captureAnimation.setClickable(true);
                cameraCaptureLayout.bringToFront();
                cameraCaptureLayout.clearAnimation();
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) { cameraCaptureLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                cameraCaptureLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                cameraCaptureLayout.startAnimation(fadeIn);

                return;

            }

            CloseActivity();
        }

    }

    /* Close activity method */
    public void CloseActivity() {

         /* If in camera preview, exit this activity */
        final Intent myIntent = new Intent(this, SplashScreenActivity.class);
        myIntent.putExtra("caller", this.getClass().getSimpleName());

        /* Memory clear */
        previewImageListener.Reset();
        previewVideoListener.Reset();

        if(duringUpload) {
            cameraCaptureLayout.clearAnimation();
            cameraCaptureLayout.setVisibility(View.GONE);
            previewLayout.clearAnimation();
            Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
                @Override
                public void onAnimationEnd(Animation animation) { previewLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            previewLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            previewLayout.startAnimation(fadeOut);
        } else {
            previewLayout.clearAnimation();
            previewLayout.setVisibility(View.GONE);
            cameraCaptureLayout.clearAnimation();
            Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
                @Override
                public void onAnimationEnd(Animation animation) { cameraCaptureLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            cameraCaptureLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            cameraCaptureLayout.startAnimation(fadeOut);
        }

        /* Disabling orientation listener */
        orientationListener.disable();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(myIntent);
                finish();

            }
        }, getResources().getInteger(R.integer.FADE_TIME));

    }

    public void ExitButton(View v) {
        onBackPressed();
    }

    @Override
    protected void onResume() {

        super.onResume();

        /* Setting notification handler */
        NotificationProcessingHandler.activity = this;

        /* Setting upload status receiver */
        UploadStateListener.currentActivity = this;

        /* Setting api parser activity */
        ApiParser.getInstance(getApplicationContext()).SetCurrentActivity(this);

        /* Anim override */
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

}
