package com.example.brandon.habitlogger.CustomCalendar.OverviewCalendarView;


import android.text.TextPaint;

import com.example.brandon.habitlogger.CustomCalendar.CalendarViewDataBase;

/**
 * Created by Brandon on 4/3/2017.
 * Class for defining the layout of this custom calendar view
 */

public class CalendarViewData extends CalendarViewDataBase<DateElement> {

    @Override
    public void setDateElementsPaint(TextPaint paint) {
        mDateElements = new DateElement[mDays.length * 6];

        for (int i = 0; i < mDateElements.length; i++) {
            if (mDateElements[i] == null)
                mDateElements[i] = new DateElement(paint, null, null);
            else
                mDateElements[i].setTextPaint(paint);
        }
    }

}
