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

    // This data contains the content of the view (Text elements, etc.)
    private CalendarViewData mCalendarData;

    //region (Measurement Member Variables)
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    private int mContentWidth;
    private int mContentHeight;
    //endregion

    //region (Colors and Paints)
    private int mTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mTitleColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mDateTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mBackgroundColor = ContextCompat.getColor(getContext(), R.color.background);

    private TextPaint mTextPaint;
    private TextPaint mTitlePaint;
    private TextPaint mDateTextPaint;
    //endregion

    //region (Drawables)
    private Drawable mBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.background_calendar_view);
    //endregion

    //region (Dimensions)
    private float mTextSize = getResources().getDimension(R.dimen.labels_text_size);
    private float mDateTextSize = getResources().getDimension(R.dimen.labels_text_size);
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

        // region Create paint objects
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mTitlePaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mDateTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        // endregion

        mCalendarData = new CalendarViewData(getContext())
                .setTitle("January 2017", mTitlePaint);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CalendarView, defStyle, 0);

        //region Gather attributes from attrs
        //region Get colors from attributes
        mTextColor = a.getColor(
                R.styleable.CalendarView_android_textColor,
                mTextColor);

        mTitleColor = a.getColor(
                R.styleable.CalendarView_title_color,
                mTextColor);

        mDateTextColor = a.getColor(
                R.styleable.CalendarView_date_label_color,
                mTextColor);

        mBackgroundColor = a.getColor(
                R.styleable.CalendarView_backgroundColor,
                mBackgroundColor);
        //endregion

        //region Get dimensions from attributes

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = a.getDimension(
                R.styleable.CalendarView_android_textSize,
                mTextSize);

        mDateTextSize = a.getDimension(
                R.styleable.CalendarView_date_label_size,
                mDateTextSize);

        float titleMarginTop = a.getDimension(
                R.styleable.CalendarView_title_margin_top, 0);
        mCalendarData.getTitle().setMarginTop(titleMarginTop);

        float contentMarginTop = a.getDimension(
                R.styleable.CalendarView_content_margin_top, 0);
        mCalendarData.setContentMarginTop(contentMarginTop);

        //endregion

        //region Get drawables from attributes
        if (a.hasValue(R.styleable.CalendarView_android_background)) {
            mBackgroundDrawable = a.getDrawable(
                    R.styleable.CalendarView_android_background);
            mBackgroundDrawable.setCallback(this);
        }
        //endregion

        //region Get text from attributes
        if (a.hasValue(R.styleable.CalendarView_no_data_available_text)) {
            String text = a.getString(R.styleable.CalendarView_no_data_available_text);
            mCalendarData.setNoDataAvailableText(text, mTextPaint);
        }
        else {
            mCalendarData.setNoDataAvailableText("No Data Available", mTextPaint);
        }
        //endregion
        //endregion

        a.recycle();

        // Update TextPaint and text measurements from attributes
        invalidatePaddingAndContentMeasurements();
        invalidateTextPaintAndMeasurements();
    }

    //region Make measurements {}
    private void invalidatePaddingAndContentMeasurements() {
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

        mTitlePaint.setTextSize(mTextSize);
        mTitlePaint.setColor(mTitleColor);

        mDateTextPaint.setTextSize(mDateTextSize);
        mDateTextPaint.setColor(mDateTextColor);

        if (mCalendarData.getNoDataAvailableText() != null)
            mCalendarData.getNoDataAvailableText().setTextPaint(mTextPaint);

        if (mCalendarData.getTitle() != null)
            mCalendarData.getTitle().setTextPaint(mTitlePaint);

        mCalendarData.setDayNameHeaderPaint(mDateTextPaint);

        mCalendarData.makeMeasurements();
    }
    //endregion // Make measurements

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        invalidatePaddingAndContentMeasurements();

        drawBackground(canvas);

        drawCalendarTitle(canvas);

        drawDateNames(canvas);

//        drawNoDataText(canvas);

//        drawDateBackgrounds();
//        drawDateNumbers();
    }


    public void drawBackground(Canvas canvas) {
        // Draw the background color
        canvas.drawColor(getBackgroundColor());

        // Draw the background drawable
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(mPaddingLeft, mPaddingTop,
                    mPaddingLeft + mContentWidth, mPaddingTop + mContentHeight);
            mBackgroundDrawable.draw(canvas);
        }
    }

    public void drawNoDataText(Canvas canvas) {
        TextElement text = mCalendarData.getNoDataAvailableText();

        // Draw the text
        text.draw(canvas,
                mPaddingLeft + (mContentWidth - text.getWidth()) / 2,
                mPaddingTop + (mContentHeight + text.getHeight()) / 2);
    }

    private void drawCalendarTitle(Canvas canvas) {
        TextElement text = mCalendarData.getTitle();

        if (text != null) {
            // Draw the text
            text.draw(canvas,
                    mPaddingLeft + (mContentWidth - text.getWidth()) / 2,
                    mPaddingTop + text.getHeight());
        }
    }

    private void drawDateNames(Canvas canvas) {
        TextElement dayNames[] = mCalendarData.getDayNameTextElements();

        float textHeight = mCalendarData.getDayNameTextElements()[0].getHeight();
//        float textWidth = mCalendarData.getDayNameTextElements()[0].getWidth();

        float contentOffset = mContentWidth * 0.082f; // 8.2% of the total width
        float calendarWidth = mContentWidth - (contentOffset * 2); // Total width minus offset on either side

        float totalLabelSpace = 0;
        for (TextElement dayText : mCalendarData.getDayNameTextElements())
            totalLabelSpace += dayText.getWidth();

        float columnSpacer = (calendarWidth - totalLabelSpace) / ((float) dayNames.length - 1);

        float y = mCalendarData.getTitle().getLastYValue() + textHeight + mCalendarData.getContentMarginTop();
        float x = contentOffset;

        for (int i = 0; i < dayNames.length; i++) {
            if (i > 0) {
                TextElement lastElement = mCalendarData.getDayNameTextElements()[i - 1];
                x = lastElement.getLastXValue() + lastElement.getWidth() + columnSpacer;
            }
            dayNames[i].draw(canvas, x, y);
        }
    }

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
}
