package com.koszelew.flashbomb.Utils.Networking;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Activities.CameraActivity;
import com.koszelew.flashbomb.Activities.LoginActivity;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Activities.SplashScreenActivity;
import com.koszelew.flashbomb.Adapters.BestTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.BlacklistListViewAdapter;
import com.koszelew.flashbomb.Adapters.HomeTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.InfluencerGalleryRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.InfluencersTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsManagementListViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsSearchListViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.ProfileGridViewAdapter;
import com.koszelew.flashbomb.Adapters.SettingsListViewAdapter;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntity;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombInfluencer;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.SingletonFlashbombEntities;
import com.koszelew.flashbomb.Utils.Other.FlashbombToast;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.koszelew.flashbomb.Utils.Other.ServerTime;
import com.onesignal.OneSignal;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import net.gotev.uploadservice.UploadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ApiParser {

    /* Class instance */
    private static ApiParser mInstance;
    /* Context variable */
    private static Context mCtx;

    /* Current activity instance */
    private Activity currentActivity;

    /* Cached responses */
    private String loginRegisterCachedResponse;
    private String bestTabCachedResponse;
    private String homeTabCachedResponse;
    private String observationsTabCachedResponse;
    private String getInfluencersCachedResponse;
    private String profileTabCachedResponse;
    private String getObservationsCachedResponse;
    private String getBlacklistCachedResponse;

    /* Lists refresh swipe layouts */
    private SwipeRefreshLayout HomeTabRefreshLayout;
    private SwipeRefreshLayout ObservationsTabRefreshLayout;
    private SwipeRefreshLayout InfluencersTabRefreshLayout;
    private SwipeRefreshLayout BestTabRefreshLayout;
    private SwipeRefreshLayout ProfileTabRefreshLayout;
    private SwipeRefreshLayout BlacklistTabRefreshLayout;
    private SwipeRefreshLayout InfluencerGalleryRefreshLayout;

    /* Flags set to true during update of each tab */
    public static boolean HomeTabUpdating = false;
    public static boolean ObservationsTabUpdating = false;
    public static boolean InfluencersUpdating = false;
    public static boolean BestTabUpdating = false;
    public static boolean ProfileTabUpdating = false;
    public static boolean BlacklistUpdating = false;
    public static boolean InfluencerGalleryUpdating = false;

    /* Constructor */
    private ApiParser(Context context) {
        mCtx = context;
    }

    /* GetInstance method */
    public static synchronized ApiParser getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ApiParser(context);
        }
        return mInstance;
    }


    /* Constructor */
    public void SetCurrentActivity(Activity currentActivity) {

        /* Setting current activity */
        this.currentActivity = currentActivity;

        /* Getting cached responses */
        final SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        loginRegisterCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_LOGIN_REGISTER_CACHE), "");
        homeTabCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_HOME_TAB_CACHE), "");
        observationsTabCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_OBSERVATIONS_TAB_CACHE), "");
        getInfluencersCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_GET_INFLUENCERS_CACHE), "");
        profileTabCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_PROFILE_TAB_CACHE), "");
        bestTabCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_BEST_TAB_CACHE), "");
        getObservationsCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_GET_OBSERVATIONS_CACHE), "");
        getBlacklistCachedResponse = sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_GET_BLACKLIST_CACHE), "");

    }

    /* Core, entry methods */
    public void HandleResponse(ResponseType type, JSONObject json, boolean cached) {

        /* Log */
        Log.d("Flashbomb-Log", "[" + this.getClass().getSimpleName() + "] " + "HandleResponse: " + json.toString());

        /* Getting prefs to save cached responses */
        SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        /* Parsing response according to its type */
        try {
            switch (type) {
                case LOGIN:
                    editor.putString(currentActivity.getString(R.string.PREFERENCES_LOGIN_REGISTER_CACHE), json.toString());
                    ParseLoginRegister(json, cached, false);
                    break;
                case REGISTER:
                    editor.putString(currentActivity.getString(R.string.PREFERENCES_LOGIN_REGISTER_CACHE), json.toString());
                    ParseLoginRegister(json, cached, true);
                    break;
                case BEST_TAB:
                    if(SingletonFlashbombEntities.getInstance(currentActivity).getBestTabListViewArray().size() == 0)
                        editor.putString(currentActivity.getString(R.string.PREFERENCES_BEST_TAB_CACHE), json.toString());
                    ParseBestTab(json);
                    break;
                case HOME_TAB:
                    if(SingletonFlashbombEntities.getInstance(currentActivity).getHomeTabListViewArray().size() == 0)
                        editor.putString(currentActivity.getString(R.string.PREFERENCES_HOME_TAB_CACHE), json.toString());
                    ParseHomeTab(json);
                    break;
                case OBSERVATIONS_TAB:
                    if(SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray().size() == 0)
                        editor.putString(currentActivity.getString(R.string.PREFERENCES_OBSERVATIONS_TAB_CACHE), json.toString());
                    ParseObservationsTab(json);
                    break;
                case INFLUENCERS_TAB:
                    editor.putString(currentActivity.getString(R.string.PREFERENCES_GET_INFLUENCERS_CACHE), json.toString());
                    ParseInfluencersTab(json);
                    break;
                case PROFILE_TAB:
                    if(SingletonFlashbombEntities.getInstance(currentActivity).getProfileTabListViewArray().size() == 0)
                        editor.putString(currentActivity.getString(R.string.PREFERENCES_PROFILE_TAB_CACHE), json.toString());
                    ParseProfileTab(json);
                    break;
                case GET_OBSERVATIONS:
                    editor.putString(currentActivity.getString(R.string.PREFERENCES_GET_OBSERVATIONS_CACHE), json.toString());
                    ParseObservations(json);
                    break;
                case GET_BLACKLIST:
                    editor.putString(currentActivity.getString(R.string.PREFERENCES_GET_BLACKLIST_CACHE), json.toString());
                    ParseBlacklist(json);
                    break;
                case OBSERVE:
                    ParseObserve(json);
                    break;
                case UNOBSERVE:
                    ParseUnobserve(json);
                    break;
                case JOIN_INFLUENCER:
                case LEAVE_INFLUENCER:
                    ParseJoinLeaveInfluencer(json);
                    break;
                case INFLUENCER_GALLERY:
                    ParseInfluencerGallery(json);
                    break;
                case SEARCH_OBSERVATIONS:
                    ParseSearchObservations(json);
                    break;
                case REPORT:
                    ParseReport(json);
                    break;
                case FORGOT_PASSWORD:
                    ParseForgotPassword(json);
                    break;
                case RESEND_ACTIVATION:
                    ParseResendActivation(json);
                    break;
                case CHANGE_PASSWORD:
                    ParseChangePassword(json);
                    break;
                case SET_ANONYMOUS:
                    ParseAnonymous(json);
                    break;
                case SET_NON_ANONYMOUS:
                    ParseNonAnonymous(json);
                    break;
                case HIDE:
                    ParseHide(json);
                    break;
                case UNHIDE:
                    ParseUnhide(json);
                    break;
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }

        editor.apply();

    }

    public void HandleError(ResponseType type, VolleyError error) {

        /* Log */
        Log.d("Flashbomb-Log", "[" + this.getClass().getSimpleName() + "] " + "HandleError: " + (error.networkResponse == null ? error.getLocalizedMessage() : "[" + error.networkResponse.statusCode + "] " + new String(error.networkResponse.data)));

        /* Authentication error logout */
        if (currentActivity.getClass() != LoginActivity.class && error.networkResponse != null && error.networkResponse.statusCode == 401) {

            /* Clearing prefs */
            SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();

            /* Clearing upload photos / videos */
            File dir = new File(currentActivity.getFilesDir() + "/Flashbomb");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }

            /* Launching login */
            Intent myIntent = new Intent(currentActivity, SplashScreenActivity.class);
            currentActivity.startActivity(myIntent);
            System.exit(0);
            return;

        }

        /* Setting layouts */
        switch (type) {

            case BEST_TAB:
                BestTabRefreshLayout = (SwipeRefreshLayout) currentActivity.findViewById(R.id.bestPageListRefresh);
                if(BestTabRefreshLayout != null)
                    BestTabRefreshLayout.setRefreshing(false);
                BestTabUpdating = false;
                break;
            case HOME_TAB:
                HomeTabRefreshLayout = (SwipeRefreshLayout) currentActivity.findViewById(R.id.homePageListRefresh);
                if(HomeTabRefreshLayout != null)
                    HomeTabRefreshLayout.setRefreshing(false);
                HomeTabUpdating = false;
                break;
            case OBSERVATIONS_TAB:
                ObservationsTabRefreshLayout = (SwipeRefreshLayout) currentActivity.findViewById(R.id.observationsPageListRefresh);
                if(ObservationsTabRefreshLayout != null)
                    ObservationsTabRefreshLayout.setRefreshing(false);
                ObservationsTabUpdating = false;
                break;
            case INFLUENCERS_TAB:
                InfluencersTabRefreshLayout = (SwipeRefreshLayout) currentActivity.findViewById(R.id.influencersPageListRefresh);
                if(InfluencersTabRefreshLayout != null)
                    InfluencersTabRefreshLayout.setRefreshing(false);
                InfluencersUpdating = false;
                break;
            case PROFILE_TAB:
                ProfileTabRefreshLayout = (SwipeRefreshLayout) currentActivity.findViewById(R.id.profileGridViewRefresh);
                if(ProfileTabRefreshLayout != null)
                    ProfileTabRefreshLayout.setRefreshing(false);
                ProfileTabUpdating = false;
                break;
            case GET_BLACKLIST:
                BlacklistTabRefreshLayout = (SwipeRefreshLayout) currentActivity.findViewById(R.id.blacklistListViewRefresh);
                if(BlacklistTabRefreshLayout != null)
                    BlacklistTabRefreshLayout.setRefreshing(false);
                BlacklistUpdating = false;
                break;
            case INFLUENCER_GALLERY:
                InfluencerGalleryRefreshLayout = (SwipeRefreshLayout) currentActivity.findViewById(R.id.influencerGalleryListRefresh);
                if(InfluencerGalleryRefreshLayout != null)
                    InfluencerGalleryRefreshLayout.setRefreshing(false);
                InfluencerGalleryUpdating = false;
                break;
            case LOGIN:
                if(currentActivity.getClass() == LoginActivity.class) {
                    currentActivity.findViewById(R.id.secondLoginButtonProgressbar).setVisibility(View.GONE);
                    currentActivity.findViewById(R.id.secondLoginButton).setVisibility(View.VISIBLE);
                }
                break;
            case REGISTER:
                if(currentActivity.getClass() == LoginActivity.class) {
                    currentActivity.findViewById(R.id.secondRegisterButtonProgressbar).setVisibility(View.GONE);
                    currentActivity.findViewById(R.id.secondRegisterButton).setVisibility(View.VISIBLE);
                }
                break;

        }

        /* Error Toast display */
        String infoText = "";

        if (error.networkResponse == null) {

            infoText = currentActivity.getString(R.string.no_internet_info);

            /* Trying to get cached response */
            try {

                String cachedResponse = "";
                switch (type) {

                    case LOGIN:
                    case REGISTER:
                        cachedResponse = loginRegisterCachedResponse;
                        break;
                    case HOME_TAB:
                        cachedResponse = SingletonFlashbombEntities.getInstance(currentActivity).getHomeTabListViewArray().size() > 0 ? "" : homeTabCachedResponse;
                        break;
                    case OBSERVATIONS_TAB:
                        cachedResponse = SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray().size() > 0 ? "" : observationsTabCachedResponse;
                        break;
                    case INFLUENCERS_TAB:
                        cachedResponse = SingletonFlashbombEntities.getInstance(currentActivity).getInfluencersTabListViewArray().size() > 0 ? "" : getInfluencersCachedResponse;
                        break;
                    case PROFILE_TAB:
                        cachedResponse = SingletonFlashbombEntities.getInstance(currentActivity).getProfileTabListViewArray().size() > 0 ? "" : profileTabCachedResponse;
                        break;
                    case BEST_TAB:
                        cachedResponse = SingletonFlashbombEntities.getInstance(currentActivity).getBestTabListViewArray().size() > 0 ? "" : bestTabCachedResponse;
                        break;
                    case GET_OBSERVATIONS:
                        cachedResponse = getObservationsCachedResponse;
                        break;
                    case GET_BLACKLIST:
                        cachedResponse = getBlacklistCachedResponse;
                        break;

                }
                if(cachedResponse.length() > 0)
                    HandleResponse(type, new JSONObject(cachedResponse), true);

            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }

        } else {

            int responseCode = -1;
            try {
                responseCode = new JSONObject(new String(error.networkResponse.data)).optInt("status_code", error.networkResponse.statusCode);
            } catch (JSONException e) {
                Crashlytics.logException(e);
            }

            switch (responseCode) {

                case 400:
                    infoText = currentActivity.getResources().getString(R.string.error_bad_request);
                    break;
                case 401:
                    infoText = currentActivity.getResources().getString(R.string.error_unauthorized);
                    break;
                case 4001:
                    infoText = currentActivity.getResources().getString(R.string.error_credentials);
                    break;
                case 4002:
                    infoText = currentActivity.getResources().getString(R.string.error_nick_taken);
                    break;
                case 4003:
                    infoText = currentActivity.getResources().getString(R.string.error_email_taken);
                    break;
                case 4004:
                    infoText = currentActivity.getResources().getString(R.string.error_short_nick);
                    break;
                case 4005:
                    infoText = currentActivity.getResources().getString(R.string.error_short_password);
                    break;
                case 4006:
                    infoText = currentActivity.getResources().getString(R.string.error_wrong_email);
                    break;
                case 4007:
                    infoText = currentActivity.getResources().getString(R.string.error_wrong_birthyear);
                    break;
                case 4010:
                    infoText = currentActivity.getResources().getString(R.string.error_user_not_found);
                    break;
                case 4011:
                    infoText = currentActivity.getResources().getString(R.string.error_multiple_report);
                    break;
                case 500:
                    infoText = currentActivity.getResources().getString(R.string.error_server);
                    break;
                case 5001:
                    infoText = currentActivity.getResources().getString(R.string.server_activation_mail_fail);
                    break;
                case 5002:
                    infoText = currentActivity.getResources().getString(R.string.server_recovery_mail_fail);
                    break;
                case 5003:
                    infoText = currentActivity.getResources().getString(R.string.error_server);
                    break;
                case 5004:
                    infoText = currentActivity.getResources().getString(R.string.error_unlike);
                    break;
                case 5005:
                    infoText = currentActivity.getResources().getString(R.string.error_like);
                    break;
                case 5006:
                    infoText = currentActivity.getResources().getString(R.string.error_register);
                    break;
                case 5007:
                    infoText = currentActivity.getResources().getString(R.string.error_server);
                    break;
                case 5008:
                    infoText = currentActivity.getResources().getString(R.string.error_observe);
                    break;
                case 5009:
                    infoText = currentActivity.getResources().getString(R.string.error_unobserve);
                    break;
                case 5010:
                    infoText = currentActivity.getResources().getString(R.string.error_no_profile);
                    break;
                case 5011:
                    infoText = currentActivity.getResources().getString(R.string.error_change_password);
                    break;
                case 5012:
                    infoText = currentActivity.getResources().getString(R.string.error_report);
                    break;
                case 5013:
                    infoText = currentActivity.getResources().getString(R.string.error_reveal);
                    break;
                case 5014:
                    infoText = currentActivity.getResources().getString(R.string.error_hide);
                    break;
                case 5015:
                    infoText = currentActivity.getResources().getString(R.string.error_unhide);
                    break;
                case 5016:
                    infoText = currentActivity.getResources().getString(R.string.error_checksession);
                    break;
                case 5017:
                    infoText = currentActivity.getResources().getString(R.string.error_best_anonymous);
                    break;
                case 5018:
                    infoText = currentActivity.getResources().getString(R.string.error_best_non_anonymous);
                    break;
                case 5019:
                    infoText = currentActivity.getResources().getString(R.string.error_add_fan);
                    break;
                case 5020:
                    infoText = currentActivity.getResources().getString(R.string.error_remove_fan);
                    break;
                case 5021:
                    infoText = currentActivity.getResources().getString(R.string.error_get_influencer_gallery_no_sessions);
                    break;
                case 5022:
                    infoText = currentActivity.getResources().getString(R.string.error_get_influencer_gallery_not_member);
                    break;
                default:
                    infoText = currentActivity.getResources().getString(R.string.error_unknown);
                    break;
            }

        }

        if(infoText.length() > 0) {
            FlashbombToast.ShowError(currentActivity, infoText, 1250);
        }
    }

    /* Parser methods */
    private void ParseHomeTab(JSONObject json) throws JSONException {

        if (currentActivity.getClass() != MainActivity.class)
            return;

        /* Getting pics from JSON */
        JSONArray pics = json.getJSONArray("pictures");

        /* Getting shared prefs */
        final SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);

        if (SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).getHomeTabListViewArray().size() == 0) {

            FlashbombEntity title = new FlashbombEntity(json, ResponseType.HOME_TAB, true);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(title);

        }

        /* Adding pictures from JSON */
        for (int i = 0; i < pics.length(); i++) {
            FlashbombEntity newPic = new FlashbombEntity(pics.getJSONObject(i), ResponseType.HOME_TAB, false);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(newPic);
        }

        /* Setting adapter to apropriate ListView */
        final DiscreteScrollView myScrollView = currentActivity.findViewById(R.id.homePageList);

        if (myScrollView != null) {
            myScrollView.setOffscreenItems(0);

            if (myScrollView.getAdapter() == null) {

                /* Pull to refresh listener */
                HomeTabRefreshLayout = currentActivity.findViewById(R.id.homePageListRefresh);
                HomeTabRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (!HomeTabUpdating) {
                            SingletonFlashbombEntities.getInstance(currentActivity).getHomeTabListViewArray().clear();
                            myScrollView.getAdapter().notifyDataSetChanged();
                            ((HomeTabRecyclerViewAdapter) myScrollView.getAdapter()).fullRefresh = true;
                            HomeTabUpdating = true;
                            SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateHomeTab();
                        } else {
                            HomeTabRefreshLayout.setRefreshing(false);
                        }
                    }
                });

                /* Setting scale transformer */
                myScrollView.setItemTransitionTimeMillis(currentActivity.getResources().getInteger(R.integer.LISTS_ITEMS_SWITCH_TIME));
                myScrollView.setItemTransformer(new ScaleTransformer.Builder()
                        .setMaxScale(1.0f)
                        .setMinScale(0.05f)
                        .build());

                /* Setting on scroll listener (auto ListView filling) */
                myScrollView.addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {

                    @Override
                    public void onScrollStart(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                        ((HomeTabRecyclerViewAdapter) myScrollView.getAdapter()).MuteMediaPlayer();

                    }

                    @Override
                    public void onScrollEnd(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                        int HomeTabRefreshPosition = SingletonFlashbombEntities.getInstance(currentActivity).getHomeTabListViewArray().size() - 1;
                        if (!HomeTabUpdating && HomeTabRefreshPosition > 0 && adapterPosition == HomeTabRefreshPosition) {
                            HomeTabUpdating = true;
                            SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateHomeTab();
                        }

                    }

                    @Override
                    public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable RecyclerView.ViewHolder currentHolder, @Nullable RecyclerView.ViewHolder newCurrent) {
                    }
                });

                /* Setting adapter */
                HomeTabRecyclerViewAdapter adapter = new HomeTabRecyclerViewAdapter(
                        (MainActivity) currentActivity,
                        SingletonFlashbombEntities.getInstance(currentActivity).getHomeTabListViewArray(),
                        CountElementHeight()
                );
                myScrollView.setAdapter(adapter);
                currentActivity.findViewById(R.id.homePageProgressBar).setVisibility(View.GONE);

                ((MainActivity) currentActivity).SplashAnimationLaunch();

            } else {

                if (pics.length() > 0 || SingletonFlashbombEntities.getInstance(currentActivity).getHomeTabListViewArray().size() == 0 || SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).getHomeTabListViewArray().size() == 1)
                    myScrollView.getAdapter().notifyDataSetChanged();

            }
        }

        if (currentActivity != null)
            HomeTabRefreshLayout = currentActivity.findViewById(R.id.homePageListRefresh);
        if (HomeTabRefreshLayout != null)
            HomeTabRefreshLayout.setRefreshing(false);
        HomeTabUpdating = false;

    }

    private void ParseBestTab(JSONObject json) throws JSONException {

        if (currentActivity.getClass() != MainActivity.class)
            return;

        /* Getting pics from JSON */
        JSONArray pics = json.getJSONArray("pictures");

        /* Getting shared prefs */
        final SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);

        /* Adding pictures from JSON */
        for (int i = 0; i < pics.length(); i++) {
            FlashbombEntity newPic = new FlashbombEntity(pics.getJSONObject(i), ResponseType.BEST_TAB, false);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(newPic);
        }

        /* Setting adapter to apropriate ListView */
        final DiscreteScrollView myScrollView = currentActivity.findViewById(R.id.bestPageList);
        myScrollView.setOffscreenItems(0);

        if (myScrollView.getAdapter() == null) {

            /* Pull to refresh listener */
            BestTabRefreshLayout = currentActivity.findViewById(R.id.bestPageListRefresh);
            BestTabRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!BestTabUpdating) {
                        SingletonFlashbombEntities.getInstance(currentActivity).getBestTabListViewArray().clear();
                        myScrollView.getAdapter().notifyDataSetChanged();
                        ((BestTabRecyclerViewAdapter) myScrollView.getAdapter()).fullRefresh = true;
                        BestTabUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateBestTab();
                    } else {
                        BestTabRefreshLayout.setRefreshing(false);
                    }
                }
            });

            /* Setting scale transformer */
            myScrollView.setItemTransitionTimeMillis(currentActivity.getResources().getInteger(R.integer.LISTS_ITEMS_SWITCH_TIME));
            myScrollView.setItemTransformer(new ScaleTransformer.Builder()
                    .setMaxScale(1.0f)
                    .setMinScale(0.05f)
                    .build());

            /* Setting on scroll listener (auto ListView filling) */
            myScrollView.addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {

                @Override
                public void onScrollStart(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                    ((BestTabRecyclerViewAdapter) myScrollView.getAdapter()).MuteMediaPlayer();

                }

                @Override
                public void onScrollEnd(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                    int BestTabRefreshPosition = SingletonFlashbombEntities.getInstance(currentActivity).getBestTabListViewArray().size() - 1;
                    if (!BestTabUpdating && BestTabRefreshPosition > 0 && adapterPosition >= BestTabRefreshPosition) {
                        BestTabUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateBestTab();
                    }

                }

                @Override
                public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable RecyclerView.ViewHolder currentHolder, @Nullable RecyclerView.ViewHolder newCurrent) {
                }
            });

            /* Setting adapter */
            BestTabRecyclerViewAdapter adapter = new BestTabRecyclerViewAdapter(
                    (MainActivity) currentActivity,
                    SingletonFlashbombEntities.getInstance(currentActivity).getBestTabListViewArray(),
                    CountElementHeight()
            );
            myScrollView.setAdapter(adapter);
            currentActivity.findViewById(R.id.bestPageProgressBar).setVisibility(View.GONE);

        } else {

            if (pics.length() > 0 || SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).getBestTabListViewArray().size() == 0)
                myScrollView.getAdapter().notifyDataSetChanged();

        }

        if (currentActivity != null)
            BestTabRefreshLayout = currentActivity.findViewById(R.id.bestPageListRefresh);
        if (BestTabRefreshLayout != null)
            BestTabRefreshLayout.setRefreshing(false);
        BestTabUpdating = false;

    }

    private void ParseObservationsTab(JSONObject json) throws JSONException {

        if (currentActivity.getClass() != MainActivity.class)
            return;

        /* Getting pics from JSON */
        JSONArray pics = json.getJSONArray("pictures");

        /* Setting tutorials */
        if (pics.length() == 0 && SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray().size() == 0) {

            currentActivity.findViewById(R.id.observationsPageTutorial).setVisibility(View.VISIBLE);
            if (SingletonFlashbombEntities.getInstance(currentActivity).getObservationsManagementListViewArray().size() > 0)
                ((TextView) currentActivity.findViewById(R.id.observationsPageTutorialTextView)).setText(currentActivity.getResources().getString(R.string.observations_tutorial_no_bombs));
            else
                ((TextView) currentActivity.findViewById(R.id.observationsPageTutorialTextView)).setText(currentActivity.getResources().getString(R.string.observations_tutorial_no_observations));
            ((TextView) currentActivity.findViewById(R.id.observationsPageTutorialTextView)).setTypeface(InitApp.contentsTypeface);

        } else {

            currentActivity.findViewById(R.id.observationsPageTutorial).setVisibility(View.GONE);

        }

        /* Adding pictures from JSON */
        for (int i = 0; i < pics.length(); i++) {
            FlashbombEntity newPic = new FlashbombEntity(pics.getJSONObject(i), ResponseType.OBSERVATIONS_TAB, false);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(newPic);
        }

        /* Setting adapter to apropriate ListView */
        final DiscreteScrollView myScrollView = currentActivity.findViewById(R.id.observationsPageList);
        myScrollView.setOffscreenItems(0);

        if (myScrollView.getAdapter() == null) {

            /* Pull to refresh listener */
            ObservationsTabRefreshLayout = currentActivity.findViewById(R.id.observationsPageListRefresh);
            ObservationsTabRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!ObservationsTabUpdating) {
                        SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray().clear();
                        myScrollView.getAdapter().notifyDataSetChanged();
                        ((ObservationsTabRecyclerViewAdapter) myScrollView.getAdapter()).fullRefresh = true;
                        ObservationsTabUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateObservationsTab();
                    } else {
                        ObservationsTabRefreshLayout.setRefreshing(false);
                    }
                }
            });

            /* Setting scale transformer */
            myScrollView.setItemTransitionTimeMillis(currentActivity.getResources().getInteger(R.integer.LISTS_ITEMS_SWITCH_TIME));
            myScrollView.setItemTransformer(new ScaleTransformer.Builder()
                    .setMaxScale(1.0f)
                    .setMinScale(0.05f)
                    .build());

            /* Setting on scroll listener (auto ListView filling) */
            myScrollView.addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {

                @Override
                public void onScrollStart(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                    ((ObservationsTabRecyclerViewAdapter) myScrollView.getAdapter()).MuteMediaPlayer();

                }

                @Override
                public void onScrollEnd(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                    int ObservationsTabRefreshPosition = SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray().size() - 1;
                    if (!ObservationsTabUpdating && ObservationsTabRefreshPosition > 0 && adapterPosition >= ObservationsTabRefreshPosition) {
                        ObservationsTabUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateObservationsTab();
                    }

                }

                @Override
                public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable RecyclerView.ViewHolder currentHolder, @Nullable RecyclerView.ViewHolder newCurrent) {
                }
            });

            /* Setting adapter */
            ObservationsTabRecyclerViewAdapter adapter = new ObservationsTabRecyclerViewAdapter(
                    (MainActivity) currentActivity,
                    SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray(),
                    CountElementHeight()
            );
            myScrollView.setAdapter(adapter);
            currentActivity.findViewById(R.id.observationsPageProgressBar).setVisibility(View.GONE);

        } else {

            if (pics.length() > 0 || SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).getObservationsTabListViewArray().size() == 0)
                myScrollView.getAdapter().notifyDataSetChanged();

        }

        if (currentActivity != null)
            ObservationsTabRefreshLayout = currentActivity.findViewById(R.id.observationsPageListRefresh);
        if (ObservationsTabRefreshLayout != null)
            ObservationsTabRefreshLayout.setRefreshing(false);
        ObservationsTabUpdating = false;

    }

    private void ParseInfluencersTab(JSONObject json) throws JSONException {

        if (currentActivity.getClass() != MainActivity.class)
            return;

        /* Getting influencers from JSON */
        JSONArray influencers = json.getJSONArray("influencers");

        /* Adding influencers from JSON */
        for (int i = 0; i < influencers.length(); i++) {
            FlashbombInfluencer newInfluencer = new FlashbombInfluencer(influencers.getJSONObject(i));
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddInfluencer(newInfluencer);
        }

        /* Setting adapter to apropriate ListView */
        final RecyclerView myScrollView = currentActivity.findViewById(R.id.influencersPageList);

        if (myScrollView.getAdapter() == null) {

            /* Pull to refresh listener */
            InfluencersTabRefreshLayout = currentActivity.findViewById(R.id.influencersPageListRefresh);
            InfluencersTabRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!InfluencersUpdating) {
                        SingletonFlashbombEntities.getInstance(currentActivity).getInfluencersTabListViewArray().clear();
                        myScrollView.getAdapter().notifyDataSetChanged();
                        ((InfluencersTabRecyclerViewAdapter) myScrollView.getAdapter()).fullRefresh = true;
                        InfluencersUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateInfluencersTab();
                    } else {
                        InfluencersTabRefreshLayout.setRefreshing(false);
                    }
                }
            });

            /* Setting adapter */
            InfluencersTabRecyclerViewAdapter adapter = new InfluencersTabRecyclerViewAdapter(
                    (MainActivity) currentActivity,
                    SingletonFlashbombEntities.getInstance(currentActivity).getInfluencersTabListViewArray(),
                    Math.round((float)CountElementHeight() / 3)
            );
            myScrollView.setHasFixedSize(true);
            myScrollView.setLayoutManager(new LinearLayoutManager(currentActivity));
            myScrollView.setAdapter(adapter);
            currentActivity.findViewById(R.id.influencersPageProgressBar).setVisibility(View.GONE);

        } else {

            if (influencers.length() > 0 || SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).getInfluencersTabListViewArray().size() == 0)
                myScrollView.getAdapter().notifyDataSetChanged();

        }

        if (currentActivity != null)
            InfluencersTabRefreshLayout = currentActivity.findViewById(R.id.influencersPageListRefresh);
        if (InfluencersTabRefreshLayout != null)
            InfluencersTabRefreshLayout.setRefreshing(false);
        InfluencersUpdating = false;

    }

    private void ParseProfileTab(JSONObject json) throws JSONException {

        if (currentActivity.findViewById(R.id.profileGridView) == null)
            return;

        /* OneSignal tags */
        JSONObject tags = new JSONObject();
        if (json.has("observators"))
            tags.put("observators", Integer.parseInt(json.getString("observators")));
        if (json.has("pictures_uploaded"))
            tags.put("pictures_uploaded", Integer.parseInt(json.getString("pictures_uploaded")));
        OneSignal.sendTags(tags);

        /* Getting pics from JSON */
        JSONArray pics = json.getJSONArray("pictures");

        if (SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).getProfileTabListViewArray().size() == 0) {

            /* Adding initial elements */
            FlashbombEntity initialEntity = new FlashbombEntity(json, ResponseType.PROFILE_TAB, false);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(initialEntity);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(initialEntity);

            /* Adding local, not uploaded picture */
            try {
                boolean reuploadActive = false;
                Long latestSessionInProfile = Long.parseLong(pics.length() > 0 ? pics.getJSONObject(0).optString("session_id", "-1") : "-1");
                File folder = new File(mCtx.getFilesDir() + "/Flashbomb");
                String[] files = folder.list();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        String localPictureThumbnailPath = folder.getAbsolutePath() + "/" + files[i];
                        if (files[i].contains("thumbnail")) {
                            String[] entityParams = files[i].substring(0, files[i].indexOf('.')).split("_");
                            if (Long.parseLong(entityParams[3].replace("<u>", "_")) > latestSessionInProfile) {
                                FlashbombEntity localPic = new FlashbombEntity(ResponseType.PROFILE_TAB, entityParams[3].replace("<u>", "_"), localPictureThumbnailPath);
                                SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(localPic);
                                SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(localPic);
                                reuploadActive = true;
                            }
                        }
                    }
                }
                ImageView profileButton = currentActivity.findViewById(R.id.profileButton);
                if (profileButton != null) {
                    profileButton.setImageResource((UploadService.getTaskList().size() == 0 && reuploadActive) ? R.drawable.ic_profile_alert : R.drawable.ic_profile);
                }
            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }
        }


        /* Adding pictures from JSON */
        for (int i = 0; i < pics.length(); i++) {
            FlashbombEntity newPic = new FlashbombEntity(pics.getJSONObject(i), ResponseType.PROFILE_TAB, false);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(newPic);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(newPic);
        }

        /* Setting adapter to apropriate ListView */
        final GridView myGridView = currentActivity.findViewById(R.id.profileGridView);

        if (myGridView.getAdapter() == null) {

            /* Pull to refresh listener */
            ProfileTabRefreshLayout = currentActivity.findViewById(R.id.profileGridViewRefresh);
            ProfileTabRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!ProfileTabUpdating) {
                        SingletonFlashbombEntities.getInstance(currentActivity).getProfileTabListViewArray().clear();
                        ProfileTabUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateProfileTab();
                    } else {
                        ProfileTabRefreshLayout.setRefreshing(false);
                    }
                }
            });

            /* Setting on scroll listener (auto GridView filling) */
            myGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

                int latestRefreshPosition = -1;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    int ProfileTabRefreshPosition = SingletonFlashbombEntities.getInstance(currentActivity).getProfileTabListViewArray().size() - 1;
                    if (!ProfileTabUpdating && latestRefreshPosition != ProfileTabRefreshPosition && ProfileTabRefreshPosition > 0 && firstVisibleItem + visibleItemCount - 1 >= ProfileTabRefreshPosition) {
                        ProfileTabUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateProfileTab();
                        latestRefreshPosition = ProfileTabRefreshPosition;
                    }

                }

            });

            /* Setting adapter */
            ProfileGridViewAdapter adapter = new ProfileGridViewAdapter(
                    currentActivity,
                    SingletonFlashbombEntities.getInstance(currentActivity).getProfileTabListViewArray()
            );
            myGridView.setAdapter(adapter);
            currentActivity.findViewById(R.id.profileProgressBar).setVisibility(View.GONE);

        } else {

            ((ProfileGridViewAdapter) myGridView.getAdapter()).notifyDataSetChanged();

        }

        if (currentActivity != null) {
            ProfileTabRefreshLayout = currentActivity.findViewById(R.id.profileGridViewRefresh);
        }
        if (ProfileTabRefreshLayout != null)
            ProfileTabRefreshLayout.setRefreshing(false);
        ProfileTabUpdating = false;

    }

    private void ParseObservations(JSONObject json) throws JSONException {

        /* Getting pics from JSON */
        JSONArray obs = json.getJSONArray("observations");

        /* OneSignal tags */
        JSONObject tags = new JSONObject();
        if (json.has("observations")) tags.put("observations", obs.length());
        OneSignal.sendTags(tags);

         /* Adding observations from JSON */
        for (int i = 0; i < obs.length(); i++) {
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddObservation(obs.getString(i));
        }

        /* Setting adapter */
        final ListView myListView = currentActivity.findViewById(R.id.observationsManagementListView);
        ObservationsManagementListViewAdapter adapter = new ObservationsManagementListViewAdapter(
                currentActivity,
                SingletonFlashbombEntities.getInstance(currentActivity).getObservationsManagementListViewArray()
        );
        myListView.setAdapter(adapter);

    }

    private void ParseBlacklist(JSONObject json) throws JSONException {

        if (currentActivity.findViewById(R.id.blacklistListView) == null)
            return;

        /* Getting observers from JSON */
        JSONArray obs = json.getJSONArray("observators");

        /* Adding observers from JSON */
        for (int i = 0; i < obs.length(); i++) {
            Pair<String, Integer> blacklistItem = new Pair<String, Integer>(obs.getJSONObject(i).getString("nick"), Integer.parseInt(obs.getJSONObject(i).getString("hidden")));
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddBlacklistItem(blacklistItem);
        }

        /* Setting adapter to apropriate ListView */
        final ListView blacklistListView = currentActivity.findViewById(R.id.blacklistListView);

        if (blacklistListView.getAdapter() == null) {

            /* Pull to refresh listener */
            BlacklistTabRefreshLayout = currentActivity.findViewById(R.id.blacklistListViewRefresh);
            BlacklistTabRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!BlacklistUpdating) {
                        SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().clear();
                        BlacklistUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateBlacklist();
                    } else {
                        BlacklistTabRefreshLayout.setRefreshing(false);
                    }
                }
            });

            /* Setting adapter */
            BlacklistListViewAdapter adapter = new BlacklistListViewAdapter(
                    currentActivity,
                    SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray()
            );
            blacklistListView.setAdapter(adapter);
            currentActivity.findViewById(R.id.blacklistProgressBar).setVisibility(View.GONE);

        } else {

            ((BlacklistListViewAdapter) blacklistListView.getAdapter()).notifyDataSetChanged();

        }

        if (currentActivity != null)
            BlacklistTabRefreshLayout = currentActivity.findViewById(R.id.blacklistListViewRefresh);
        if (BlacklistTabRefreshLayout != null)
            BlacklistTabRefreshLayout.setRefreshing(false);
        BlacklistUpdating = false;

        /* Showing / hiding tutorial */
        currentActivity.findViewById(R.id.blacklistTutorial).setVisibility(SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().size() > 0 ? View.GONE : View.VISIBLE);

    }

    private void ParseSearchObservations(JSONObject json) throws JSONException {

        /* Getting nicks from JSON */
        JSONArray nicks = json.getJSONArray("nicks");
        String phrase = json.optString("phrase", "");

        /* Setting current phrase */
        SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).setCurrentSearchPhrase(phrase);

         /* Adding nicks from JSON */
        for (int i = 0; i < nicks.length(); i++) {
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddSearchedNick(nicks.getString(i));
        }

        final ListView myListView = currentActivity.findViewById(R.id.mainActivitySearchListView);

        ((ObservationsSearchListViewAdapter) myListView.getAdapter()).notifyDataSetChanged();

        if (SingletonFlashbombEntities.getInstance(currentActivity).getSearchNicksSubarray().size() > 0)
            ((MainActivity) currentActivity).searchTextListener.ShowSuggestions();

    }

    private void ParseLoginRegister(JSONObject json, boolean cached, boolean register) throws JSONException {

        /* Setting current server time */
        ServerTime.setServerTimestamp(json.optString("current_timestamp", ""));

        /* Saving login / password / user data */
        final SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(currentActivity.getString(R.string.PREFERENCES_LOGIN_KEY), json.optString("nick", ""));
        editor.putString(currentActivity.getString(R.string.PREFERENCES_PASSWORD_KEY), json.optString("password", ""));
        editor.putString(currentActivity.getString(R.string.PREFERENCES_NICK_KEY), json.optString("nick", ""));
        editor.putString(currentActivity.getString(R.string.PREFERENCES_EMAIL_KEY), json.optString("email", ""));
        editor.putString(currentActivity.getString(R.string.PREFERENCES_BIRTHYEAR_KEY), json.optString("birthyear", ""));

        /* Crashlytics UserID */
        Crashlytics.setUserIdentifier(json.optString("nick", ""));

        /* Managing sessions */
        if (!cached) {

            Long sessionId = Long.parseLong(json.optString("latest_session_id", "-1"));
            editor.putString(currentActivity.getString(R.string.PREFERENCES_LATEST_SESSION_ID_KEY), sessionId.toString());
            Long previousSessionId = Long.parseLong(sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), "-1"));
            Long notificationDelay = ServerTime.getCurrentTimestamp() - sessionId;
            if (sessionId > -1 && sessionId > previousSessionId && notificationDelay < currentActivity.getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS)) {
                editor.putString(currentActivity.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), sessionId.toString());
                Long endTimestamp = ServerTime.getCurrentTimestamp() + currentActivity.getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS);
                editor.putString(currentActivity.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), endTimestamp.toString());
                editor.putBoolean(currentActivity.getString(R.string.PREFERENCES_BEST_LEAFLET_ACTIVE), true);
            }

            /* Disabling session if user already participated */
            if (json.optString("participated", "0").equals("1") || register) {
                editor.putString(currentActivity.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1");
            }

            editor.apply();

        }

        /* Camera debug mode */
        if (currentActivity.getResources().getBoolean(R.bool.CAMERA_DEBUG_MODE)) {
            editor.putString(currentActivity.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), (ServerTime.getCurrentTimestamp()) + "");
            editor.putString(currentActivity.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), (ServerTime.getCurrentTimestamp() + currentActivity.getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS)) + "");
            editor.apply();
        }

        /* Checking app version */
        try {

            PackageInfo pInfo = currentActivity.getPackageManager().getPackageInfo(currentActivity.getPackageName(), 0);
            int local_version = pInfo.versionCode;
            int store_version = Integer.parseInt(json.optString("android_version", "0"));
            editor.putBoolean(currentActivity.getString(R.string.PREFERENCES_APP_OUTDATED), store_version > local_version);
            editor.apply();

        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

        /* Checking account verification state */
        String verified = json.optString("verified", "0");
        editor.putBoolean(currentActivity.getString(R.string.PREFERENCES_ACCOUNT_VERIFIED), verified.equals("1"));
        editor.apply();

        /* OneSignal subscription */
        OneSignal.setSubscription(verified.equals("1"));

        /* OneSignal tags */
        JSONObject tags = new JSONObject();
        if (json.has("nick")) tags.put("nick", json.getString("nick"));
        if (json.has("email")) tags.put("email", json.getString("email"));
        if (json.has("birthyear")) tags.put("birthyear", Integer.parseInt(json.getString("birthyear")));
        if (json.has("verified")) tags.put("verified", json.getString("verified").equals("1"));
        if (json.has("best_anonymous")) tags.put("best_anonymous", json.getString("best_anonymous").equals("1"));
        OneSignal.sendTags(tags);

        /* Setting best_anonymous */
        editor.putBoolean(currentActivity.getString(R.string.PREFERENCES_ANONYMOUS_BEST), json.optString("best_anonymous", "1").equals("1"));
        editor.apply();

        /* Showing SplashScreen when in login screen */
        if (currentActivity.getClass() == LoginActivity.class) {

            Animation splashAnim = AnimationUtils.loadAnimation(currentActivity, R.anim.fade_in);
            splashAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    currentActivity.findViewById(R.id.loginActivitySplash).setLayerType(View.LAYER_TYPE_NONE, null);

                    /* Launching main activity */
                    Intent myIntent;
                    myIntent = new Intent(currentActivity, MainActivity.class);
                    myIntent.putExtra("caller", currentActivity.getClass().getSimpleName());
                    currentActivity.startActivity(myIntent);
                    currentActivity.finish();

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            currentActivity.findViewById(R.id.loginActivitySplashLottie).setVisibility(View.INVISIBLE);
            currentActivity.findViewById(R.id.loginActivitySplash).setLayerType(View.LAYER_TYPE_HARDWARE, null);
            currentActivity.findViewById(R.id.loginActivitySplash).startAnimation(splashAnim);

        } else {

            /* Launching main or camera activity */
            boolean cameraLaunch = false;
            Intent myIntent = new Intent(currentActivity, MainActivity.class);

            if (sharedPref.getBoolean(currentActivity.getString(R.string.PREFERENCES_NOTIFICATION_OPENED_FLAG), false)) {

                editor = sharedPref.edit();
                editor.remove(currentActivity.getString(R.string.PREFERENCES_NOTIFICATION_OPENED_FLAG));
                editor.apply();

                long session_end = Long.parseLong(sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1"));
                long timeleft = session_end - ServerTime.getCurrentTimestamp();
                timeleft = (timeleft > 0) ? timeleft : 0;
                timeleft = (timeleft < mCtx.getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS)) ? timeleft : mCtx.getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS);
                if (timeleft > 0) {

                    if (InitApp.RequestLackingPermissions(currentActivity, currentActivity.getResources().getInteger(R.integer.CAMERA_PERMISSIONS_REQUEST_CODE)))
                        return;

                    myIntent = new Intent(currentActivity, CameraActivity.class);
                    myIntent.putExtra("type", "default");
                    myIntent.putExtra("timeleft", timeleft);
                    myIntent.putExtra("session_id", sharedPref.getString(currentActivity.getResources().getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), ""));
                    myIntent.putExtra("total_time", currentActivity.getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS));
                    cameraLaunch = true;

                }
            }

            if (cameraLaunch) {

                Handler handler = new Handler();
                final Intent finalMyIntent = myIntent;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finalMyIntent.putExtra("caller", currentActivity.getClass().getSimpleName());
                        currentActivity.startActivity(finalMyIntent);
                        System.exit(0);
                    }
                }, 100);

            } else {

                myIntent.putExtra("caller", currentActivity.getClass().getSimpleName());
                currentActivity.startActivity(myIntent);
                currentActivity.finish();

            }

        }

    }

    private void ParseObserve(JSONObject json) throws JSONException {

        /* Getting observed nick */
        String obs = json.optString("observation", "");
        SingletonFlashbombEntities.getInstance(currentActivity).AddObservation(obs);

        /* Checking if observation from url */
        final SharedPreferences sharedPrefs = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        final String observeNick = sharedPrefs.getString(mCtx.getString(R.string.PREFERENCES_URL_OBSERVATION), "");
        sharedPrefs.edit().remove(mCtx.getString(R.string.PREFERENCES_URL_OBSERVATION)).apply();
        if(observeNick.length() > 2) {

            final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
            alertDialog.setTitle(currentActivity.getString(R.string.dialog_obs_confirm_title));
            alertDialog.setMessage(Html.fromHtml(currentActivity.getString(R.string.dialog_obs_confirm_text).replace("{1}", "<b>" + obs + "</b>")));
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, currentActivity.getString(R.string.understood), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();

                }

            });
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(currentActivity.getResources().getColor(R.color.yellow_transparent_best));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(currentActivity.getResources().getColor(R.color.red));
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(currentActivity.getResources().getColor(R.color.green));
                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }

        /* Updating observations */
        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateObservationsTab();

    }

    private void ParseUnobserve(JSONObject json) throws JSONException {

        /* Getting unobserved nick */
        String obs = json.optString("observation", "");
        SingletonFlashbombEntities.getInstance(currentActivity).RemoveObservation(obs);

        /* Updating observations RecyclerView */
        SingletonFlashbombEntities.getInstance(currentActivity).RemoveEntitiesByUser(obs, false, false, true);
        if(currentActivity.getClass() == MainActivity.class) {

            if(((DiscreteScrollView) currentActivity.findViewById(R.id.observationsPageList)).getAdapter() != null)
                ((DiscreteScrollView) currentActivity.findViewById(R.id.observationsPageList)).getAdapter().notifyDataSetChanged();

            /* Setting tutorials */
            if (SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray().size() == 0) {

                currentActivity.findViewById(R.id.observationsPageTutorial).setVisibility(View.VISIBLE);
                if (SingletonFlashbombEntities.getInstance(currentActivity).getObservationsManagementListViewArray().size() > 0)
                    ((TextView) currentActivity.findViewById(R.id.observationsPageTutorialTextView)).setText(currentActivity.getResources().getString(R.string.observations_tutorial_no_bombs));
                else
                    ((TextView) currentActivity.findViewById(R.id.observationsPageTutorialTextView)).setText(currentActivity.getResources().getString(R.string.observations_tutorial_no_observations));
                ((TextView) currentActivity.findViewById(R.id.observationsPageTutorialTextView)).setTypeface(InitApp.contentsTypeface);

            } else {

                currentActivity.findViewById(R.id.observationsPageTutorial).setVisibility(View.GONE);

            }

        }

    }

    private void ParseJoinLeaveInfluencer(JSONObject json) throws JSONException {

        final RecyclerView influencersRecyclerView = currentActivity.findViewById(R.id.influencersPageList);
        final InfluencersTabRecyclerViewAdapter influencersAdapter = influencersRecyclerView == null ? null : (InfluencersTabRecyclerViewAdapter) influencersRecyclerView.getAdapter();
        final SwipeRefreshLayout influencersList = currentActivity.findViewById(R.id.influencersPageListRefresh);

        /* Refreshing influencers tab */
        if (influencersList != null && influencersAdapter != null) {

            influencersList.post(new Runnable() {
                @Override
                public void run() {
                    influencersList.setRefreshing(true);
                }
            });

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ApiParser.InfluencersUpdating = true;
                    SingletonFlashbombEntities.getInstance(currentActivity).getInfluencersTabListViewArray().clear();
                    influencersAdapter.notifyDataSetChanged();
                    influencersAdapter.fullRefresh = true;
                    SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateInfluencersTab();
                }
            }, 750);

        }

    }

    private void ParseInfluencerGallery(JSONObject json) throws JSONException {

        if (currentActivity.getClass() != MainActivity.class)
            return;

        /* Getting pics from JSON */
        JSONArray pics = json.getJSONArray("pictures");

        /* Getting influencerID */
        final String influencerID = json.optString("influencer_id", "0");

        /* Adding pictures from JSON */
        for (int i = 0; i < pics.length(); i++) {
            FlashbombEntity newPic = new FlashbombEntity(pics.getJSONObject(i), ResponseType.INFLUENCER_GALLERY, false);
            SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).AddEntity(newPic);
        }

        /* Setting adapter to apropriate ListView */
        final DiscreteScrollView myScrollView = currentActivity.findViewById(R.id.influencerGalleryList);
        myScrollView.setOffscreenItems(0);

        if (myScrollView.getAdapter() == null) {

            /* Pull to refresh listener */
            InfluencerGalleryRefreshLayout = currentActivity.findViewById(R.id.influencerGalleryListRefresh);
            InfluencerGalleryRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!InfluencerGalleryUpdating) {
                        SingletonFlashbombEntities.getInstance(currentActivity).getInfluencerGalleryListViewArray().clear();
                        myScrollView.getAdapter().notifyDataSetChanged();
                        ((InfluencerGalleryRecyclerViewAdapter) myScrollView.getAdapter()).fullRefresh = true;
                        ObservationsTabUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateInfluencerGallery(influencerID);
                    } else {
                        InfluencerGalleryRefreshLayout.setRefreshing(false);
                    }
                }
            });

            /* Setting scale transformer */
            myScrollView.setItemTransitionTimeMillis(currentActivity.getResources().getInteger(R.integer.LISTS_ITEMS_SWITCH_TIME));
            myScrollView.setItemTransformer(new ScaleTransformer.Builder()
                    .setMaxScale(1.0f)
                    .setMinScale(0.05f)
                    .build());

            /* Setting on scroll listener (auto ListView filling) */
            myScrollView.addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {

                @Override
                public void onScrollStart(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                    ((InfluencerGalleryRecyclerViewAdapter) myScrollView.getAdapter()).MuteMediaPlayer();

                }

                @Override
                public void onScrollEnd(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

                    int InfluencerGalleryRefreshPosition = SingletonFlashbombEntities.getInstance(currentActivity).getInfluencerGalleryListViewArray().size() - 1;
                    if (!InfluencerGalleryUpdating && InfluencerGalleryRefreshPosition > 0 && adapterPosition >= InfluencerGalleryRefreshPosition) {
                        InfluencerGalleryUpdating = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateInfluencerGallery(influencerID);
                    }

                }

                @Override
                public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable RecyclerView.ViewHolder currentHolder, @Nullable RecyclerView.ViewHolder newCurrent) {
                }
            });

            /* Setting adapter */
            InfluencerGalleryRecyclerViewAdapter adapter = new InfluencerGalleryRecyclerViewAdapter(
                    (MainActivity) currentActivity,
                    SingletonFlashbombEntities.getInstance(currentActivity).getInfluencerGalleryListViewArray(),
                    CountElementHeight(),
                    influencerID
            );
            myScrollView.setAdapter(adapter);
            currentActivity.findViewById(R.id.influencerGalleryProgressBar).setVisibility(View.GONE);

        } else {

            if (pics.length() > 0 || SingletonFlashbombEntities.getInstance(currentActivity.getApplicationContext()).getInfluencerGalleryListViewArray().size() == 0)
                myScrollView.getAdapter().notifyDataSetChanged();

        }

        if (currentActivity != null)
            InfluencerGalleryRefreshLayout = currentActivity.findViewById(R.id.influencerGalleryListRefresh);
        if (InfluencerGalleryRefreshLayout != null)
            InfluencerGalleryRefreshLayout.setRefreshing(false);
        InfluencerGalleryUpdating = false;

    }

    private void ParseHide(JSONObject json) throws JSONException {

        /* Getting hidden nick */
        String obs = json.optString("observation", "");

        /* Updating blacklist */
        int index = -1;
        for (int i = 0; i < SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().size(); i++) {
            if (SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().get(i).first != null &&
                    SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().get(i).first.equals(obs)) {
                index = i;
                break;
            }
        }
        if (index > -1)
            SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().set(index, new Pair<>(obs, 1));

    }

    private void ParseUnhide(JSONObject json) throws JSONException {

        /* Getting unhidden nick */
        String obs = json.optString("observation", "");

        /* Updating blacklist */
        int index = -1;
        for (int i = 0; i < SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().size(); i++) {
            if (SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().get(i).first != null &&
                    SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().get(i).first.equals(obs)) {
                index = i;
                break;
            }
        }
        if (index > -1)
            SingletonFlashbombEntities.getInstance(currentActivity).getBlacklistListViewArray().set(index, new Pair<>(obs, 0));

    }

    private void ParseReport(JSONObject json) throws JSONException {

        final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
        alertDialog.setTitle(currentActivity.getString(R.string.thank_you));
        alertDialog.setMessage(currentActivity.getString(R.string.report_info));
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, currentActivity.getString(R.string.understood), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            }

        });
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(currentActivity.getResources().getColor(R.color.yellow_transparent_best));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(currentActivity.getResources().getColor(R.color.red));
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(currentActivity.getResources().getColor(R.color.green));
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return;

    }

    private void ParseForgotPassword(JSONObject json) throws JSONException {

        final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
        alertDialog.setTitle(currentActivity.getString(R.string.password_recovery_info_title));
        alertDialog.setMessage(currentActivity.getString(R.string.password_recovery_info_success));
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, currentActivity.getString(R.string.understood), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(currentActivity.getResources().getColor(R.color.yellow_transparent_best));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(currentActivity.getResources().getColor(R.color.red));
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(currentActivity.getResources().getColor(R.color.green));
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return;

    }

    private void ParseResendActivation(JSONObject json) throws JSONException {

        final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
        alertDialog.setTitle(currentActivity.getString(R.string.activation_resend_success_title));
        alertDialog.setMessage(currentActivity.getString(R.string.activation_resend_success_content));
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, currentActivity.getString(R.string.understood), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(currentActivity.getResources().getColor(R.color.yellow_transparent_best));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(currentActivity.getResources().getColor(R.color.red));
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(currentActivity.getResources().getColor(R.color.green));
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return;

    }

    private void ParseChangePassword(JSONObject json) throws JSONException {

        /* Prefs */
        final SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        /* Getting new password */
        String newPassword = json.optString("new_password", sharedPref.getString(currentActivity.getString(R.string.PREFERENCES_PASSWORD_KEY), ""));
        editor.putString(currentActivity.getString(R.string.PREFERENCES_PASSWORD_KEY), newPassword);
        editor.apply();

        final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
        alertDialog.setTitle(currentActivity.getString(R.string.change_password_success_title));
        alertDialog.setMessage(currentActivity.getString(R.string.change_passsword_success_content));
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, currentActivity.getString(R.string.understood), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                ((MainActivity)(currentActivity)).back(null);

            }

        });
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(currentActivity.getResources().getColor(R.color.yellow_transparent_best));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(currentActivity.getResources().getColor(R.color.red));
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(currentActivity.getResources().getColor(R.color.green));
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        return;

    }

    private void ParseAnonymous(JSONObject json) throws JSONException {

        /* Setting prefs */
        final SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(mCtx.getString(R.string.PREFERENCES_ANONYMOUS_BEST), true);
        editor.apply();

        if(currentActivity.findViewById(R.id.settingsListView) != null) {
            ((SettingsListViewAdapter)((ListView)currentActivity.findViewById(R.id.settingsListView)).getAdapter()).notifyDataSetChanged();
        }

    }

    private void ParseNonAnonymous(JSONObject json) throws JSONException {

        /* Setting prefs */
        final SharedPreferences sharedPref = currentActivity.getSharedPreferences(currentActivity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(mCtx.getString(R.string.PREFERENCES_ANONYMOUS_BEST), false);
        editor.apply();

        if(currentActivity.findViewById(R.id.settingsListView) != null) {
            ((SettingsListViewAdapter)((ListView)currentActivity.findViewById(R.id.settingsListView)).getAdapter()).notifyDataSetChanged();
        }

    }

    /* Help methods */
    private int CountElementHeight() {

        if (currentActivity.getClass() == MainActivity.class) {

            RelativeLayout homeActivityLayout = currentActivity.findViewById(R.id.mainActivityLayout);
            LinearLayout topBarLayout = currentActivity.findViewById(R.id.mainActivityHomeLayout);
            int elemHeight = homeActivityLayout.getHeight() - topBarLayout.getHeight();
            return elemHeight;

        }

        return 0;

    }

}
