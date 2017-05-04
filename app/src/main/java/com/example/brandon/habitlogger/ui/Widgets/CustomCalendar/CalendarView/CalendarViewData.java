package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarView;

import android.content.Context;
import android.text.TextPaint;

import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewDataBase;

/**
 * Created by Brandon on 3/15/2017.
 * Class for defining the layout of this custom calendar view
 */

public class CalendarViewData extends CalendarViewDataBase<DateElement> {

    private Context mContext;

    public CalendarViewData(Context context) {
        mContext = context;
    }

    @Override
    protected Context getContext() {
        return mContext;
    }

    @Override
    public void setDateElementsPaint(TextPaint paint) {
        mDateElements = new DateElement[mDays.length * 6];

        for (int i = 0; i < mDateElements.length; i++) {
            if (mDateElements[i] == null)
                mDateElements[i] = new DateElement(getContext(), paint, null);
            else
                mDateElements[i].setTextPaint(paint);
        }
    }

}