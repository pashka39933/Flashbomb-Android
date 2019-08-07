package com.koszelew.flashbomb.UIComponents;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.koszelew.flashbomb.Listeners.PanoramaGyroscopeListener;

public class PanoramaImageView extends android.support.v7.widget.AppCompatImageView {

    // Image's scroll orientation
    public final static byte ORIENTATION_NONE = -1;
    public final static byte ORIENTATION_HORIZONTAL = 0;
    public final static byte ORIENTATION_VERTICAL = 1;
    private byte mOrientation = ORIENTATION_NONE;

    // If true, the image scroll left(top) when the device clockwise rotate along y-axis(x-axis).
    private boolean mInvertScrollDirection;

    // Image's width and height
    private int mDrawableWidth;
    private int mDrawableHeight;

    // View's width and height
    private int mWidth;
    private int mHeight;

    // Image's offset from initial state(center in the view).
    private float mMaxOffset;

    // The scroll progress.
    private float mProgress;

    // Show scroll bar or not
    private boolean mEnableScrollbar;

    // The paint to draw scrollbar
    private Paint mScrollbarPaint;

    // Observe scroll state
    private OnPanoramaScrollListener mOnPanoramaScrollListener;

    public PanoramaImageView(Context context) {
        this(context, null, 0);
    }

    public PanoramaImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanoramaImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setScaleType(ScaleType.CENTER_CROP);

        mInvertScrollDirection = false;
        mEnableScrollbar = false;

        if (mEnableScrollbar) {
            initScrollbarPaint();
        }
    }

    private void initScrollbarPaint() {
        mScrollbarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScrollbarPaint.setColor(Color.WHITE);
        mScrollbarPaint.setStrokeWidth(dp2px(1.5f));
    }

    public void setGyroscopeObserver(PanoramaGyroscopeListener observer) {
        if (observer != null) {
            observer.addPanoramaImageView(this);
        }
    }

    public void updateProgress(float progress) {
        mProgress = mInvertScrollDirection ? -progress : progress;
        invalidate();
        if (mOnPanoramaScrollListener != null) {
            mOnPanoramaScrollListener.onScrolled(this, -mProgress);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        mHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        if (getDrawable() != null) {
            mDrawableWidth = getDrawable().getIntrinsicWidth();
            mDrawableHeight = getDrawable().getIntrinsicHeight();

            if (mDrawableWidth * mHeight > mDrawableHeight * mWidth) {
                mOrientation = ORIENTATION_HORIZONTAL;
                float imgScale = (float) mHeight / (float) mDrawableHeight;
                mMaxOffset = Math.abs((mDrawableWidth * imgScale - mWidth) * 0.5f);
            } else if(mDrawableWidth * mHeight < mDrawableHeight * mWidth) {
                mOrientation = ORIENTATION_VERTICAL;
                float imgScale = (float) mWidth / (float) mDrawableWidth;
                mMaxOffset = Math.abs((mDrawableHeight * imgScale - mHeight) * 0.5f);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (getDrawable() == null || isInEditMode()) {
            super.onDraw(canvas);
            return;
        }

        // Draw image
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            float currentOffsetX = mMaxOffset * mProgress;
            canvas.save();
            canvas.translate(currentOffsetX, 0);
            super.onDraw(canvas);
            canvas.restore();
        } else if (mOrientation == ORIENTATION_VERTICAL) {
            float currentOffsetY = mMaxOffset * mProgress;
            canvas.save();
            canvas.translate(0, currentOffsetY);
            super.onDraw(canvas);
            canvas.restore();
        }

        // Draw scrollbar
        if (mEnableScrollbar) {
            switch (mOrientation) {
                case ORIENTATION_HORIZONTAL: {
                    float barBgWidth = mWidth * 0.9f;
                    float barWidth = barBgWidth * mWidth / mDrawableWidth;

                    float barBgStartX = (mWidth - barBgWidth) / 2;
                    float barBgEndX = barBgStartX + barBgWidth;
                    float barStartX = barBgStartX + (barBgWidth - barWidth) / 2 * (1 - mProgress);
                    float barEndX = barStartX + barWidth;
                    float barY = mHeight * 0.95f;

                    mScrollbarPaint.setAlpha(100);
                    canvas.drawLine(barBgStartX, barY, barBgEndX, barY, mScrollbarPaint);
                    mScrollbarPaint.setAlpha(255);
                    canvas.drawLine(barStartX, barY, barEndX, barY, mScrollbarPaint);
                    break;
                }
                case ORIENTATION_VERTICAL: {
                    float barBgHeight = mHeight * 0.9f;
                    float barHeight = barBgHeight * mHeight / mDrawableHeight;

                    float barBgStartY = (mHeight - barBgHeight) / 2;
                    float barBgEndY = barBgStartY + barBgHeight;
                    float barStartY = barBgStartY + (barBgHeight - barHeight) / 2 * (1 - mProgress);
                    float barEndY = barStartY + barHeight;
                    float barX = mWidth * 0.95f;

                    mScrollbarPaint.setAlpha(100);
                    canvas.drawLine(barX, barBgStartY, barX, barBgEndY, mScrollbarPaint);
                    mScrollbarPaint.setAlpha(255);
                    canvas.drawLine(barX, barStartY, barX, barEndY, mScrollbarPaint);
                    break;
                }
            }
        }

    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public void setInvertScrollDirection(boolean invert) {
        if (mInvertScrollDirection != invert) {
            mInvertScrollDirection = invert;
        }
    }

    public boolean isInvertScrollDirection() {
        return mInvertScrollDirection;
    }

    public void setEnableScrollbar(boolean enable) {
        if (mEnableScrollbar != enable){
            mEnableScrollbar = enable;
            if (mEnableScrollbar) {
                initScrollbarPaint();
            } else {
                mScrollbarPaint = null;
            }
        }
    }

    public boolean isScrollbarEnabled() {
        return mEnableScrollbar;
    }

    public byte getOrientation() {
        return mOrientation;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        /**
         * Do nothing because PanoramaImageView only
         * supports {@link scaleType.CENTER_CROP}
         */
    }

    /**
     * Interface definition for a callback to be invoked when the image is scrolling
     */
    public interface OnPanoramaScrollListener {
        /**
         * Call when the image is scrolling
         *
         * @param view the panoramaImageView shows the image
         *
         * @param offsetProgress value between (-1, 1) indicating the offset progress.
         *                 -1 means the image scrolls to show its left(top) bound,
         *                 1 means the image scrolls to show its right(bottom) bound.
         */
        void onScrolled(PanoramaImageView view, float offsetProgress);
    }

    public void setOnPanoramaScrollListener(OnPanoramaScrollListener listener) {
        mOnPanoramaScrollListener = listener;
    }
}