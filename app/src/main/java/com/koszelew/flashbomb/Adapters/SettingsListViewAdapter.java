package com.koszelew.flashbomb.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.koszelew.flashbomb.Fragments.SettingsFragment;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Other.InitApp;

import java.util.ArrayList;

public class SettingsListViewAdapter extends ArrayAdapter {

    /* Variables */
    private final SettingsFragment activity;
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<String> values;

    /* Constructor */
    public SettingsListViewAdapter(SettingsFragment activity, ArrayList<String> values) {

        super(activity.getActivity(), R.layout.settings_listview_item, values);

        this.activity = activity;
        this.context = activity.getActivity();
        this.values = values;
        inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.settings_listview_item, parent, false);
        }

        /* Finding view */
        final TextView txt = convertView.findViewById(R.id.settingsText);
        final CheckBox checkbox = convertView.findViewById(R.id.settingsCheckbox);
        txt.setVisibility(View.GONE);
        checkbox.setVisibility(View.GONE);

        /* Determining color of TextView */
        if (values.get(position).contains("label:")) {

            txt.setTextColor((context.getResources().getColor(R.color.yellow)));
            txt.setTypeface(InitApp.headersTypeface);
            txt.setAlpha(1f);
            txt.setVisibility(View.VISIBLE);


        } else if (values.get(position).contains("lowalpha:")) {

            txt.setTextColor((context.getResources().getColor(R.color.black)));
            txt.setTypeface(InitApp.clickablesTypeface);
            txt.setAlpha(0.25f);
            txt.setVisibility(View.VISIBLE);

        } else if (values.get(position).contains("checkbox:")) {

            txt.setTextColor((context.getResources().getColor(R.color.black)));
            txt.setTypeface(InitApp.clickablesTypeface);
            txt.setAlpha(1f);
            txt.setVisibility(View.VISIBLE);
            checkbox.setVisibility(View.VISIBLE);


        } else {

            txt.setTextColor((context.getResources().getColor(R.color.black)));
            txt.setTypeface(InitApp.clickablesTypeface);
            txt.setAlpha(1f);
            txt.setVisibility(View.VISIBLE);
        }

        /* Removing options */
        txt.setText(values.get(position).
                replace("label:", "").
                replace("lowalpha:", "").
                replace("checkbox:", "").
                split(activity.OptionsIDsDelimiter)[0]
        );

        /* Adding settings click listeners */
        switch (Integer.parseInt(values.get(position).split(activity.OptionsIDsDelimiter)[1])) {
            case 0:
                break;
            case 1:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.OpenObservations();
                    }
                });
                break;
            case 2:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.ChangePassword();
                    }
                });
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                checkbox.setChecked(context.getSharedPreferences(context.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE).getBoolean(context.getString(R.string.PREFERENCES_ANONYMOUS_BEST), true));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.ChangeAnonymous(checkbox);
                    }
                });
                break;
            case 6:
                checkbox.setChecked(context.getSharedPreferences(context.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE).getBoolean(context.getString(R.string.PREFERENCES_VIBRATION_NOTIFICATIONS), false));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.ChangeVibration(checkbox);
                    }
                });
                break;
            case 7:
                checkbox.setChecked(context.getSharedPreferences(context.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE).getBoolean(context.getString(R.string.PREFERENCES_IMAGE_EFFECTS_ACTIVE), false));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.ChangeImageEffects(checkbox);
                    }
                });
                break;
            case 8:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.RateUs();
                    }
                });
                break;
            case 9:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.OpenPrivacyPolicy();
                    }
                });
                break;
            case 10:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.OpenTerms();
                    }
                });
                break;
            case 11:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { activity.Logout();
                    }
                });
                break;
        }

        return convertView;
    }

}