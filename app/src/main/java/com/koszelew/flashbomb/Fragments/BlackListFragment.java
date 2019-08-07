package com.koszelew.flashbomb.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Other.InitApp;

public class BlackListFragment extends Fragment {

    /* Initialization */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);

        /* Setting fonts */
        ((TextView) view.findViewById(R.id.blacklistTutorialTextView)).setTypeface(InitApp.contentsTypeface);

        /* Updating blacklist tab */
        //SingletonNetwork.getInstance(getActivity()).UpdateBlacklist();

        return view;

    }

}
