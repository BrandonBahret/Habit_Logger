package com.example.brandon.habitlogger.CustomCalendar.OverviewCalendarView;


import android.text.TextPaint;

import com.example.brandon.habitlogger.CustomCalendar.CalendarViewDataBase;
import com.example.brandon.habitlogger.CustomCalendar.DateElementBase;
import com.example.brandon.habitlogger.CustomCalendar.TextElement;

/**
 * Created by Brandon on 4/3/2017.
 * Class for defining the layout of this custom calendar view
 */

public class CalendarViewData extends CalendarViewDataBase {

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


    public CalendarViewDataBase setDateElementsPaint(TextPaint paint) {
        mDateElements = new DateElement[mDays.length * 6];

        for (int i = 0; i < mDateElements.length; i++) {
            if (mDateElements[i] == null)
                mDateElements[i] = new DateElement(paint, null, null);
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
