package com.example.brandon.habitlogger.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.example.brandon.habitlogger.R;

/**
 * A view to represent entry data of a habit in a month-long calendar space.
 */

public class CalendarView extends View {
    private String mNoDataAvailableString = getResources().getString(R.string.no_data_available);
    private float mTextSize = getResources().getDimension(R.dimen.labels_text_size);
    private int mTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mBackgroundColor = ContextCompat.getColor(getContext(), R.color.background);
    private Drawable mBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.background_calendar_view);

    private TextPaint mTextPaint;

    //region (Measurement Member Variables)
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    private int mContentWidth;
    private int mContentHeight;

    private float mTextWidth;
    private float mTextHeight;
    //endregion

    //region Constructors {}
    public CalendarView(Context context) {
        super(context);
        init(null, 0);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    //endregion

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CalendarView, defStyle, 0);

        //region Gather attributes from attrs
        if(a.hasValue(R.styleable.CalendarView_no_data_available_text)){
            mNoDataAvailableString = a.getString(
                    R.styleable.CalendarView_no_data_available_text);
        }

        mTextColor = a.getColor(
                R.styleable.CalendarView_android_textColor,
                mTextColor);

        mBackgroundColor = a.getColor(
                R.styleable.CalendarView_backgroundColor,
                mBackgroundColor);

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = a.getDimension(
                R.styleable.CalendarView_android_textSize,
                mTextSize);

        if (a.hasValue(R.styleable.CalendarView_android_background)) {
            mBackgroundDrawable = a.getDrawable(
                    R.styleable.CalendarView_android_background);
            mBackgroundDrawable.setCallback(this);
        }
        //endregion

        a.recycle();

//        setElevation(getResources().getDimension(R.dimen.cardview_default_elevation));

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
        updatePaddingAndCalculateContentWidthAndHeight();
    }

    //region // Make measurements
    private void updatePaddingAndCalculateContentWidthAndHeight(){
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();

        mContentWidth = getWidth() - mPaddingLeft - mPaddingRight;
        mContentHeight = getHeight() - mPaddingTop - mPaddingBottom;
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextWidth = mTextPaint.measureText(mNoDataAvailableString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }
    //endregion // Make measurements

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updatePaddingAndCalculateContentWidthAndHeight();

        // Draw the background color
        canvas.drawColor(getBackgroundColor());

        // Draw the background drawable
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(mPaddingLeft, mPaddingTop,
                    mPaddingLeft + mContentWidth, mPaddingTop + mContentHeight);
            mBackgroundDrawable.draw(canvas);
        }

        // Draw the text.
        canvas.drawText(mNoDataAvailableString,
                mPaddingLeft + (mContentWidth - mTextWidth) / 2,
                mPaddingTop + (mContentHeight + mTextHeight) / 2,
                mTextPaint);
    }

    //region // Getters and Setters

    //region // Getters

    /**
     * @return The color used for text.
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * @return The size of the text.
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * @param color The color to be used for text.
     */
    public void setTextColor(int color) {
        mTextColor = color;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * @return The background drawable.
     */
    public Drawable getExampleDrawable() {
        return mBackgroundDrawable;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    //endregion // Getters

    //region // Setters

    /**
     * @param textSize The size to be used for text.
     */
    public void setExampleDimension(float textSize) {
        mTextSize = textSize;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * @param backgroundDrawable The background drawable to drawn above the background color.
     */
    public void setExampleDrawable(Drawable backgroundDrawable) {
        mBackgroundDrawable = backgroundDrawable;
    }

    /**
     * @param color The color to be drawn under the background drawable.
     */
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        invalidateTextPaintAndMeasurements();
    }

    //endregion // Setters

    //endregion // Getters and Setters
}
