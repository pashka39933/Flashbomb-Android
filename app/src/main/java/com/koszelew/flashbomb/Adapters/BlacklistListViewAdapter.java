package com.koszelew.flashbomb.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
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

public class BlacklistListViewAdapter extends ArrayAdapter {

    /* Variables */
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<Pair<String, Integer>> items;

    /* Constructor */
    public BlacklistListViewAdapter(Context ctx, ArrayList<Pair<String, Integer>> values) {

        super(ctx, R.layout.blacklist_listview_item, values);

        this.context = ctx;

        this.items = values;

        inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.blacklist_listview_item, parent, false);
        }

        /* Finding views */
        final TextView txt = convertView.findViewById(R.id.blacklistText);
        final LottieAnimationView observeButton = convertView.findViewById(R.id.blacklistButton);

        final String nick = items.get(position).first;
        final int animationProgress = items.get(position).second;
        observeButton.setProgress(animationProgress);
        txt.setTextColor(animationProgress == 0 ? context.getResources().getColor(R.color.blue_blacklist) : context.getResources().getColor(R.color.red_blacklist));

        /* Setting nick */
        txt.setText(nick);
        txt.setTypeface(InitApp.contentsTypeface);

        /* Setting observe animation listener */
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (observeButton.getProgress() == 1) {

                    SingletonNetwork.getInstance(context).Unhide(nick);
                    observeButton.setSpeed(-1);
                    observeButton.playAnimation();
                    items.set(position, new Pair<>(nick, 0));
                    txt.setTextColor(context.getResources().getColor(R.color.blue_blacklist));

                } else {

                    SingletonNetwork.getInstance(context).Hide(nick);
                    observeButton.setSpeed(1);
                    observeButton.playAnimation();
                    items.set(position, new Pair<>(nick, 1));
                    txt.setTextColor(context.getResources().getColor(R.color.red_blacklist));

                }

            }

        });

        return convertView;
    }

}