package com.koszelew.flashbomb.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.SingletonFlashbombEntities;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;

import java.util.ArrayList;

public class ObservationsSearchListViewAdapter extends ArrayAdapter {

    /* Variables */
    private final MainActivity activity;
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<String> values;

    /* Constructor */
    public ObservationsSearchListViewAdapter(MainActivity activity, ArrayList<String> values) {

        super(activity.getApplicationContext(), R.layout.observations_search_listview_item, values);

        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.values = values;
        inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.observations_search_listview_item, parent, false);
        }

        /* Finding views */
        final TextView txt = convertView.findViewById(R.id.observationSearchText);
        final LottieAnimationView observeButton = convertView.findViewById(R.id.observationSearchButton);

        final String nick = values.get(position);

        /* Setting nick */
        txt.setText(values.get(position));
        txt.setTypeface(InitApp.contentsTypeface);

        /* Setting proper observe animation state */
        observeButton.setProgress(SingletonFlashbombEntities.getInstance(context).getObservationsManagementListViewArray().contains(nick) ? 1 : 0);

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