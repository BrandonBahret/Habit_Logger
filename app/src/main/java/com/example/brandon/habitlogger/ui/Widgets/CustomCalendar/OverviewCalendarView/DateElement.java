package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.OverviewCalendarView;

import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.text.TextPaint;

import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.DateElementBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.TextElement;

/**
 * Created by Brandon on 3/16/2017.
 * Class representing each date in this calendar view
 */

public class DateElement extends DateElementBase {

    //region (Member attributes)
    private CalendarPieDataSet mPieData;
    //endregion

    public DateElement(TextPaint paint, @Nullable TextElement dateText, @Nullable CalendarPieDataSet pieData) {
        super(paint, dateText);
        mPieData = pieData;
    }

    @Override
    public void onDrawOverBase(Canvas canvas, float x, float y) {

        // Draw the pie chart on top of the date element base
        if (mPieData != null)
            mPieData.draw(canvas, x, y, mSmallRadius);

        // Draw the date text
        if (mDateText != null)
            mDateText.draw(canvas, x, y - mDateText.getHeight() / 3f - mRadius);

    }

    //region Setters {}
    public DateElement setPieData(CalendarPieDataSet pieData) {
        mPieData = pieData;
        return this;
    }
    //endregion

}
