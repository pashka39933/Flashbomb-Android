package com.koszelew.flashbomb.Activities;

import android.animation.Animator;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Listeners.UploadStateListener;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntityLoader;
import com.koszelew.flashbomb.Utils.Networking.ApiParser;
import com.koszelew.flashbomb.Utils.Networking.NotificationProcessingHandler;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.FlashbombToast;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.onesignal.OneSignal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

/* Class representing login/register screen (launcher) */
public class LoginActivity extends AppCompatActivity {

    /* Views */
    private LinearLayout loginRegisterLayout;
    private LinearLayout loginLayout;
    private LinearLayout registerLayout;
    private RelativeLayout slideshowLayout;

    /* EditTexts */
    private EditText registerNick;
    private EditText registerEmail;
    private EditText registerBirthyear;
    private EditText registerPassword;
    private EditText registerRePassword;
    private EditText loginNick;
    private EditText loginPassword;

    /* Buttons */
    private TextView firstRegisterButton;
    private TextView secondRegisterButton;
    private TextView firstLoginButton;
    private TextView secondLoginButton;
    private TextView lostPasswordButton;

    /* Progress bars */
    private ProgressBar secondRegisterButtonProgressBar;
    private ProgressBar secondLoginButtonProgressBar;

    /* Slideshow ImageViews */
    private ImageView slideshowImage;

    /* Slideshow continuation help var */
    private boolean slideshowContinue = false;

    /* Loaded pictures */
    private ArrayList<String> resPaths;
    int currentIndex = 0;

    /* Initialization */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Adding hide input listeners */
        SetupUI(findViewById(R.id.activity_login));

        /* OneSignal subscription */
        OneSignal.setSubscription(false);

        /* Splash animation */
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                /* Getting views */
                loginRegisterLayout = findViewById(R.id.loginRegisterLayout);
                loginLayout = findViewById(R.id.loginLayout);
                registerLayout = findViewById(R.id.registerLayout);
                slideshowLayout = findViewById(R.id.loginSlideshow);
                registerNick = findViewById(R.id.registerNick);
                registerNick.setTypeface(InitApp.headersTypeface);
                registerEmail = findViewById(R.id.registerEmail);
                registerEmail.setTypeface(InitApp.headersTypeface);
                registerBirthyear = findViewById(R.id.registerBirthyear);
                registerBirthyear.setTypeface(InitApp.headersTypeface);
                registerPassword = findViewById(R.id.registerPassword);
                registerPassword.setTypeface(InitApp.headersTypeface);
                registerRePassword = findViewById(R.id.registerRePassword);
                registerRePassword.setTypeface(InitApp.headersTypeface);
                loginNick = findViewById(R.id.loginNick);
                loginNick.setTypeface(InitApp.headersTypeface);
                loginPassword = findViewById(R.id.loginPassword);
                loginPassword.setTypeface(InitApp.headersTypeface);
                firstRegisterButton = findViewById(R.id.firstRegisterButton);
                firstRegisterButton.setTypeface(InitApp.headersTypeface);
                secondRegisterButton = findViewById(R.id.secondRegisterButton);
                secondRegisterButton.setTypeface(InitApp.headersTypeface);
                firstLoginButton = findViewById(R.id.firstLoginButton);
                firstLoginButton.setTypeface(InitApp.headersTypeface);
                secondLoginButton = findViewById(R.id.secondLoginButton);
                secondLoginButton.setTypeface(InitApp.headersTypeface);
                lostPasswordButton = findViewById(R.id.lostPasswordButton);
                lostPasswordButton.setTypeface(InitApp.headersTypeface);
                slideshowImage = findViewById(R.id.loginSlideshowImage);
                secondRegisterButtonProgressBar = findViewById(R.id.secondRegisterButtonProgressbar);
                secondLoginButtonProgressBar = findViewById(R.id.secondLoginButtonProgressbar);

