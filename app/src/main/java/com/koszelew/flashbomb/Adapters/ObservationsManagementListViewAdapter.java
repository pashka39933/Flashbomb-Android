package com.koszelew.flashbomb.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;

import java.util.ArrayList;

public class ObservationsManagementListViewAdapter extends ArrayAdapter {

    /* Variables */
    private final Context context;
    private final LayoutInflater inflater;
    private ArrayList<String> nicks = new ArrayList<>();

    /* Constructor */
    public ObservationsManagementListViewAdapter(Activity activity, ArrayList<String> values) {

        super(activity, R.layout.observations_management_listview_item, values);

        this.context = activity;
        this.nicks = values;
        inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.observations_management_listview_item, parent, false);
        }

        /* Finding views */
        final TextView txt = convertView.findViewById(R.id.observationManagementText);
        final LottieAnimationView observeButton = convertView.findViewById(R.id.observationManagementButton);

        final String nick = nicks.get(position);
        observeButton.setProgress(1);

        /* Setting nick */
        txt.setText(nick);
        txt.setTypeface(InitApp.contentsTypeface);

        /* Setting observe animation listener */
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (observeButton.getProgress() == 1) {

                    SingletonNetwork.getInstance(context).Unobserve(nick);
                    observeButton.setSpeed(-1);
                    observeButton.playAnimation();

                } else {

                    SingletonNetwork.getInstance(context).Observe(nick);
                    observeButton.setSpeed(1);
                    observeButton.playAnimation();

                }

            }

        });

        return convertView;
    }

}