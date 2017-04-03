package com.example.brandon.habitlogger.CustomCalendar;

import android.text.TextPaint;

import com.example.brandon.habitlogger.CustomCalendar.OverviewCalendarView.DateElement;

/**
 * Created by Brandon on 3/15/2017.
 * The base class for defining the layout of calendar views
 */

public abstract class CalendarViewDataBase {

    //region (Member attributes)
    private TextElement mNoDataAvailableText;
    private TextElement mTitle;

    private String mDays[] = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    private TextElement[] mDayNameTextElements;
    private float mContentMarginTop;

    private DateElementBase mDateElements[];
    //endregion

    public void makeMeasurements() {
        if (getTitle() != null)
            getTitle().makeMeasurements();

        if (getNoDataAvailableText() != null)
            getNoDataAvailableText().makeMeasurements();

        if (mDayNameTextElements != null) {
            for (TextElement textElement : mDayNameTextElements)
                textElement.makeMeasurements();
        }

        if (mDateElements != null) {
            for (DateElementBase dateElement : mDateElements)
                dateElement.makeMeasurements();
        }
    }

    //region Getters {}
    public TextElement getTitle() {
        return mTitle;
    }

    public TextElement getNoDataAvailableText() {
        return mNoDataAvailableText;
    }

    public TextElement[] getDayNameTextElements() {
        return mDayNameTextElements;
    }

    public DateElementBase[] getDateElements() {
        return mDateElements;
    }

    public float getContentMarginTop() {
        return mContentMarginTop;
    }
    //endregion

    //region Setters {}
    public CalendarViewDataBase setDayNameHeaderPaint(TextPaint paint) {
        mDayNameTextElements = new TextElement[mDays.length];
        for (int i = 0; i < mDays.length; i++)
            mDayNameTextElements[i] = new TextElement(mDays[i], paint);

        return this;
    }


    protected abstract DateElementBase getNewDateElement(TextPaint paint);

    public CalendarViewDataBase setDateElementsPaint(TextPaint paint) {
        mDateElements = new DateElement[mDays.length * 6];

        for (int i = 0; i < mDateElements.length; i++) {
            if (mDateElements[i] == null)
                mDateElements[i] = getNewDateElement(paint);
            else
                mDateElements[i].setTextPaint(paint);
        }

        return this;
    }

    public CalendarViewDataBase setDateElementsRadius(float radius) {

        for (DateElementBase dateElement : mDateElements)
            dateElement.setRadius(radius);

        return this;
    }

    public CalendarViewDataBase setTitle(String title, TextPaint paint) {
        mTitle = new TextElement(title, paint);
        return this;
    }

    public CalendarViewDataBase setNoDataAvailableText(String text, TextPaint paint) {
        mNoDataAvailableText = new TextElement(text, paint);
        return this;
    }

    public void setContentMarginTop(float contentMarginTop) {
        mContentMarginTop = contentMarginTop;
    }
    //endregion
}