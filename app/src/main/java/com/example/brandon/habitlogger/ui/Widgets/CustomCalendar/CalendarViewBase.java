package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.example.brandon.habitlogger.R;

/**
 * Created by Brandon on 4/3/2017.
 * Base class for all custom calendar views
 */

public abstract class CalendarViewBase extends View {

    //region ( ---- Member attributes ---- )

    protected CalendarViewDataBase mCalendarData;
    protected CalendarViewModelBase mModel;

    //region (Measurements)
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    protected int mContentWidth;
    protected int mContentHeight;
    //endregion -- end --

    //region (Dimensions)
    private float mTextSize = getResources().getDimension(R.dimen.labels_text_size);
    private float mDateTextSize = getResources().getDimension(R.dimen.labels_text_size);
    private float mDateRadius = 48;
    //endregion -- end --

    //region (Colors and Paints)
    private int mTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mTitleColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    protected int mDateTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mBackgroundColor = ContextCompat.getColor(getContext(), R.color.background1);
    private int mCalendarBackgroundColor = ContextCompat.getColor(getContext(), R.color.defaultCardViewBackground);
    private int mDateElementColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    protected int mEmptyDateColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
    protected int mCurrentDayColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryVeryLight);

    private TextPaint mBackgroundPaint;
    protected TextPaint mCalendarBackgroundPaint;
    private TextPaint mTextPaint;
    protected TextPaint mTitlePaint;
    protected TextPaint mDateTextPaint;
    protected TextPaint mDateElementPaint;
    protected TextPaint mEmptyDatePaint;
    protected TextPaint mCurrentDayPaint;
    //endregion -- end --

    //region (Drawables)
    private Drawable mBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.background_simple_rectangle);
    //endregion -- end --

    //endregion ( ---- end ---- )

    //region Constructors {}
    public CalendarViewBase(Context context) {
        super(context);
        init(null, 0);
    }

    public CalendarViewBase(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CalendarViewBase(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public CalendarViewBase(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }
    //endregion

    //region Methods responsible for initializing the view

    protected abstract CalendarViewDataBase onCreateCalenderModel();

    private void init(AttributeSet attrs, int defStyle) {

        mCalendarData = onCreateCalenderModel();

        createPaintObjects();

        // Gather attributes
        final TypedArray attributes = getContext().obtainStyledAttributes
                (attrs, R.styleable.CalendarView, defStyle, 0);

        gatherAttributes(attributes);

        attributes.recycle();

        // Update TextPaint and text measurements from attributes
        invalidatePaddingAndContentMeasurements();
        invalidateTextPaintAndMeasurements();
    }

    protected abstract void onGatherAttributes(TypedArray attributes);

    private void gatherAttributes(TypedArray attributes) {
        onGatherAttributes(attributes);

        gatherColorAttributes(attributes);
        gatherDimensionAttributes(attributes);
        gatherDrawableAttributes(attributes);
        gatherTextAttributes(attributes);

    }

    protected abstract void onCreatePaintObjects();

    private void createPaintObjects() {
        onCreatePaintObjects();
        mBackgroundPaint = new TextPaint();

        mDateElementPaint = new TextPaint();
        mDateElementPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mDateElementPaint.setTextAlign(Paint.Align.CENTER);

        mEmptyDatePaint = new TextPaint();
        mEmptyDatePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mEmptyDatePaint.setTextAlign(Paint.Align.LEFT);

        mCalendarBackgroundPaint = new TextPaint();
        mCalendarBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mTitlePaint = new TextPaint();
        mTitlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setTextAlign(Paint.Align.LEFT);

        mDateTextPaint = new TextPaint();
        mDateTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mDateTextPaint.setTextAlign(Paint.Align.LEFT);

        mCurrentDayPaint = new TextPaint();
        mCurrentDayPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    private void gatherColorAttributes(TypedArray attributes) {
        mTextColor = attributes.getColor(
                R.styleable.CalendarView_android_textColor,
                mTextColor);

        mTitleColor = attributes.getColor(
                R.styleable.CalendarView_title_color,
                mTitleColor);

        mDateTextColor = attributes.getColor(
                R.styleable.CalendarView_date_label_color,
                mDateTextColor);

        mBackgroundColor = attributes.getColor(
                R.styleable.CalendarView_backgroundColor,
                mBackgroundColor);

        mDateElementColor = attributes.getColor(
                R.styleable.CalendarView_date_element_color,
                mDateElementColor);

        mEmptyDateColor = attributes.getColor(
                R.styleable.CalendarView_empty_date_color,
                mEmptyDateColor);

        mCurrentDayColor = attributes.getColor(
                R.styleable.CalendarView_current_day_highlight_color,
                mCurrentDayColor);
    }

    private void gatherDimensionAttributes(TypedArray attributes) {
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = attributes.getDimension(
                R.styleable.CalendarView_android_textSize,
                mTextSize);

        mDateTextSize = attributes.getDimension(
                R.styleable.CalendarView_date_label_size,
                mDateTextSize);

        float titleMarginTop = attributes.getDimension(
                R.styleable.CalendarView_title_margin_top, 0);
        mCalendarData.getTitle().setMarginTop(titleMarginTop);

        float contentMarginTop = attributes.getDimension(
                R.styleable.CalendarView_content_margin_top, 0);
        mCalendarData.setContentMarginTop(contentMarginTop);

        mDateRadius = attributes.getDimension(
                R.styleable.CalendarView_date_circle_radius,
                mDateRadius);
    }

    private void gatherDrawableAttributes(TypedArray attributes) {
        if (attributes.hasValue(R.styleable.CalendarView_android_background)) {
            mBackgroundDrawable = attributes.getDrawable
                    (R.styleable.CalendarView_android_background);

            if (mBackgroundDrawable != null)
                mBackgroundDrawable.setCallback(this);
        }
    }

    private void gatherTextAttributes(TypedArray attributes) {
        if (attributes.hasValue(R.styleable.CalendarView_no_data_available_text)) {
            String text = attributes.getString(R.styleable.CalendarView_no_data_available_text);
            mCalendarData.setNoDataAvailableText(text, mTextPaint);
        }
        else
            mCalendarData.setNoDataAvailableText("No Data Available", mTextPaint);
    }

    //endregion

    //region Methods responsible for handling invalidation and measurements
    protected void invalidatePaddingAndContentMeasurements() {
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();

        mContentWidth = getWidth() - mPaddingLeft - mPaddingRight;
        mContentHeight = getHeight() - mPaddingTop - mPaddingBottom;
    }

    /**
     * This is where you should:
     * reset paint colors, text sizes, and make calls to measurement methods
     */
    protected abstract void onInvalidateTextPaintAndMeasurements();

    protected void invalidateTextPaintAndMeasurements() {
        onInvalidateTextPaintAndMeasurements();

        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mTitlePaint.setTextSize(mTextSize);
        mTitlePaint.setColor(mTitleColor);

        mDateTextPaint.setTextSize(mDateTextSize);
        mDateTextPaint.setColor(mDateTextColor);

        mDateElementPaint.setTextSize(mDateTextSize);
        mDateElementPaint.setColor(mDateElementColor);

        mEmptyDatePaint.setColor(mEmptyDateColor);

        mCurrentDayPaint.setColor(mCurrentDayColor);

        mCalendarBackgroundPaint.setColor(mCalendarBackgroundColor);

        mBackgroundPaint.setColor(mBackgroundColor);

        if (mCalendarData.getNoDataAvailableText() != null)
            mCalendarData.getNoDataAvailableText().setTextPaint(mTextPaint);

        if (mCalendarData.getTitle() != null)
            mCalendarData.getTitle().setTextPaint(mTitlePaint);

        mCalendarData.setDayNameHeaderPaint(mDateTextPaint);
        mCalendarData.setDateElementsPaint(mDateTextPaint);

        mCalendarData.setDateElementsRadius(mDateRadius);

        mCalendarData.makeMeasurements();
    }
    //endregion -- end --

    //region Methods responsible for drawing the view
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        invalidateTextPaintAndMeasurements();

        invalidatePaddingAndContentMeasurements();

        drawBackground(canvas);

        drawCalendarTitle(canvas);

        drawDateNames(canvas);

        onDrawOverBase(canvas);
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

    public abstract void onDrawOverBase(Canvas canvas);
    //endregion -- end --

    //region Setters {}

    /**
     * @param color The color to be drawn under the background drawable.
     */
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * @param color The color to be used for text.
     */
    public void setTextColor(int color) {
        mTextColor = color;
        invalidateTextPaintAndMeasurements();
    }

    public void setModel(CalendarViewModelBase model) {
        this.mModel = model;
        if (mCalendarData != null) {
            mCalendarData.getTitle().setText(model.getMonthTitle());
            invalidateTextPaintAndMeasurements();
            invalidatePaddingAndContentMeasurements();
            invalidate();
        }
    }
    //endregion -- end --

    //region Getters {}
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

    public int getBackgroundColor() {
        return mBackgroundColor;
    }
    //endregion -- end --

}