                // Adding and shuffling all slideshow images paths from assets
                resPaths = new ArrayList<>(Arrays.asList(loadPicturesPaths()));
                Collections.shuffle(resPaths);
                if(resPaths.size() > 1) {
                    FlashbombEntityLoader.getInstance(getApplicationContext()).LoadImageFromPath(resPaths.get(resPaths.size() - 1), slideshowImage);
                    FlashbombEntityLoader.getInstance(getApplicationContext()).FetchImageFromPath(resPaths.get(0));
                }

                /* Playing animation */
                final LottieAnimationView splashOut = findViewById(R.id.loginActivitySplashLottie);
                splashOut.setMinAndMaxProgress(0, 0.75f);
                splashOut.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {}

                    @Override
                    public void onAnimationEnd(Animator animator) {

                        splashOut.setLayerType(View.LAYER_TYPE_NONE, null);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }
                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        findViewById(R.id.loginActivitySplash).setLayerType(View.LAYER_TYPE_NONE, null);
                                    }
                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                findViewById(R.id.loginActivitySplash).setLayerType(View.LAYER_TYPE_HARDWARE, null);
                                fadeOut.setDuration(3000);
                                findViewById(R.id.loginActivitySplash).startAnimation(fadeOut);

                                /* Launching slideshow */
                                slideshowContinue = true;
                                SlideshowController();

                                fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slideshow_out);
                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }
                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        slideshowLayout.setLayerType(View.LAYER_TYPE_NONE, null);
                                        slideshowContinue = false;
                                    }
                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                slideshowLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                                slideshowLayout.startAnimation(fadeOut);

                            }
                        }, 250);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {}

                    @Override
                    public void onAnimationRepeat(Animator animator) {}
                });
                Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_top);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) { splashOut.playAnimation(); }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fadeIn.setDuration(700);
                splashOut.setSpeed(-1);
                splashOut.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                splashOut.startAnimation(fadeIn);

            }
        }, 500);

    }

    /* Slideshow controlling recurrent function */
    private int interval = 130;
    private void SlideshowController() {

        FlashbombEntityLoader.getInstance(this).LoadImageFromPath(resPaths.get(currentIndex), slideshowImage);
        currentIndex = (currentIndex + 1) % resPaths.size();
        FlashbombEntityLoader.getInstance(this).FetchImageFromPath(resPaths.get(currentIndex));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(slideshowContinue) {
                    interval -= ((interval > 90) ? 2 : 0);
                    SlideshowController();
                }

            }
        }, interval);

    }

    // Method returning paths in folder 'assets/slideshowPics/'
    private String[] loadPicturesPaths() {

        String[] list;

        try {

            list = getAssets().list("slideshowPics");

            for (int i = 0; i < list.length; i++) {

                list[i] = "file:///android_asset/slideshowPics/" + list[i];

            }

            return list;

        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        return new String[0];

    }

    /* Nick in register verification function */
    private boolean VerifyNick() {

        String nick = registerNick.getText().toString();
        String pattern = "^[a-zA-Z0-9._-]{3,18}$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(nick);
        if(nick.length() < 3) {
            FlashbombToast.ShowError(this, getString(R.string.error_short_nick), 1000);
            return false;
        }
        if(!m.matches()) {
            FlashbombToast.ShowError(this, getString(R.string.error_wrong_nick), 1000);
            return false;
        }

        return true;

    }

    /* Email in register verification function */
    private boolean VerifyEmail() {

        String email = registerEmail.getText().toString();
        String pattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(email);
        if(!m.matches()) {
            FlashbombToast.ShowError(this, getString(R.string.error_wrong_email), 1000);
            return false;
        }

        return true;

    }

    /* Password in register verification function */
    private boolean VerifyPassword() {

        String password = registerPassword.getText().toString();
        String rePassword = registerRePassword.getText().toString();
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(password);
        if(password.length() < 5) {
            FlashbombToast.ShowError(this, getString(R.string.error_short_password), 1000);
            return false;
        }
        if(!m.matches()) {
            FlashbombToast.ShowError(this, getString(R.string.error_wrong_password), 1000);
            return false;
        }
        if(!password.equals(rePassword)) {
            FlashbombToast.ShowError(this, getString(R.string.error_not_matching_passwords), 1000);
            return false;
        }

        return true;

    }

    /* Birthyear in register verification function */
    private boolean VerifyBirthyear() {

        final String birthyear = registerBirthyear.getText().toString();
        int birthyearInt = -1;
        try {
            birthyearInt = Integer.parseInt(birthyear);
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            FlashbombToast.ShowError(this, getString(R.string.error_wrong_birthyear), 1000);
            return false;
        }
        if(birthyearInt < 1920 || birthyearInt > Calendar.getInstance().get(Calendar.YEAR) - 3) {
            FlashbombToast.ShowError(this, getString(R.string.error_wrong_birthyear), 1000);
            return false;
        }

        return true;

    }

    /* Back button callback */
    @Override
    public void onBackPressed() {

        ClearAllEditTextsFocus();
        ClearAllEditTextsValues();

        Animation anim;

        if (loginRegisterLayout != null)
            loginRegisterLayout.setVisibility(View.VISIBLE);

        if (loginLayout != null && loginLayout.getVisibility() == View.VISIBLE) {

            anim = AnimationUtils.loadAnimation(this, R.anim.fly_out_left);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    loginLayout.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            anim.setInterpolator(new FastOutSlowInInterpolator());
            loginLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            loginLayout.startAnimation(anim);

        } else if (registerLayout != null && registerLayout.getVisibility() == View.VISIBLE) {

            anim = AnimationUtils.loadAnimation(this, R.anim.fly_out_right);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    registerLayout.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            anim.setInterpolator(new FastOutSlowInInterpolator());
            registerLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            registerLayout.startAnimation(anim);

        } else {

            return;

        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (loginLayout != null)
                    loginLayout.setVisibility(View.GONE);
                if (registerLayout != null)
                    registerLayout.setVisibility(View.GONE);
                if (loginRegisterLayout != null)
                    loginRegisterLayout.bringToFront();

            }
        }, anim.getDuration());

    }

    /* Switch to login button */
    public void SwitchToLoginButton(View v) {

        if(loginLayout == null || loginRegisterLayout == null)
            return;

        Animation inLeft = AnimationUtils.loadAnimation(this, R.anim.fly_in_left);
        inLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) { loginLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        inLeft.setInterpolator(new FastOutSlowInInterpolator());
        loginLayout.setVisibility(View.VISIBLE);
        loginLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        loginLayout.bringToFront();

        loginLayout.startAnimation(inLeft);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                loginRegisterLayout.setVisibility(View.GONE);

            }
        }, inLeft.getDuration());

    }

    /* Switch to register button */
    public void SwitchToRegisterButton(View v) {

        if(registerLayout == null || loginRegisterLayout == null)
            return;

        Animation inRight = AnimationUtils.loadAnimation(this, R.anim.fly_in_right);
        inRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) { registerLayout.setLayerType(View.LAYER_TYPE_NONE, null); }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        inRight.setInterpolator(new FastOutSlowInInterpolator());
        registerLayout.setVisibility(View.VISIBLE);
        registerLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        registerLayout.bringToFront();

        registerLayout.startAnimation(inRight);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                loginRegisterLayout.setVisibility(View.GONE);

            }
        }, inRight.getDuration());

    }

    /* Login button callback */
    public void LoginButton(View v) {

        /* Login values */
        final String nick = loginNick.getText().toString();
        String password = loginPassword.getText().toString();
        for(int i = 0; i < 1000; i++) {
            password = SingletonNetwork.SHA256(password);
        }

        secondLoginButton.setVisibility(View.GONE);
        secondLoginButtonProgressBar.setVisibility(View.VISIBLE);

        SingletonNetwork.getInstance(this).LogIn(nick, password);

    }

    /* Login button callback */
    public void ForgotPasswordButton(View v) {

        /* Login value */
        final String nick = loginNick.getText().toString();

        String nickPattern = "^[a-zA-Z0-9._-]{5,18}$";
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        java.util.regex.Pattern nickPatternCompiled = java.util.regex.Pattern.compile(nickPattern);
        java.util.regex.Pattern emailPatternCompiled = java.util.regex.Pattern.compile(emailPattern);
        java.util.regex.Matcher nickMatcher = nickPatternCompiled.matcher(nick);
        java.util.regex.Matcher emailMatcher = emailPatternCompiled.matcher(nick);

        if(!nickMatcher.matches() && !emailMatcher.matches()) {

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(this.getString(R.string.password_recovery_info_title));
            alertDialog.setMessage(this.getString(R.string.password_recovery_info_content));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, this.getString(R.string.understood),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.yellow_transparent_best));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.red));
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.green));
                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            return;

        }

        if(nickMatcher.matches()) {

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.password_recovery_info_title));
            alertDialog.setMessage(getString(R.string.password_recovery_nick_content).replace("{1}", nick));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        SingletonNetwork.getInstance(getApplicationContext()).ForgotPassword(nick);

                        }

                    });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.yellow_transparent_best));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.red));
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.green));
                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            return;

        }

        if(emailMatcher.matches()) {

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.password_recovery_info_title));
            alertDialog.setMessage(getString(R.string.password_recovery_email_content).replace("{1}", nick));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    SingletonNetwork.getInstance(getApplicationContext()).ForgotPassword(nick);

                }

            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.yellow_transparent_best));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.red));
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.green));
                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            return;

        }

        SingletonNetwork.getInstance(this).ForgotPassword(nick);

    }

    /* Register button callback */
    public void RegisterButton(View v) {

        if (!VerifyNick() || !VerifyEmail() || !VerifyPassword() || !VerifyBirthyear())
            return;

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.register_confirmation_title));
        alertDialog.setMessage(Html.fromHtml("<br>" + getString(R.string.register_confirmation_text).replace("{1}", "<br><br><b><i>" + registerEmail.getText().toString() + "</i></b><br><br><b>") + "</b><br>"));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.register), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                /* Register values */
                final String nick = registerNick.getText().toString();
                String password = registerPassword.getText().toString();
                for (int i = 0; i < 1000; i++) {
                    password = SingletonNetwork.SHA256(password);
                }
                final String email = registerEmail.getText().toString();
                final String birthyear = registerBirthyear.getText().toString();

                secondRegisterButton.setVisibility(View.GONE);
                secondRegisterButtonProgressBar.setVisibility(View.VISIBLE);

                SingletonNetwork.getInstance(getApplicationContext()).Register(nick, password, email, birthyear);

            }

        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.register_correct_email), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.yellow_transparent_best));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.red));
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.green));
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    /* Method handling edittexts behaviour */
    private void ClearAllEditTextsFocus() {

        if(registerNick != null)
            registerNick.clearFocus();
        if(registerEmail != null)
            registerEmail.clearFocus();
        if(registerBirthyear != null)
            registerBirthyear.clearFocus();
        if(registerPassword != null)
            registerPassword.clearFocus();
        if(registerRePassword != null)
            registerRePassword.clearFocus();
        if(loginNick != null)
            loginNick.clearFocus();
        if(loginPassword != null)
            loginPassword.clearFocus();

    }

    /* Method handling edittexts values */
    private void ClearAllEditTextsValues() {

        if(registerNick != null)
            registerNick.setText("");
        if(registerEmail != null)
            registerEmail.setText("");
        if(registerBirthyear != null)
            registerBirthyear.setText("");
        if(registerPassword != null)
            registerPassword.setText("");
        if(registerRePassword != null)
            registerRePassword.setText("");
        if(loginNick != null)
            loginNick.setText("");
        if(loginPassword != null)
            loginPassword.setText("");

    }

    /* Method hiding soft input */
    private void hideSoftKeyboard() {

        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(this.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

    }

    /* Method adding listeners to hide soft keyboard */
    public void SetupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                SetupUI(innerView);
            }
        }
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
