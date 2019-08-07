package com.koszelew.flashbomb.Listeners;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Adapters.BestTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.HomeTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsTabRecyclerViewAdapter;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.SingletonFlashbombEntities;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;

public class HomePagerSwitchListener implements ViewPager.OnPageChangeListener {

    /* MainActivity instance */
    MainActivity activity;

    /* Help variable */
    int currentTabIndex = -1;

    /* Video previews listener on tabs */
    public BestTabRecyclerViewAdapter bestListAdapter;
    public HomeTabRecyclerViewAdapter homeListAdapter;
    public ObservationsTabRecyclerViewAdapter observationsListAdapter;

    /* Tutorial leaflets */
    private ImageView bestLeaflet, obsLeaflet;

    /* MainActivity layout */
    private RelativeLayout mainActivityLayout;

    /* Constructor */
    public HomePagerSwitchListener(MainActivity activity) {

        this.activity = activity;
        this.bestLeaflet = activity.findViewById(R.id.swipeTutorialBest);
        this.obsLeaflet = activity.findViewById(R.id.swipeTutorialObs);
        this.mainActivityLayout = activity.findViewById(R.id.mainActivityLayout);

        final SharedPreferences sharedPrefs = activity.getSharedPreferences(activity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(activity.getString(R.string.PREFERENCES_BEST_LEAFLET_ACTIVE), false)) {
            this.bestLeaflet.setVisibility(View.VISIBLE);
            this.bestLeaflet.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            this.bestLeaflet.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.leaflet_shake_left));
        }

        int obsLeafletCounter = sharedPrefs.getInt(activity.getString(R.string.PREFERENCES_OBS_LEAFLET_ACTIVE), 1);
        if (obsLeafletCounter == 0) {
            this.obsLeaflet.setVisibility(View.VISIBLE);
            this.obsLeaflet.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            this.obsLeaflet.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.leaflet_shake_right));
        } else if (obsLeafletCounter > 0) {
            if (!sharedPrefs.getBoolean(activity.getString(R.string.PREFERENCES_APP_OUTDATED), false) &&
                    sharedPrefs.getBoolean(activity.getString(R.string.PREFERENCES_ACCOUNT_VERIFIED), false) &&
                    sharedPrefs.getBoolean(activity.getString(R.string.PREFERENCES_SHAKE_TUTORIAL_OPENED), false)) {
                sharedPrefs.edit().putInt(activity.getString(R.string.PREFERENCES_OBS_LEAFLET_ACTIVE), --obsLeafletCounter).apply();
            }
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        /* Tutorial leaflets */
        if(this.bestLeaflet.getVisibility() == View.VISIBLE || this.obsLeaflet.getVisibility() == View.VISIBLE) {
            if (position == 0) {
                this.bestLeaflet.setTranslationX(InitApp.screenSize.x - positionOffsetPixels);
                this.bestLeaflet.setAlpha(positionOffset);
                this.obsLeaflet.setTranslationX(InitApp.screenSize.x - positionOffsetPixels - 1);
            } else if (position == 1) {
                this.bestLeaflet.setTranslationX(-positionOffsetPixels);
                this.obsLeaflet.setTranslationX(-positionOffsetPixels - 1);
                this.obsLeaflet.setAlpha(1f - positionOffset);
            }
        }

        /* Influencers page background color switch */
        if(position == 1) {
            this.mainActivityLayout.setBackgroundColor(Color.rgb(255, 255, 255));
        } else if(position == 2) {
            int colorPart = 255 - Math.round (((float)positionOffsetPixels / (float)InitApp.screenSize.x) * 255f);
            this.mainActivityLayout.setBackgroundColor(Color.rgb(colorPart, colorPart, colorPart));
        } else if(position == 3) {
            this.mainActivityLayout.setBackgroundColor(Color.rgb(0, 0, 0));
        }

    }

    @Override
    public void onPageSelected(int position) {

        /* Updating tabs on page switch */
        switch (position) {

            case 0:

                /* Best Tab */
                if (SingletonFlashbombEntities.getInstance(activity).getBestTabListViewArray().size() == 0) {

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SingletonNetwork.getInstance(activity).UpdateBestTab();
                            final SharedPreferences sharedPrefs = activity.getSharedPreferences(activity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                            sharedPrefs.edit().putBoolean(activity.getString(R.string.PREFERENCES_BEST_LEAFLET_ACTIVE), false).apply();
                            bestLeaflet.setVisibility(View.GONE);
                            bestLeaflet.clearAnimation();
                            bestLeaflet.setLayerType(View.LAYER_TYPE_NONE, null);

                            if (!sharedPrefs.getBoolean(activity.getString(R.string.PREFERENCES_BEST_TAB_SEEN), false)) {
                                activity.hearShake();
                                sharedPrefs.edit().putBoolean(activity.getString(R.string.PREFERENCES_BEST_TAB_SEEN), true).apply();
                            }
                        }
                    }, 300);

                }

                if (bestListAdapter != null) {
                    bestListAdapter.MuteMediaPlayer();
                    bestListAdapter.mediaPlayer.start();
                }
                if (homeListAdapter != null) {
                    homeListAdapter.mediaPlayer.pause();
                }
                if (observationsListAdapter != null) {
                    observationsListAdapter.mediaPlayer.pause();
                }

                activity.currentTutorialPrintText = activity.getString(R.string.tutorial_best);

                break;

            case 1:

                /* Home Tab */
                if (SingletonFlashbombEntities.getInstance(activity).getHomeTabListViewArray().size() == 0) {

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SingletonNetwork.getInstance(activity).UpdateHomeTab();
                        }
                    }, 600);

                }

                if (bestListAdapter != null) {
                    bestListAdapter.mediaPlayer.pause();
                }
                if (homeListAdapter != null) {
                    homeListAdapter.MuteMediaPlayer();
                    homeListAdapter.mediaPlayer.start();
                }
                if (observationsListAdapter != null) {
                    observationsListAdapter.mediaPlayer.pause();
                }

                activity.currentTutorialPrintText = activity.getString(R.string.tutorial_home);

                break;

            case 2:

                /* Observations Tab */
                if (SingletonFlashbombEntities.getInstance(activity).getObservationsTabListViewArray().size() == 0) {

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SingletonNetwork.getInstance(activity).UpdateObservationsTab();
                            final SharedPreferences sharedPrefs = activity.getSharedPreferences(activity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                            if (sharedPrefs.getInt(activity.getString(R.string.PREFERENCES_OBS_LEAFLET_ACTIVE), 1) >= 0) {
                                activity.hearShake();
                                sharedPrefs.edit().putInt(activity.getString(R.string.PREFERENCES_OBS_LEAFLET_ACTIVE), -1).apply();
                                obsLeaflet.setVisibility(View.GONE);
                                obsLeaflet.clearAnimation();
                                obsLeaflet.setLayerType(View.LAYER_TYPE_NONE, null);
                            }
                        }
                    }, 300);

                }

                if (bestListAdapter != null) {
                    bestListAdapter.mediaPlayer.pause();
                }
                if (homeListAdapter != null) {
                    homeListAdapter.mediaPlayer.pause();
                }
                if (observationsListAdapter != null) {
                    observationsListAdapter.MuteMediaPlayer();
                    observationsListAdapter.mediaPlayer.start();
                }

                activity.currentTutorialPrintText = activity.getString(R.string.tutorial_observations);

                break;

            case 3:

                /* Influencers Tab */
                if (SingletonFlashbombEntities.getInstance(activity).getInfluencersTabListViewArray().size() == 0) {

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SingletonNetwork.getInstance(activity).UpdateInfluencersTab();
                        }
                    }, 300);

                }

                if (bestListAdapter != null) {
                    bestListAdapter.mediaPlayer.pause();
                }
                if (homeListAdapter != null) {
                    homeListAdapter.mediaPlayer.pause();
                }
                if (observationsListAdapter != null) {
                    observationsListAdapter.mediaPlayer.pause();
                }

                activity.currentTutorialPrintText = activity.getString(R.string.tutorial_influencers);

                break;

        }

        /* Toggling current top bar */
        ToggleTopBar(currentTabIndex, position);

        /* Setting current tab index */
        currentTabIndex = position;

    }

    @Override
    public void onPageScrollStateChanged(int state) {}


    /* Toggling top bars */
    public void ToggleTopBar(int outIndex, int inIndex) {

        /* Returning if wrong index */
        if(outIndex < 0 || inIndex < 0)
            return;

        /* Getting views */
        ViewGroup tmpOutView = null;

        switch (outIndex) {

            case 0:
                tmpOutView = (LinearLayout) activity.findViewById(R.id.mainActivityBestLayout);
                break;
            case 1:
                tmpOutView = (LinearLayout) activity.findViewById(R.id.mainActivityHomeLayout);
                break;
            case 2:
                tmpOutView = (LinearLayout) activity.findViewById(R.id.mainActivitySearchLayout);
                break;
            case 3:
                tmpOutView = (LinearLayout) activity.findViewById(R.id.mainActivityInfluencersLayout);
                break;

        }

        ViewGroup tmpInView = null;

        switch (inIndex) {

            case 0:
                tmpInView = (LinearLayout) activity.findViewById(R.id.mainActivityBestLayout);
                break;
            case 1:
                tmpInView = (LinearLayout) activity.findViewById(R.id.mainActivityHomeLayout);
                break;
            case 2:
                tmpInView = (LinearLayout) activity.findViewById(R.id.mainActivitySearchLayout);
                break;
            case 3:
                tmpInView = (LinearLayout) activity.findViewById(R.id.mainActivityInfluencersLayout);
                break;

        }

        final ViewGroup outView = tmpOutView;
        final ViewGroup inView = tmpInView;

        outView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        inView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        /* Animations */
        Animation flyIn = AnimationUtils.loadAnimation(activity, R.anim.fly_in_top);
        Animation flyOut = AnimationUtils.loadAnimation(activity, R.anim.fly_out_top);

        flyIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

                inView.setVisibility(View.VISIBLE);

                for (int i = 0; i < inView.getChildCount(); i++) {
                    if(inView.getChildAt(i).getTag() == null || !inView.getChildAt(i).getTag().equals("mainActivitySearchListViewLayout"))
                        inView.getChildAt(i).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                inView.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

        });

        flyOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {

                outView.setLayerType(View.LAYER_TYPE_NONE, null);
                outView.setVisibility(View.INVISIBLE);

                for (int i = 0; i < outView.getChildCount(); i++) {
                    outView.getChildAt(i).setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

        });

        outView.startAnimation(flyOut);
        inView.startAnimation(flyIn);

    }

}
