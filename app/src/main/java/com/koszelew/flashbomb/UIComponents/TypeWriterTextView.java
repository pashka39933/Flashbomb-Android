package com.koszelew.flashbomb.UIComponents;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

public class TypeWriterTextView extends android.support.v7.widget.AppCompatTextView {

    private CharSequence mText;
    private CharSequence textTmp = "";
    private int mIndex;
    private long mDelay = 500;


    public TypeWriterTextView(Context context) {
        super(context);
    }

    public TypeWriterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            String txt = textTmp.toString() + mText.subSequence(0, mIndex++);
            setText(txt);
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void stopAnimateText() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void animateText(CharSequence text) {
        textTmp = getText();
        mText = text;
        mIndex = 0;

        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }

    public long getCharacterDelay() {
        return mDelay;
    }
}