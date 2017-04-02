package com.example.brandon.habitlogger.ui.CalendarView;

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

import java.util.Calendar;

/**
 * A rootView to represent entry data of a habit in a month-long calendar space.
 */

public class CalendarView extends View {

    // This data contains the content of the rootView (Text elements, etc.)
    private CalendarViewData mCalendarData;
    CalendarViewMonthModel model;
    public static final int NUMBER_OF_CELLS = 42;

    //region (Measurement Member Variables)
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    private int mContentWidth;
    private int mContentHeight;
    //endregion

    //region (Colors and Paints)
    private int mStreakColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mTitleColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mDateTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private int mBackgroundColor = ContextCompat.getColor(getContext(), R.color.background);
    private int mCalendarBackgroundColor = ContextCompat.getColor(getContext(), R.color.habitCard);
    private int mDateElementColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);

    private TextPaint mBackgroundPaint;
    private TextPaint mCalendarBackgroundPaint;
    private TextPaint mTextPaint;
    private TextPaint mTitlePaint;
    private TextPaint mDateTextPaint;
    private TextPaint mStreakPaint;
    private TextPaint mDateElementPaint;
    //endregion

    //region (Drawables)
    private Drawable mBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.background_calendar_view);
    //endregion

    //region (Dimensions)
    private float mTextSize = getResources().getDimension(R.dimen.labels_text_size);
    private float mDateTextSize = getResources().getDimension(R.dimen.labels_text_size);
    private float mDateRadius = 48;
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
        mStreakPaint = new TextPaint();

        mBackgroundPaint = new TextPaint();

        mDateElementPaint = new TextPaint();
        mDateElementPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mDateElementPaint.setTextAlign(Paint.Align.LEFT);

        mCalendarBackgroundPaint = new TextPaint();

        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mTitlePaint = new TextPaint();
        mTitlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setTextAlign(Paint.Align.LEFT);

        mDateTextPaint = new TextPaint();
        mDateTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mDateTextPaint.setTextAlign(Paint.Align.LEFT);
        // endregion


        mCalendarData = new CalendarViewData()
                .setTitle("January 2017", mTitlePaint);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CalendarView, defStyle, 0);

        //region Gather attributes from attrs
        //region Get colors from attributes
        mStreakColor = a.getColor(
                R.styleable.CalendarView_date_circle_streak_color,
                mStreakColor);

        mTextColor = a.getColor(
                R.styleable.CalendarView_android_textColor,
                mTextColor);

        mTitleColor = a.getColor(
                R.styleable.CalendarView_title_color,
                mTitleColor);

        mDateTextColor = a.getColor(
                R.styleable.CalendarView_date_label_color,
                mDateTextColor);

        mBackgroundColor = a.getColor(
                R.styleable.CalendarView_backgroundColor,
                mBackgroundColor);

        mDateElementColor = a.getColor(
                R.styleable.CalendarView_date_element_color,
                mDateElementColor);
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

        mDateRadius = a.getDimension(
                R.styleable.CalendarView_date_circle_radius,
                mDateRadius);
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

        mStreakPaint.setColor(mStreakColor);

        mDateElementPaint.setTextSize(mDateTextSize);
        mDateElementPaint.setColor(mDateElementColor);

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
    //endregion // Make measurements

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        invalidateTextPaintAndMeasurements();

        invalidatePaddingAndContentMeasurements();

        drawBackground(canvas);

        drawCalendarTitle(canvas);

        drawDateNames(canvas);

        drawDateElements(canvas);

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


    private void drawDateElements(Canvas canvas) {

        DateElement dateElements[] = mCalendarData.getDateElements();

        TextElement currentDayLabel = mCalendarData.getDayNameTextElements()[0];
        float yOrigin = currentDayLabel.getLastYValue() + currentDayLabel.getHeight();
        float calendarHeight = mContentHeight - yOrigin;

        float totalCalendarElementsHeight = dateElements[0].getDiameter() * 6;
        float elementSpace = (calendarHeight - totalCalendarElementsHeight) / 6f;

        int firstDay = model.getFirstWeekDay();
        int totalDays = model.getCalendarMonth().getActualMaximum(Calendar.DAY_OF_MONTH);
        int maxCell = totalDays + firstDay;

        int modelMonth = model.getCalendarMonth().get(Calendar.MONTH);
        int modelYear = model.getCalendarMonth().get(Calendar.YEAR);

        Calendar c = Calendar.getInstance();
        int currentDate = c.get(Calendar.DAY_OF_MONTH);
        int currentMonth = c.get(Calendar.MONTH);
        int currentYear = c.get(Calendar.YEAR);

        boolean isCurrentMonth = (modelMonth == currentMonth) && (modelYear == currentYear);

        boolean pastCurrentDate = false;


        for (int day = 0; day < 7; day++) {
            currentDayLabel = mCalendarData.getDayNameTextElements()[day];

            // Draw the first date element
            float x = currentDayLabel.getLastXValue() + (currentDayLabel.getWidth() / 2);
            float y = yOrigin + elementSpace;
            DateElement currentElement = dateElements[day];

            if (day < firstDay - 1)
                currentElement.setTextPaint(mDateTextPaint);
            else {
                int thisDay = day - (firstDay - 2);

                if (model.getDatesWithEntries().contains(thisDay)) {
                    currentElement.setTextPaint(mStreakPaint);

                    boolean isAStreak = (day == 0 && model.getDatesWithEntries().contains(thisDay - 1)) ||
                            model.getDatesWithEntries().contains(thisDay + 1);

                    currentElement.setIsAStreak(isAStreak);
                }
                else
                    currentElement.setTextPaint(mCalendarBackgroundPaint);

                if (!pastCurrentDate && isCurrentMonth && thisDay == currentDate) {
                    pastCurrentDate = true;
                    currentElement.setIsCurrentDay(true);
//                    mDateElementPaint.setAlpha(127);
                }

                mDateElementPaint.setAlpha(thisDay > currentDate && isCurrentMonth ? 77 : 255);
                TextElement text = new TextElement(String.valueOf(thisDay), mDateElementPaint);
                text.makeMeasurements();
                currentElement.setDateText(text);


            }

//            else if (model.getDatesWithEntries().contains(day))
//                currentElement.setTextPaint(mStreakPaint);

            if (currentElement.getIsAStreak()) {
                float x1 = x;
                float x2 = mContentWidth;

                if (day + 1 < mCalendarData.getDayNameTextElements().length) {
                    TextElement element = mCalendarData.getDayNameTextElements()[day + 1];
                    x2 = element.getLastXValue();
                }
                int thisDay = day - (firstDay - 2);
                if (day == 0 && model.getDatesWithEntries().contains(thisDay - 1)) {
                    x1 = 0;
                    x2 = model.getDatesWithEntries().contains(thisDay + 1) ? x2 : x;
                }

                currentElement.drawStreakLine(canvas, y, x1, x2);
            }

            currentElement.draw(canvas, x, y);

            // Draw the rest of the date elements in this column
            for (int row = 1; row < 6; row++) {
                y = currentElement.getLastYValue() + currentElement.getHeight() + elementSpace;
                int dayIndex = day + (row * 7);
                currentElement = dateElements[dayIndex];

                if (dayIndex >= maxCell - 1)
                    currentElement.setTextPaint(mDateTextPaint);
                else {
                    int thisDay = dayIndex - (firstDay - 2);
                    if (model.getDatesWithEntries().contains(thisDay)) {
                        currentElement.setTextPaint(mStreakPaint);

                        boolean isAStreak = (day == 0 && model.getDatesWithEntries().contains(thisDay - 1)) ||
                                model.getDatesWithEntries().contains(thisDay + 1);

                        currentElement.setIsAStreak(isAStreak);
                    }
                    else
                        currentElement.setTextPaint(mCalendarBackgroundPaint);

                    mDateElementPaint.setAlpha(thisDay > currentDate && isCurrentMonth ? 77 : 255);
                    TextElement text = new TextElement(String.valueOf(thisDay), mDateElementPaint);
                    text.makeMeasurements();
                    currentElement.setDateText(text);

                    if (!pastCurrentDate && isCurrentMonth && thisDay == currentDate) {
                        pastCurrentDate = true;
                        currentElement.setIsCurrentDay(true);
//                        mDateElementPaint.setAlpha(127);
                    }
                }

//                else if (model.getDatesWithEntries().contains(day))
//                    currentElement.setTextPaint(mStreakPaint);

                if (currentElement.getIsAStreak()) {
                    float x1 = x;
                    float x2 = mContentWidth;

                    if (day + 1 < mCalendarData.getDayNameTextElements().length) {
                        TextElement element = mCalendarData.getDayNameTextElements()[day + 1];
                        x2 = element.getLastXValue();
                    }
                    int thisDay = dayIndex - (firstDay - 2);
                    if (day == 0 && model.getDatesWithEntries().contains(thisDay - 1)) {
                        x1 = 0;
                        x2 = model.getDatesWithEntries().contains(thisDay + 1) ? x2 : x;
                    }

                    currentElement.drawStreakLine(canvas, y, x1, x2);
                }
                currentElement.draw(canvas, x, y);
            }

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

    public void setStreakColor(int streakColor) {
        mStreakColor = streakColor;
        if (mStreakPaint != null)
            mStreakPaint.setColor(streakColor);
    }

    public void bindModel(CalendarViewMonthModel model) {
        this.model = model;
        if (mCalendarData != null) {
            mCalendarData.getTitle().setText(model.getMonthTitle());
            invalidateTextPaintAndMeasurements();
            invalidatePaddingAndContentMeasurements();
            invalidate();
        }
    }
    //endregion // Setters
}
