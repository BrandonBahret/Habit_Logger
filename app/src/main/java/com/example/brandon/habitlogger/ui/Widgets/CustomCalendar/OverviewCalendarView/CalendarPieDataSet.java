package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.OverviewCalendarView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

/**
 * Created by Brandon on 3/18/2017.
 * A class to represent the pie charts used with this calendar view
 */

public class CalendarPieDataSet {

    //region (Member attributes)
    private List<CalendarPieEntry> mCalendarPieEntries;
    private Paint mCurrentPaint;
    private int mDate;
    //endregion

    public CalendarPieDataSet(List<CalendarPieEntry> entries, int date) {
        mCalendarPieEntries = entries;
        mCurrentPaint = new Paint();
        mDate = date;
    }

    public void draw(Canvas canvas, float x, float y, float radius){
        RectF oval = new RectF(x - radius, y - radius, x + radius, y + radius);

        float startAngle = 0;
        for(CalendarPieEntry entry : mCalendarPieEntries){
            int arc = (int)(entry.getValue() * 360);

            mCurrentPaint.setColor(entry.getColor());
            canvas.drawArc(oval, startAngle, arc, true, mCurrentPaint);
            startAngle = startAngle + arc;
        }
    }

    //region Getters {}
    public int getDate() {
        return mDate;
    }
    //endregion=

    public static class CalendarPieEntry {
        private float mValue;
        private int mColor;

        public CalendarPieEntry(float value, int color){
            mValue = value;
            mColor = color;
        }

        //region Getters {}
        public float getValue() {
            return mValue;
        }

        public int getColor() {
            return mColor;
        }
        //endregion

        //region Setters {}
        public void setValue(float value) {
            mValue = value;
        }

        public void setColor(int color) {
            mColor = color;
        }
        //endregion
    }
}
