package com.koszelew.flashbomb.Activities;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ToxicBakery.viewpager.transforms.ScaleInOutTransformer;
import com.airbnb.lottie.LottieAnimationView;
import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Adapters.BestTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.BlacklistListViewAdapter;
import com.koszelew.flashbomb.Adapters.HomeTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.InfluencerGalleryRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsManagementListViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsSearchListViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.ProfileGridViewAdapter;
import com.koszelew.flashbomb.Adapters.SettingsListViewAdapter;
import com.koszelew.flashbomb.Fragments.BlackListFragment;
import com.koszelew.flashbomb.Fragments.ChangePasswordFragment;
import com.koszelew.flashbomb.Fragments.FaqFragment;
import com.koszelew.flashbomb.Fragments.HomeFragment;
import com.koszelew.flashbomb.Fragments.InfluencerGalleryFragment;
import com.koszelew.flashbomb.Fragments.ObservationsManagementFragment;
import com.koszelew.flashbomb.Fragments.ProfileFragment;
import com.koszelew.flashbomb.Fragments.SettingsFragment;
import com.koszelew.flashbomb.Listeners.HomePagerSwitchListener;
import com.koszelew.flashbomb.Listeners.PanoramaGyroscopeListener;
import com.koszelew.flashbomb.Listeners.SearchListener;
import com.koszelew.flashbomb.Listeners.UploadStateListener;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.UIComponents.TypeWriterTextView;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntityLoader;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombInfluencer;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.PhotoFilters;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.SingletonFlashbombEntities;
import com.koszelew.flashbomb.Utils.Networking.ApiParser;
import com.koszelew.flashbomb.Utils.Networking.NotificationProcessingHandler;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.FlashbombToast;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.koszelew.flashbomb.Utils.Other.ServerTime;
import com.onesignal.OneSignal;
import com.squareup.seismic.ShakeDetector;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener {

    /* ViewPagers tabs numbers */
    private final int HOME_PAGER_TABS_NUM = 4;

    /* ViewPager's instances */
    private ViewPager homePager;

    /* Home ViewPager adapter */
    private HomePagerAdapter homePagerAdapter;

    /* Home pager switch listener */
    private HomePagerSwitchListener homePagerSwitchListener;

    /* Search EditText */
    private EditText searchEditText;

    /* Search text changes listener */
    public SearchListener searchTextListener;

    /* Search layout */
    LinearLayout searchListViewLayout;

    /* Fragments */
    private Fragment currentFragment = null;
    private ProfileFragment profileFragment;
    private SettingsFragment settingsFragment;
    private BlackListFragment blacklistFragment;
    private ChangePasswordFragment changePasswordFragment;
    private ObservationsManagementFragment observationsManagementFragment;
    private FaqFragment faqFragment;
    private InfluencerGalleryFragment influencerGalleryFragment;

    /* Core layout */
    private RelativeLayout MainActivityCoreLayout;

    /* Current text to display on tutorial dialog */
    public String currentTutorialPrintText = "";

    /* Tutorial state variables */
    private boolean tutorialOn = false;
    private boolean tutorialClosing = false;

    /* PanoramaGyroscopeListener */
    public PanoramaGyroscopeListener panoramaGyroscopeListener;

    /* Initialization */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        System.gc();

        setContentView(R.layout.activity_main);

        /* Gyro */
        panoramaGyroscopeListener = new PanoramaGyroscopeListener();

        /* Starting tutorial's ShakeDetector */
        ShakeDetector shakeDetector = new ShakeDetector(this);
        shakeDetector.setSensitivity(ShakeDetector.SENSITIVITY_MEDIUM);
        shakeDetector.start((SensorManager) getSystemService(SENSOR_SERVICE));

        /* Clearing app data */
        SingletonFlashbombEntities.getInstance(this).ClearAll();

        /* Core layout */
        MainActivityCoreLayout = findViewById(R.id.mainActivityLayoutCore);

        /* Home ViewPager */
        homePagerAdapter = new HomePagerAdapter(getSupportFragmentManager(), this);
        homePager = findViewById(R.id.homePager);
        homePager.setAdapter(homePagerAdapter);
        homePager.setPageTransformer(true, new ScaleInOutTransformer());
        homePager.setOffscreenPageLimit(3);

        /* Setting switch listener */
        homePagerSwitchListener = new HomePagerSwitchListener(this);
        homePager.addOnPageChangeListener(homePagerSwitchListener);

        /* Setting initial page */
        homePager.setCurrentItem(1);

        /* Setting search view listeners */
        searchEditText = findViewById(R.id.mainActivitySearchInput);
        searchEditText.setTypeface(InitApp.headersTypeface);
        searchTextListener = new SearchListener(this, getApplicationContext());
        searchEditText.addTextChangedListener(searchTextListener);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        findViewById(R.id.touchInterceptor).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                searchEditText.clearFocus();
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                searchEditText.setText("");
                searchTextListener.HideSuggestions();
                return false;

            }
        });

        /* Setting search listview adapter */
        ObservationsSearchListViewAdapter adapter = new ObservationsSearchListViewAdapter(this, SingletonFlashbombEntities.getInstance(this).getSearchNicksSubarray());
        ((ListView) findViewById(R.id.mainActivitySearchListView)).setAdapter(adapter);
        searchListViewLayout = findViewById(R.id.mainActivitySearchListViewLayout);
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                findViewById(R.id.mainActivitySearchIcon).setVisibility(hasFocus ? View.GONE : View.VISIBLE);

                long duration = 400;
                Interpolator interpolator = new FastOutSlowInInterpolator();

                Animation hideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_out_bottom_search);
                hideDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        homePager.setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                hideDown.setInterpolator(interpolator);
                hideDown.setDuration(duration);
                Animation showDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_bottom_search);
                showDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        homePager.setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                showDown.setInterpolator(interpolator);
                showDown.setDuration(duration);
                Animation showUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_top);
                showUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        searchListViewLayout.setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                showUp.setInterpolator(interpolator);
                showUp.setDuration(duration);
                Animation hideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_out_top);
                hideUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        searchListViewLayout.setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                hideUp.setInterpolator(interpolator);
                hideUp.setDuration(duration);

                homePager.setPivotY(homePager.getMeasuredHeight());
                homePager.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                homePager.startAnimation(hasFocus ? hideDown : showDown);
                searchListViewLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                searchListViewLayout.startAnimation(hasFocus ? showUp : hideUp);

            }
        });

        /* Menu fragments */
        InitProfileFragment();
        InitBlacklistFragment();
        InitObservationsFragment();
        InitSettingsFragment();
        InitChangePasswordFragment();
        InitFaqFragment();

    }

    /* OnShake callback*/
    @Override
    public void hearShake() {

        ShowTutorialDialog(false, "");

    }

    /* OnShake tutorial handle method */
    public void ShowTutorialDialog(final boolean initial, final String customText) {

        if (tutorialOn) {
            final SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
            if (!sharedPrefs.getBoolean(getString(R.string.PREFERENCES_SHAKE_TUTORIAL_OPENED), false))
                CloseTutorialDialog();
            return;
        }
        tutorialOn = true;

        final LinearLayout tutorialLayout = findViewById(R.id.mainActivityTutorialLayout);
        final RelativeLayout dialogLayout = findViewById(R.id.mainActivityTutorialDialogLayout);
        final TypeWriterTextView printer = findViewById(R.id.mainActivityTutorialDialogText);
        printer.setTypeface(InitApp.contentsTypeface);
        printer.setCharacterDelay(20);
        printer.setText("");

        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        tutorialLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        tutorialLayout.startAnimation(fadeIn);
        tutorialLayout.setVisibility(View.VISIBLE);
        tutorialLayout.setClickable(true);

        Animation flyIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.tutorial_dialog_in);
        flyIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                printer.animateText(customText.length() == 0 ? (initial ? getString(R.string.tutorial_hello) : currentTutorialPrintText) : customText);
                if (InitApp.isEmulator() || !initial)
                    tutorialLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CloseTutorialDialog();
                        }
                    });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        dialogLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        dialogLayout.startAnimation(flyIn);
        dialogLayout.setVisibility(View.VISIBLE);

    }

    /* Tutorial dialog close method */
    private void CloseTutorialDialog() {

        if (tutorialClosing) {
            return;
        }
        tutorialClosing = true;

        final LinearLayout tutorialLayout = findViewById(R.id.mainActivityTutorialLayout);
        final RelativeLayout dialogLayout = findViewById(R.id.mainActivityTutorialDialogLayout);
        final TypeWriterTextView printer = findViewById(R.id.mainActivityTutorialDialogText);

        final Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        tutorialLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        tutorialLayout.startAnimation(fadeOut);

        Animation flyOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.tutorial_dialog_out);
        flyOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                printer.stopAnimateText();
                tutorialLayout.clearAnimation();
                tutorialLayout.setLayerType(View.LAYER_TYPE_NONE, null);
                tutorialLayout.setVisibility(View.GONE);
                dialogLayout.clearAnimation();
                dialogLayout.setLayerType(View.LAYER_TYPE_NONE, null);
                dialogLayout.setVisibility(View.GONE);

                final SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                sharedPrefs.edit().putBoolean(getString(R.string.PREFERENCES_SHAKE_TUTORIAL_OPENED), true).apply();

                tutorialOn = false;
                tutorialClosing = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        dialogLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        dialogLayout.startAnimation(flyOut);

        tutorialLayout.setOnClickListener(null);
        tutorialLayout.setClickable(false);

    }

    /* Method launching splash animation */
    public void SplashAnimationLaunch() {

        final SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);

        /* Checking if app is actual */
        if (sharedPrefs.getBoolean(getString(R.string.PREFERENCES_APP_OUTDATED), false)) {

            ((TextView) findViewById(R.id.appOutdatedInfoText)).setTypeface(InitApp.contentsTypeface);
            ((TextView) findViewById(R.id.appOutdatedButtonText)).setTypeface(InitApp.headersTypeface);
            findViewById(R.id.appOutdatedScreen).setVisibility(View.VISIBLE);

        }

        /* Checking if account is verified */
        if (!sharedPrefs.getBoolean(getString(R.string.PREFERENCES_ACCOUNT_VERIFIED), false)) {

            ((TextView) findViewById(R.id.accNotVerifiedInfoText)).setText(getString(R.string.account_not_verified_info).replace("{1}", sharedPrefs.getString(getString(R.string.PREFERENCES_EMAIL_KEY), "")));
            ((TextView) findViewById(R.id.accNotVerifiedInfoText)).setTypeface(InitApp.contentsTypeface);
            ((TextView) findViewById(R.id.accNotVerifiedButtonResendText)).setTypeface(InitApp.headersTypeface);
            ((TextView) findViewById(R.id.accNotVerifiedButtonLoginText)).setTypeface(InitApp.headersTypeface);
            findViewById(R.id.accNotVerifiedScreen).setVisibility(View.VISIBLE);

        }

        /* Setting session if active */
        SetSessionActiveState();

        final Context context = this;

        final LottieAnimationView splashOut = findViewById(R.id.mainActivitySplashLottie);
        splashOut.setMinAndMaxProgress(0, 0.75f);
        splashOut.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

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
                                findViewById(R.id.mainActivitySplash).setLayerType(View.LAYER_TYPE_NONE, null);
                                findViewById(R.id.mainActivitySplash).setClickable(false);
                                AddObservationAlert();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        findViewById(R.id.mainActivitySplash).setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        findViewById(R.id.mainActivitySplash).startAnimation(fadeOut);

                    }
                }, 200);

            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_top);
        fadeIn.setDuration(700);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashOut.playAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        splashOut.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        splashOut.setSpeed(-1);
        splashOut.startAnimation(fadeIn);

        /* Checking for initial tutorial dialog */
        if (!sharedPrefs.getBoolean(getString(R.string.PREFERENCES_APP_OUTDATED), false) &&
                sharedPrefs.getBoolean(getString(R.string.PREFERENCES_ACCOUNT_VERIFIED), false) &&
                !sharedPrefs.getBoolean(getString(R.string.PREFERENCES_SHAKE_TUTORIAL_OPENED), false)) {
            ShowTutorialDialog(true, "");
        }

    }

    /* Menu management */
    public void InitSettingsFragment() {
        settingsFragment = new SettingsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.settingsFragmentContainer, settingsFragment);
        transaction.commit();
    }

    public void InitProfileFragment() {
        profileFragment = new ProfileFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.profileFragmentContainer, profileFragment);
        transaction.commit();
    }

    public void InitBlacklistFragment() {
        blacklistFragment = new BlackListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.blacklistFragmentContainer, blacklistFragment);
        transaction.commit();
    }

    public void InitObservationsFragment() {
        observationsManagementFragment = new ObservationsManagementFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.observationsFragmentContainer, observationsManagementFragment);
        transaction.commit();
    }

    public void InitChangePasswordFragment() {
        changePasswordFragment = new ChangePasswordFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.changePasswordFragmentContainer, changePasswordFragment);
        transaction.commit();
    }

    public void InitFaqFragment() {
        faqFragment = new FaqFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.faqFragmentContainer, faqFragment);
        transaction.commit();
    }

    public void InitInfluencerGalleryFragment() {
        influencerGalleryFragment = new InfluencerGalleryFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.influencerGalleryFragmentContainer, influencerGalleryFragment);
        transaction.commit();
    }

    public void OpenTab(final View v) {

        View inView = null, outView = null;
        if (((String) v.getTag()).split("_")[0].equals("settings")) {
            if (((ListView) findViewById(R.id.settingsListView)).getAdapter() != null)
                ((SettingsListViewAdapter) ((ListView) findViewById(R.id.settingsListView)).getAdapter()).notifyDataSetChanged();
            currentFragment = settingsFragment;
            inView = findViewById(R.id.settingsFragmentContainer);
            outView = MainActivityCoreLayout;
            currentTutorialPrintText = getString(R.string.tutorial_settings);
        } else if (((String) v.getTag()).split("_")[0].equals("blacklist")) {
            if(((ListView) findViewById(R.id.blacklistListView)).getAdapter() != null)
                ((BlacklistListViewAdapter) ((ListView) findViewById(R.id.blacklistListView)).getAdapter()).notifyDataSetChanged();
            currentFragment = blacklistFragment;
            inView = findViewById(R.id.blacklistFragmentContainer);
            outView = findViewById(R.id.profileFragmentContainer);
            currentTutorialPrintText = getString(R.string.tutorial_blacklist);
        } else if (((String) v.getTag()).split("_")[0].equals("observations")) {
            if(((ListView) findViewById(R.id.observationsManagementListView)).getAdapter() != null)
                ((ObservationsManagementListViewAdapter) ((ListView) findViewById(R.id.observationsManagementListView)).getAdapter()).notifyDataSetChanged();
            currentFragment = observationsManagementFragment;
            inView = findViewById(R.id.observationsFragmentContainer);
            outView = findViewById(R.id.settingsFragmentContainer);
            currentTutorialPrintText = getString(R.string.tutorial_observations_management);
        } else if (((String) v.getTag()).split("_")[0].equals("changepassword")) {
            currentFragment = changePasswordFragment;
            inView = findViewById(R.id.changePasswordFragmentContainer);
            outView = findViewById(R.id.settingsFragmentContainer);
            currentTutorialPrintText = getString(R.string.tutorial_change_password);
        } else if (((String) v.getTag()).split("_")[0].equals("profile")) {
            if(((GridView) findViewById(R.id.profileGridView)).getAdapter() != null)
                ((ProfileGridViewAdapter) ((GridView) findViewById(R.id.profileGridView)).getAdapter()).notifyDataSetChanged();
            currentFragment = profileFragment;
            inView = findViewById(R.id.profileFragmentContainer);
            outView = MainActivityCoreLayout;
            currentTutorialPrintText = getString(R.string.tutorial_profile);
        } else if (((String) v.getTag()).split("_")[0].equals("faq")) {
            faqFragment.LoadFaq();
            currentFragment = faqFragment;
            inView = findViewById(R.id.faqFragmentContainer);
            outView = findViewById(R.id.settingsFragmentContainer);
            currentTutorialPrintText = getString(R.string.tutorial_faq);
        }

        if (currentFragment == null || inView == null || outView == null)
            return;

        final View finalInView = inView, finalOutView = outView;
        finalInView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        Animation inAnim = AnimationUtils.loadAnimation(this, ((String) v.getTag()).split("_")[1].equals("l") ? R.anim.fly_in_left : R.anim.fly_in_right);
        inAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                finalInView.setVisibility(View.VISIBLE);
                finalInView.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finalOutView.setVisibility(View.GONE);
                finalInView.clearAnimation();
                finalInView.setLayerType(View.LAYER_TYPE_NONE, null);

                if (((String) v.getTag()).split("_")[0].equals("blacklist") && ((ListView) findViewById(R.id.blacklistListView)).getAdapter() == null)
                    SingletonNetwork.getInstance(getApplicationContext()).UpdateBlacklist();
                else if (((String) v.getTag()).split("_")[0].equals("observations") && ((ListView) findViewById(R.id.observationsManagementListView)).getAdapter() == null)
                    SingletonNetwork.getInstance(getApplicationContext()).UpdateObservations();
                else if (((String) v.getTag()).split("_")[0].equals("profile") && ((GridView) findViewById(R.id.profileGridView)).getAdapter() == null)
                    SingletonNetwork.getInstance(getApplicationContext()).UpdateProfileTab();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        inAnim.setInterpolator(new OvershootInterpolator(0.333f));
        inView.startAnimation(inAnim);

    }

    public void OpenInfluencerGallery(final FlashbombInfluencer influencer) {

        boolean updateNeeded = true;
        DiscreteScrollView influencerGalleryList = (DiscreteScrollView) findViewById(R.id.influencerGalleryList);
        if(influencerGalleryList != null) {
            InfluencerGalleryRecyclerViewAdapter adapter = (InfluencerGalleryRecyclerViewAdapter) influencerGalleryList.getAdapter();
            if (adapter != null && !influencer.id.equals(adapter.influencerID)) {
                if (influencerGalleryFragment != null)
                    getSupportFragmentManager().beginTransaction().remove(influencerGalleryFragment).commit();
                InitInfluencerGalleryFragment();
                SingletonFlashbombEntities.getInstance(this).getInfluencerGalleryListViewArray().clear();
            } else {
                if (SingletonFlashbombEntities.getInstance(this).getInfluencerGalleryListViewArray().size() > 0)
                    updateNeeded = false;
            }
        } else {
            InitInfluencerGalleryFragment();
        }

        View inView = null, outView = null;
        currentFragment = influencerGalleryFragment;
        inView = findViewById(R.id.influencerGalleryFragmentContainer);
        outView = MainActivityCoreLayout;
        currentTutorialPrintText = getString(R.string.tutorial_influencer_gallery);

        if (currentFragment == null || inView == null || outView == null)
            return;

        final View finalInView = inView, finalOutView = outView;
        finalInView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        final Animation inAnim = AnimationUtils.loadAnimation(this, R.anim.fly_in_right);
        final boolean finalUpdateNeeded = updateNeeded;

        inAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                finalInView.setVisibility(View.VISIBLE);
                finalInView.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finalOutView.setVisibility(View.GONE);
                finalInView.clearAnimation();
                finalInView.setLayerType(View.LAYER_TYPE_NONE, null);

                if(finalUpdateNeeded)
                    SingletonNetwork.getInstance(getApplicationContext()).UpdateInfluencerGallery(influencer.id);

                ImageView influencerGalleryMainIcon = (ImageView) findViewById(R.id.influencerGalleryMainIcon);
                FlashbombEntityLoader.getInstance(getApplicationContext()).LoadImageFromUrl(influencer.avatar, influencerGalleryMainIcon);

                inAnim.setAnimationListener(null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        inAnim.setInterpolator(new OvershootInterpolator(0.333f));
        inView.startAnimation(inAnim);

    }

    public void back(View v) {

        View outView = null, inView = null;
        Animation outAnim = AnimationUtils.loadAnimation(this, R.anim.fly_out_right);
        if (currentFragment == settingsFragment) {
            currentFragment = null;
            outView = findViewById(R.id.settingsFragmentContainer);
            inView = MainActivityCoreLayout;
            currentTutorialPrintText = getString(R.string.tutorial_home);
        } else if (currentFragment == profileFragment) {
            currentFragment = null;
            outView = findViewById(R.id.profileFragmentContainer);
            inView = MainActivityCoreLayout;
            outAnim = AnimationUtils.loadAnimation(this, R.anim.fly_out_left);
            currentTutorialPrintText = getString(R.string.tutorial_home);
        } else if (currentFragment == blacklistFragment) {
            currentFragment = profileFragment;
            outView = findViewById(R.id.blacklistFragmentContainer);
            inView = findViewById(R.id.profileFragmentContainer);
            outAnim = AnimationUtils.loadAnimation(this, R.anim.fly_out_left);
            currentTutorialPrintText = getString(R.string.tutorial_profile);
        } else if (currentFragment == observationsManagementFragment) {
            currentFragment = settingsFragment;
            outView = findViewById(R.id.observationsFragmentContainer);
            inView = findViewById(R.id.settingsFragmentContainer);
            currentTutorialPrintText = getString(R.string.tutorial_settings);
        } else if (currentFragment == changePasswordFragment) {
            currentFragment = settingsFragment;
            outView = findViewById(R.id.changePasswordFragmentContainer);
            inView = findViewById(R.id.settingsFragmentContainer);
            currentTutorialPrintText = getString(R.string.tutorial_settings);
        } else if (currentFragment == faqFragment) {
            currentFragment = settingsFragment;
            outView = findViewById(R.id.faqFragmentContainer);
            inView = findViewById(R.id.settingsFragmentContainer);
            currentTutorialPrintText = getString(R.string.tutorial_settings);
        } else if (currentFragment == influencerGalleryFragment) {
            currentFragment = null;
            outView = findViewById(R.id.influencerGalleryFragmentContainer);
            inView = MainActivityCoreLayout;
            currentTutorialPrintText = getString(R.string.tutorial_influencers);
        }

        if (outView == null || inView == null)
            return;

        final View finalOutView = outView, finalInView = inView;
        finalOutView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                finalOutView.setClickable(false);
                finalInView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finalOutView.clearAnimation();
                finalOutView.setVisibility(View.GONE);
                finalOutView.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        outAnim.setInterpolator(new AnticipateOvershootInterpolator(0.333f));
        outView.startAnimation(outAnim);

    }

    /* URL observation method */
    public void AddObservationAlert() {

        final SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        final String observeNick = sharedPrefs.getString(getString(R.string.PREFERENCES_URL_OBSERVATION), "");
        if (observeNick.length() > 2) {

            final boolean alreadyObserving = SingletonFlashbombEntities.getInstance(this).getObservationsManagementListViewArray().contains(observeNick);
            if (alreadyObserving)
                sharedPrefs.edit().remove(getString(R.string.PREFERENCES_URL_OBSERVATION)).apply();

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(alreadyObserving ? R.string.dialog_obs_already_observing_title : R.string.dialog_add_obs_title));
            alertDialog.setMessage(Html.fromHtml(getString(alreadyObserving ? R.string.dialog_obs_already_observing_text : R.string.dialog_add_obs_text).replace("{1}", "<b>" + observeNick) + "</b>"));
            alertDialog.setButton(alreadyObserving ? DialogInterface.BUTTON_NEUTRAL : DialogInterface.BUTTON_POSITIVE, getString(alreadyObserving ? R.string.understood : R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!alreadyObserving)
                        SingletonNetwork.getInstance(getApplicationContext()).Observe(observeNick);
                }
            });
            if (!alreadyObserving) {
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPrefs.edit().remove(getString(R.string.PREFERENCES_URL_OBSERVATION)).apply();
                    }
                });
            }
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

    }

    /* Change password method (button callback) */
    public void ChangePassword(View v) {
        changePasswordFragment.ChangePassword();
    }

    /* Open Camera / scroll to top */
    public void HomeMainIconClick(View v) {

        final DiscreteScrollView myScrollView = findViewById(R.id.homePageList);
        if (myScrollView != null)
            myScrollView.smoothScrollToPosition(0);

    }

    /* Best scroll to top */
    public void ObservationsMainIconClick(View v) {

        searchEditText.setText("");
        searchEditText.requestFocus();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);

    }

    /* Best scroll to top */
    public void BestMainIconClick(View v) {

        final DiscreteScrollView myScrollView = findViewById(R.id.bestPageList);
        if (myScrollView != null)
            myScrollView.smoothScrollToPosition(0);

    }

    /* Influencers scroll to top */
    public void InfluencersIconClick(View v) {

        final RecyclerView myScrollView = findViewById(R.id.influencersPageList);
        if(myScrollView != null) {
            myScrollView.smoothScrollToPosition(0);
        }

    }

    /* Influencer gallery scroll to top */
    public void InfluencerGalleryIconClick(View v) {

        final DiscreteScrollView myScrollView = findViewById(R.id.influencerGalleryList);
        if(myScrollView != null) {
            myScrollView.smoothScrollToPosition(0);
        }

    }

    /* Profile scroll to top */
    public void ProfileIconClick(View v) {

        final GridView myGridView = findViewById(R.id.profileGridView);
        if(myGridView != null) {
            myGridView.smoothScrollToPosition(0);
        }

    }

    /* Open GooglePlay app page */
    public void OpenGooglePlayAppPage(View v) {

        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }

    }

    /* Method opening camera */
    public void OpenCamera(final long overrideTimeleft, final long overrideTotalTime, final long overrideSessionId, final String type) {

        if (InitApp.RequestLackingPermissions(this, getResources().getInteger(R.integer.CAMERA_PERMISSIONS_REQUEST_CODE)))
            return;

        if (((SwipeRefreshLayout) findViewById(R.id.bestPageListRefresh)).isRefreshing() ||
                ((SwipeRefreshLayout) findViewById(R.id.homePageListRefresh)).isRefreshing() ||
                ((SwipeRefreshLayout) findViewById(R.id.observationsPageListRefresh)).isRefreshing())
            return;

        final Intent myIntent = new Intent(this, CameraActivity.class);

        Animation splashAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        splashAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                findViewById(R.id.mainActivitySplash).setLayerType(View.LAYER_TYPE_NONE, null);

                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                long session_end = Long.parseLong(sharedPref.getString(getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1"));

                long timeleft = session_end - ServerTime.getCurrentTimestamp();
                timeleft = (timeleft > 0) ? timeleft : 0;
                timeleft = (timeleft < getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS)) ? timeleft : getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS);
                timeleft = overrideTimeleft > 0 ? overrideTimeleft : timeleft;

                long total_time = getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS);
                total_time = overrideTotalTime > 0 ? overrideTotalTime : total_time;

                long session_id = Long.parseLong(sharedPref.getString(getResources().getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), "-1"));
                session_id = overrideSessionId > 0 ? overrideSessionId : session_id;

                myIntent.putExtra("type", type);
                myIntent.putExtra("session_id", Long.toString(session_id));
                myIntent.putExtra("timeleft", timeleft);
                myIntent.putExtra("total_time", total_time);
                startActivity(myIntent);
                System.exit(0);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ((LottieAnimationView)findViewById(R.id.mainActivitySplashLottie)).setProgress(1);
        findViewById(R.id.mainActivitySplash).setVisibility(View.VISIBLE);
        findViewById(R.id.mainActivitySplash).setClickable(true);
        findViewById(R.id.mainActivitySplash).setLayerType(View.LAYER_TYPE_HARDWARE, null);
        findViewById(R.id.mainActivitySplash).startAnimation(splashAnim);

    }

    /* Request permissions result callback */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == getResources().getInteger(R.integer.CAMERA_PERMISSIONS_REQUEST_CODE)) {

            boolean cameraGranted = false, audioGranted = false;

            for (int i = 0; i < permissions.length; i++) {
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

            if (cameraGranted && audioGranted) {
                OpenCamera(-1, -1, -1, "default");
            } else {
                FlashbombToast.ShowError(this, getString(R.string.camera_permissions_fail), 1000);
            }

        } else if (requestCode == getResources().getInteger(R.integer.EXTERNAL_SAVE_PERMISSIONS_REQUEST_CODE)) {

            boolean writeGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        writeGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                        break;
                    default:
                        break;
                }
            }

            if (writeGranted) {
                ((ProfileGridViewAdapter) ((GridView) findViewById(R.id.profileGridView)).getAdapter()).DownloadEntity();
            } else {
                FlashbombToast.ShowError(this, getString(R.string.save_permission_fail), 1000);
            }

        }

    }

    /* VideoPreview listener in HomePagerSwitchListener settings */
    public void SetBestListAdapter(BestTabRecyclerViewAdapter adapter) {

        homePagerSwitchListener.bestListAdapter = adapter;

    }

    public void SetHomeListAdapter(HomeTabRecyclerViewAdapter adapter) {

        homePagerSwitchListener.homeListAdapter = adapter;

    }

    public void SetObservationsListAdapter(ObservationsTabRecyclerViewAdapter adapter) {

        homePagerSwitchListener.observationsListAdapter = adapter;

    }

    /* Setting session active state */
    public void SetSessionActiveState() {

        InitApp.isSessionActive = true;

        PhotoFilters.SetRainbowGradient(findViewById(R.id.mainActivityHomeLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.mainActivityBestLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.mainActivityInfluencersLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.mainActivitySearchInput));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.settingsActivityTopBarLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.blacklistTopBarLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.changePasswordTopBarLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.observationsManagementTopBarLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.profileTopBarLayout));
        PhotoFilters.SetRainbowGradient(findViewById(R.id.faqTopBarLayout));
        RecyclerView homeList = findViewById(R.id.homePageList);
        if (homeList != null) {
            HomeTabRecyclerViewAdapter adapter = (HomeTabRecyclerViewAdapter) homeList.getAdapter();
            if (adapter != null && adapter.getItemCount() > 0)
                adapter.notifyItemChanged(0);
        }

        final SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        long session_end = Long.parseLong(sharedPref.getString(getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1"));
        long timeleft = session_end - ServerTime.getCurrentTimestamp();
        timeleft = (timeleft > 0) ? timeleft : 0;
        timeleft = (timeleft < getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS)) ? timeleft : getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS);

        /* Setting active session state timeout handler */
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                InitApp.isSessionActive = false;

                try {

                    findViewById(R.id.mainActivityHomeLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.mainActivityBestLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.mainActivityInfluencersLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.mainActivitySearchInput).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.settingsActivityTopBarLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.blacklistTopBarLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.changePasswordTopBarLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.observationsManagementTopBarLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.profileTopBarLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    findViewById(R.id.faqTopBarLayout).setBackgroundColor(getResources().getColor(R.color.black));
                    RecyclerView homeList = findViewById(R.id.homePageList);
                    if (homeList != null) {
                        HomeTabRecyclerViewAdapter adapter = (HomeTabRecyclerViewAdapter) homeList.getAdapter();
                        if (adapter != null && adapter.getItemCount() > 0)
                            adapter.notifyItemChanged(0);
                    }

                } catch (Exception e) {
                    Log.e("Flashbomb-Log", "catch", e);
                    Crashlytics.logException(e);
                }

            }
        }, timeleft * 1000);

    }

    /* Methods to notify adapters */
    public void NotifyBestTabAdapter() {

        if (homePagerSwitchListener.bestListAdapter != null)
            homePagerSwitchListener.bestListAdapter.notifyDataSetChanged();

    }

    public void NotifyObservationsTabAdapter() {

        if (homePagerSwitchListener.observationsListAdapter != null)
            homePagerSwitchListener.observationsListAdapter.notifyDataSetChanged();

    }

    public void NotifyHomeTabAdapter() {

        if (homePagerSwitchListener.homeListAdapter != null)
            homePagerSwitchListener.homeListAdapter.notifyDataSetChanged();

    }

    /* Home ViewPager adapter class */
    private class HomePagerAdapter extends FragmentStatePagerAdapter {

        MainActivity activity;

        public HomePagerAdapter(FragmentManager fm, MainActivity activity) {

            super(fm);
            this.activity = activity;

        }

        @Override
        public int getCount() {
            return HOME_PAGER_TABS_NUM;
        }

        @Override
        public Fragment getItem(int position) {

            /* HomeFragment instance */
            HomeFragment fragment = new HomeFragment();

            /* Passing FragmentID parameter */
            Bundle args = new Bundle();
            args.putInt("FragmentID", position);
            fragment.setArguments(args);
            return fragment;

        }

    }

    /* Back button callback */
    @Override
    public void onBackPressed() {

        if(tutorialOn) {
            CloseTutorialDialog();
            return;
        }

        if (currentFragment != null) {
            back(null);
            return;
        }

        switch (homePager.getCurrentItem()) {

            case 0:
            case 2:

                if (searchEditText.hasFocus()) {

                    searchEditText.clearFocus();
                    searchEditText.setText("");
                    searchTextListener.HideSuggestions();

                } else {

                    homePager.setCurrentItem(1, true);

                }

                break;
            case 1:

                 /* App exit */
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                break;
            case 3:

                homePager.setCurrentItem(2, true);

                break;

        }

    }

    /* Logging out */
    public void Logout(View v) {

        /* Unsubscribe notifications */
        OneSignal.setSubscription(false);

        /* Clearing prefs */
        getSharedPreferences(getString(R.string.PREFERENCES_TAG), 0).edit().clear().apply();

        /* Clearing upload photos / videos */
        File dir = new File(this.getFilesDir() + "/Flashbomb");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }

        /* Going to SplashScreen */
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent myIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                startActivity(myIntent);
                System.exit(0);
            }
        }, 200);

    }

    /* Resending activation email */
    public void ActivationEmailResend(View v) {

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String email = sharedPref.getString(getString(R.string.PREFERENCES_EMAIL_KEY), "");

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.activation_resend_title));
        alertDialog.setMessage(getString(R.string.activation_resend_content).replace("{1}", email));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SingletonNetwork.getInstance(getApplicationContext()).ResendActivation();

            }

        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

    @Override
    protected void onResume() {

        super.onResume();

        /* Gyroscope */
        panoramaGyroscopeListener.register(this);

        /* Setting notification handler */
        NotificationProcessingHandler.activity = this;

        /* Setting upload status receiver */
        UploadStateListener.currentActivity = this;

        /* Setting api parser activity */
        ApiParser.getInstance(getApplicationContext()).SetCurrentActivity(this);

        /* Anim override */
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    @Override
    protected void onPause() {
        super.onPause();

        /* Gyroscope */
        panoramaGyroscopeListener.unregister();
    }

}
