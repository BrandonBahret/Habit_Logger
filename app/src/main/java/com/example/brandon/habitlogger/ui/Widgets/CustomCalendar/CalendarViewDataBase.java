package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar;

import android.text.TextPaint;

/**
 * Created by Brandon on 3/15/2017.
 * The base class for defining the layout of calendar views
 */

public abstract class CalendarViewDataBase <DateElement extends DateElementBase> {

    //region (Member attributes)
    private TextElement mNoDataAvailableText;
    private TextElement mTitle;

    protected String[] mDays = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    private TextElement[] mDayNameTextElements;
    private float mContentMarginTop;

    protected DateElement[] mDateElements;
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
            for (DateElement dateElement : mDateElements)
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

    public DateElement[] getDateElements() {
        return mDateElements;
    }

    public float getContentMarginTop() {
        return mContentMarginTop;
    }
    //endregion

    //region Setters {}
    public void setDayNameHeaderPaint(TextPaint paint) {
        mDayNameTextElements = new TextElement[mDays.length];

        for (int i = 0; i < mDays.length; i++) {
            mDayNameTextElements[i] = new TextElement(mDays[i], paint);
        }
    }

    public abstract void setDateElementsPaint(TextPaint paint);

    public void setDateElementsRadius(float radius) {

        for (DateElement dateElement : mDateElements) {
            dateElement.setRadius(radius);
        }
    }

    public void setTitle(String title, TextPaint paint) {
        mTitle = new TextElement(title, paint);
    }

    public void setNoDataAvailableText(String text, TextPaint paint) {
        mNoDataAvailableText = new TextElement(text, paint);
    }

    public void setContentMarginTop(float contentMarginTop) {
        mContentMarginTop = contentMarginTop;
    }
    //endregion

}