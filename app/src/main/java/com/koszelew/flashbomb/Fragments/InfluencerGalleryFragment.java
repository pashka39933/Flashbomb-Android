package com.koszelew.flashbomb.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koszelew.flashbomb.R;

public class InfluencerGalleryFragment extends Fragment {

    /* Initialization */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_influencer_gallery, container, false);

        return view;

    }

}
