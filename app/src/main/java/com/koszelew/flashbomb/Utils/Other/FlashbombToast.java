package com.koszelew.flashbomb.Utils.Other;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.koszelew.flashbomb.R;

public class FlashbombToast {

    public static void ShowError(Activity ctx, String text, long showTime) {

        LayoutInflater inflater = ctx.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout_error, (ViewGroup) ctx.findViewById(R.id.toastLayout));
        ((TextView) layout.findViewById(R.id.toastMsg)).setText(text);
        ((TextView) layout.findViewById(R.id.toastMsg)).setTypeface(InitApp.contentsTypeface);
        final Toast toast = new Toast(ctx);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, showTime);

    }

    public static void ShowInfo(Activity ctx, String text, long showTime) {

        LayoutInflater inflater = ctx.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout_info, (ViewGroup) ctx.findViewById(R.id.toastLayout));
        ((TextView) layout.findViewById(R.id.toastMsg)).setText(text);
        ((TextView) layout.findViewById(R.id.toastMsg)).setTypeface(InitApp.contentsTypeface);
        final Toast toast = new Toast(ctx);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, showTime);

    }

}
