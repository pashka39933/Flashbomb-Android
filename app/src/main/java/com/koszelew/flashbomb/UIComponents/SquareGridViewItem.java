package com.koszelew.flashbomb.UIComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.koszelew.flashbomb.Utils.Other.InitApp;

public class SquareGridViewItem extends RelativeLayout {

    public SquareGridViewItem(Context context) {
        super(context);
    }

    public SquareGridViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridViewItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(Math.round((float) View.MeasureSpec.getSize(widthMeasureSpec) * 0.689f * InitApp.screenAspect), View.MeasureSpec.getMode(widthMeasureSpec)));
    }
}