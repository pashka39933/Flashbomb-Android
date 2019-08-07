package com.koszelew.flashbomb.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koszelew.flashbomb.R;

/* Class representing single fragment of login/register ViewPager */
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int fragmentID = getArguments().getInt("FragmentID", 0);
        ViewGroup rootView;

        /* Layout choosing according to FragmentID passed */
        switch (fragmentID) {

            case 0:
                rootView = (ViewGroup) inflater.inflate(
                        R.layout.home_viewpager_fragment0, container, false);
                break;
            case 1:
                rootView = (ViewGroup) inflater.inflate(
                        R.layout.home_viewpager_fragment1, container, false);
                break;
            case 2:
                rootView = (ViewGroup) inflater.inflate(
                        R.layout.home_viewpager_fragment2, container, false);
                break;
            case 3:
                rootView = (ViewGroup) inflater.inflate(
                        R.layout.home_viewpager_fragment3, container, false);
                break;
            default:
                rootView = (ViewGroup) inflater.inflate(
                        R.layout.home_viewpager_fragment0, container, false);
                break;

        }

        return rootView;
    }

}
