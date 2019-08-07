package com.koszelew.flashbomb.Listeners;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.GridView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Adapters.HomeTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.ObservationsTabRecyclerViewAdapter;
import com.koszelew.flashbomb.Adapters.ProfileGridViewAdapter;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.SingletonFlashbombEntities;
import com.koszelew.flashbomb.Utils.Networking.ApiParser;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.io.File;

public class UploadStateListener extends UploadServiceBroadcastReceiver {

    public static Activity currentActivity;

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {

        super.onCompleted(context, uploadInfo, serverResponse);

        /* Session ends for this user */
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), "-1");
        editor.apply();

        /* Clearing upload photos / videos */
        File dir = new File(context.getFilesDir() + "/Flashbomb");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }

        /* If in main activity */
        if (currentActivity != null && currentActivity.getClass() == MainActivity.class) {

            /* Getting views and components */
            final DiscreteScrollView homePageRecyclerView = currentActivity.findViewById(R.id.homePageList);
            final DiscreteScrollView observationsPageRecyclerView = currentActivity.findViewById(R.id.observationsPageList);
            final GridView profilePageGridView = currentActivity.findViewById(R.id.profileGridView);

            final HomeTabRecyclerViewAdapter homeAdapter = homePageRecyclerView == null ? null : (HomeTabRecyclerViewAdapter) homePageRecyclerView.getAdapter();
            final ObservationsTabRecyclerViewAdapter observationsAdapter = observationsPageRecyclerView == null ? null : (ObservationsTabRecyclerViewAdapter) observationsPageRecyclerView.getAdapter();
            final ProfileGridViewAdapter profileAdapter = profilePageGridView == null ? null : (ProfileGridViewAdapter) profilePageGridView.getAdapter();

            final SwipeRefreshLayout homeRefresh = currentActivity.findViewById(R.id.homePageListRefresh);
            final SwipeRefreshLayout observationsRefresh = currentActivity.findViewById(R.id.observationsPageListRefresh);
            final SwipeRefreshLayout profileRefresh = currentActivity.findViewById(R.id.profileGridViewRefresh);

            /* Triggering swipe refresh layouts */
            if (homeAdapter != null && homeRefresh != null) {
                homeRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        homeRefresh.setRefreshing(true);
                    }
                });
            }
            if (observationsAdapter != null && observationsRefresh != null) {
                observationsRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        observationsRefresh.setRefreshing(true);
                    }
                });
            }
            if (profileAdapter != null && profileRefresh != null) {
                profileRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        profileRefresh.setRefreshing(true);
                    }
                });
            }

            final Context ctx = context;

            /* Refreshing lists */
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (homeAdapter != null && homeRefresh != null) {

                        ApiParser.HomeTabUpdating = true;
                        SingletonFlashbombEntities.getInstance(currentActivity).getHomeTabListViewArray().clear();
                        homeAdapter.notifyDataSetChanged();
                        homeAdapter.fullRefresh = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateHomeTab();

                    }
                    if (observationsAdapter != null && observationsRefresh != null) {

                        ApiParser.ObservationsTabUpdating = true;
                        SingletonFlashbombEntities.getInstance(currentActivity).getObservationsTabListViewArray().clear();
                        observationsAdapter.notifyDataSetChanged();
                        observationsAdapter.fullRefresh = true;
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateObservationsTab();

                    }
                    if (profileAdapter != null && profileRefresh != null) {

                        ApiParser.ProfileTabUpdating = true;
                        SingletonFlashbombEntities.getInstance(currentActivity).getProfileTabListViewArray().clear();
                        SingletonNetwork.getInstance(currentActivity.getApplicationContext()).UpdateProfileTab();

                    }

                }
            }, 3250);

        }

    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {

        super.onError(context, uploadInfo, serverResponse, exception);

        /* Firebase log */
        Bundle bundle = new Bundle();
        if (uploadInfo != null) {
            bundle.putInt("totalFiles", uploadInfo.getTotalFiles());
            bundle.putInt("progressPercent", uploadInfo.getProgressPercent());
            bundle.putInt("numberOfRetries", uploadInfo.getNumberOfRetries());
            bundle.putLong("elapsedTime", uploadInfo.getElapsedTime());
            bundle.putLong("uploadedBytes", uploadInfo.getUploadedBytes());
            bundle.putLong("totalBytes", uploadInfo.getTotalBytes());
            bundle.putDouble("uploadRate", uploadInfo.getUploadRate());
        }
        if (serverResponse != null) {
            bundle.putInt("httpCode", serverResponse.getHttpCode());
            bundle.putString("httpBody", serverResponse.getBodyAsString());
        }
        FirebaseAnalytics.getInstance(context).logEvent("UploadError", bundle);
        Crashlytics.logException(exception);

    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {

        super.onCancelled(context, uploadInfo);

        /* Firebase log */
        Bundle bundle = new Bundle();
        bundle.putInt("totalFiles", uploadInfo.getTotalFiles());
        bundle.putInt("progressPercent", uploadInfo.getProgressPercent());
        FirebaseAnalytics.getInstance(context).logEvent("UploadCancelled", bundle);

    }

}