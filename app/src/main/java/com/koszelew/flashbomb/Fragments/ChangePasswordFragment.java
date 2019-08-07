package com.koszelew.flashbomb.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.FlashbombToast;
import com.koszelew.flashbomb.Utils.Other.InitApp;

public class ChangePasswordFragment extends Fragment {

    /* EditTexts */
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;

    /* Initialization */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        ((TextView) view.findViewById(R.id.changePasswordButton)).setTypeface(InitApp.headersTypeface);

        oldPasswordEditText = view.findViewById(R.id.oldPassword);
        oldPasswordEditText.setTypeface(InitApp.headersTypeface);
        newPasswordEditText = view.findViewById(R.id.newPassword);
        newPasswordEditText.setTypeface(InitApp.headersTypeface);

        return view;

    }


    /* Change password button */
    public void ChangePassword() {

        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String currentPassword = sharedPref.getString(getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        final String oldPassword = oldPasswordEditText.getText().toString();
        final String newPassword = newPasswordEditText.getText().toString();

        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher oldPasswordMatcher = p.matcher(oldPassword);
        java.util.regex.Matcher newPasswordMatcher = p.matcher(newPassword);

        String hashedOldPassword = oldPassword;
        for(int i = 0; i < 1000; i++) {
            hashedOldPassword = SingletonNetwork.SHA256(hashedOldPassword);
        }

        if(!hashedOldPassword.equals(currentPassword)) {
            FlashbombToast.ShowError(getActivity(), getString(R.string.old_password_wrong), 1000);
            return;
        }

        if(newPassword.length() < 5) {
            FlashbombToast.ShowError(getActivity(), getString(R.string.new_password_short), 1000);
            return;
        }

        if(!newPasswordMatcher.matches()) {
            FlashbombToast.ShowError(getActivity(), getString(R.string.new_password_wrong_structure), 1000);
            return;
        }

        if(oldPassword.equals(newPassword)) {
            FlashbombToast.ShowError(getActivity(), getString(R.string.change_password_same_password), 1000);
            return;
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getString(R.string.change_password_confirmation_title));
        alertDialog.setMessage(getString(R.string.change_password_confirmation_content));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String hashedNewPassword = newPassword;
                for(int i = 0; i < 1000; i++) {
                    hashedNewPassword = SingletonNetwork.SHA256((hashedNewPassword));
                }
                SingletonNetwork.getInstance(getActivity()).ChangePassword(hashedNewPassword);

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
