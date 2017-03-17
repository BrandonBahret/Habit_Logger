package com.example.brandon.habitlogger.ui.CalendarView;

import android.content.Context;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/15/2017.
 */

class CalendarViewData {
    private Context mContext;

    private TextElement mNoDataAvailableText;
    private TextElement mTitle;

    private String days[] = new String[]{"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
    private TextElement[] mDayNameTextElements;
    private float mContentMarginTop;

    private DateElement mDateElements[];

    public CalendarViewData(Context context) {
        mContext = context;
    }

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
    public CalendarViewData setDayNameHeaderPaint(TextPaint paint) {
        mDayNameTextElements = new TextElement[days.length];

        for (int i = 0; i < days.length; i++) {
            mDayNameTextElements[i] = new TextElement(days[i], paint);
        }

        return this;
    }

    public CalendarViewData setDateElementsPaint(TextPaint paint) {
        mDateElements = new DateElement[days.length * 6];

        for (int i = 0; i < mDateElements.length; i++) {
            if (mDateElements[i] == null)
                mDateElements[i] = new DateElement(paint, null);
            else
                mDateElements[i].setTextPaint(paint);
        }

        return this;
    }

    public CalendarViewData setDateElementsRadius(float radius) {

        for (DateElement dateElement : mDateElements) {
            dateElement.setRadius(radius);
        }

        return this;
    }

    public CalendarViewData setTitle(String title, TextPaint paint) {
        mTitle = new TextElement(title, paint);
        return this;
    }

    public CalendarViewData setNoDataAvailableText(String text, TextPaint paint) {
        mNoDataAvailableText = new TextElement(text, paint);
        return this;
    }

    public void setContentMarginTop(float contentMarginTop) {
        mContentMarginTop = contentMarginTop;
    }
    //endregion
}













