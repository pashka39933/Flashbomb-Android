package com.koszelew.flashbomb.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Activities.SplashScreenActivity;
import com.koszelew.flashbomb.Adapters.SettingsListViewAdapter;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.onesignal.OneSignal;

import java.io.File;
import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    /* Options IDs delimiter */
    public final String OptionsIDsDelimiter = "=> ";

    /* Initialization */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        /* Getting nick from prefs */
        SharedPreferences sharedPref = getActivity().getSharedPreferences(this.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(this.getString(R.string.PREFERENCES_NICK_KEY), "");

        /* Creating settings list */
        ArrayList<String> settingsLabels = new ArrayList<String>();
        settingsLabels.add("label:" + getString(R.string.settings_myaccount) + " - " + nick + OptionsIDsDelimiter + "0");
        settingsLabels.add("" + getString(R.string.settings_observations) + OptionsIDsDelimiter + "1");
        settingsLabels.add("" + getString(R.string.settings_change_password) + OptionsIDsDelimiter + "2");
        settingsLabels.add("lowalpha:" + getString(R.string.settings_phone_number) + OptionsIDsDelimiter + "3");
        settingsLabels.add("label:" + getString(R.string.settings_application) + OptionsIDsDelimiter + "4");
        settingsLabels.add("checkbox:" + getString(R.string.settings_best_anonymous) + OptionsIDsDelimiter + "5");
        settingsLabels.add("checkbox:" + getString(R.string.settings_vibrations) + OptionsIDsDelimiter + "6");
        if (InitApp.gyroscopeAvailable)
            settingsLabels.add("checkbox:" + getString(R.string.settings_image_effects) + OptionsIDsDelimiter + "7");
        settingsLabels.add("" + getString(R.string.settings_rate) + OptionsIDsDelimiter + "8");
        settingsLabels.add("" + getString(R.string.settings_privacy) + OptionsIDsDelimiter + "9");
        settingsLabels.add("" + getString(R.string.settings_terms) + OptionsIDsDelimiter + "10");
        settingsLabels.add("" + getString(R.string.settings_logout) + OptionsIDsDelimiter + "11");

        /* Setting adapter */
        SettingsListViewAdapter adapter = new SettingsListViewAdapter(
                this,
                settingsLabels
        );
        final ListView settingsListView = view.findViewById(R.id.settingsListView);
        settingsListView.setAdapter(adapter);

        /* Adding down buttons listeners */
        view.findViewById(R.id.messengerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { OpenMessenger();
            }
        });
        view.findViewById(R.id.tumblrButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { OpenInstagram();
            }
        });
        view.findViewById(R.id.emailButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Email();
            }
        });
        view.findViewById(R.id.faqButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFaq();
            }
        });

        return view;

    }

    /* Observations tab open */
    public void OpenObservations() {

        View v = new View(getActivity());
        v.setTag("observations_r");
        ((MainActivity)getActivity()).OpenTab(v);

    }

    /* Changing password */
    public void ChangePassword() {

        View v = new View(getActivity());
        v.setTag("changepassword_r");
        ((MainActivity)getActivity()).OpenTab(v);

    }

    /* Best anonymous setting change */
    public void ChangeAnonymous(CheckBox checkbox) {

        if(checkbox.isChecked())
            SingletonNetwork.getInstance(getActivity()).BestNonAnonymous();
        else
            SingletonNetwork.getInstance(getActivity()).BestAnonymous();

    }

    /* Vibration setting change */
    public void ChangeVibration(CheckBox checkbox) {

        SharedPreferences.Editor editor = getActivity().getSharedPreferences(getString(R.string.PREFERENCES_TAG), 0).edit();
        checkbox.setChecked(!checkbox.isChecked());
        editor.putBoolean(getString(R.string.PREFERENCES_VIBRATION_NOTIFICATIONS), checkbox.isChecked());
        editor.apply();

    }

    /* Image effects setting change */
    public void ChangeImageEffects(CheckBox checkbox) {

        SharedPreferences.Editor editor = getActivity().getSharedPreferences(getString(R.string.PREFERENCES_TAG), 0).edit();
        checkbox.setChecked(!checkbox.isChecked());
        editor.putBoolean(getString(R.string.PREFERENCES_IMAGE_EFFECTS_ACTIVE), checkbox.isChecked());
        editor.apply();

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.panoramaGyroscopeListener.register(activity);
        }

    }

    /* Opening store */
    public void RateUs() {

        try {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
            }
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    /* Open Messenger */
    public void OpenMessenger() {

        try {
            getActivity().getPackageManager().getApplicationInfo("com.facebook.orca", 0);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://user/" + "218464992038772")));
        } catch (PackageManager.NameNotFoundException e1) {
            try {
                getActivity().getPackageManager().getApplicationInfo("com.facebook.katana", 0);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + "218464992038772")));
            } catch (PackageManager.NameNotFoundException e2) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + "218464992038772")));
            } catch (NullPointerException e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }
        } catch (NullPointerException e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    /* Open Tumblr */
    public void OpenInstagram() {

        try {
            getActivity().getPackageManager().getApplicationInfo("com.instagram.android", 0);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=flashbombapp")));
        } catch (PackageManager.NameNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/flashbombapp")));
        } catch (NullPointerException e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    /* Open email */
    public void Email() {

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "koszelew@gmail.com", null));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.settings_email_choose)));

    }

    /* FAQ tab */
    public void OpenFaq() {

        View v = new View(getActivity());
        v.setTag("faq_r");
        ((MainActivity) getActivity()).OpenTab(v);

    }

    /* Open Privacy policy */
    public void OpenPrivacyPolicy() {

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://flashbomb.org/DOCUMENTS/privacy.html")));

    }

    /* Open Terms */
    public void OpenTerms() {

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://flashbomb.org/DOCUMENTS/terms.html")));

    }

    /* Logging out */
    public void Logout() {

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getString(R.string.logout_confirmation_title));
        alertDialog.setMessage(getString(R.string.logout_confirmation_message));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /* Unsubscribe notifications */
                OneSignal.setSubscription(false);

                /* Clearing prefs */
                getActivity().getSharedPreferences(getString(R.string.PREFERENCES_TAG), 0).edit().clear().apply();

                /* Clearing upload photos / videos */
                File dir = new File(getActivity().getFilesDir() + "/Flashbomb");
                if (dir.isDirectory()) {
                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(dir, children[i]).delete();
                    }
                }

                /* Fade into darkness */
                Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) { getActivity().findViewById(R.id.settingsActivitySplash).setLayerType(View.LAYER_TYPE_NONE, null); }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                getActivity().findViewById(R.id.settingsActivitySplash).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.settingsActivitySplash).setLayerType(View.LAYER_TYPE_HARDWARE, null);
                getActivity().findViewById(R.id.settingsActivitySplash).startAnimation(fadeIn);

                /* Going to SplashScreen */
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent myIntent = new Intent(getActivity(), SplashScreenActivity.class);
                        startActivity(myIntent);
                        System.exit(0);
                    }
                }, fadeIn.getDuration());

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

    }

}
