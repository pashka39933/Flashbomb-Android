package com.koszelew.flashbomb.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Other.InitApp;

public class ProfileFragment extends Fragment {

    /* Initialization */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        /* Setting nick */
        final SharedPreferences sharedPref = getActivity().getSharedPreferences(getActivity().getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String profileNick = "@" + sharedPref.getString(getActivity().getString(R.string.PREFERENCES_NICK_KEY), "");
        TextView profileNickTextView = view.findViewById(R.id.profileNickText);
        profileNickTextView.setTypeface(InitApp.headersTypeface);
        profileNickTextView.setText(profileNick);
        if (profileNick.length() > 12) {
            float sp = getResources().getDimension(R.dimen.search_textview) / getResources().getDisplayMetrics().scaledDensity;
            profileNickTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp - (profileNick.length() - 12));
        }

        /* Updating profile tab */
        //SingletonNetwork.getInstance(getActivity()).UpdateProfileTab();

        return view;

    }

}
