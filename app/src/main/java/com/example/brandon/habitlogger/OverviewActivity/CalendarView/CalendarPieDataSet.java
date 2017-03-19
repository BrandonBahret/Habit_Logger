package com.example.brandon.habitlogger.OverviewActivity.CalendarView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 3/18/2017.
 *
 */

public class CalendarPieDataSet {

    private List<CalendarPieEntry> calendarPieEntries;
    private Paint mCurrentPaint;

    public CalendarPieDataSet(List<Float> values, List<Integer> colors){
        calendarPieEntries = CalendarPieEntry.getDataSet(values, colors);
        mCurrentPaint = new Paint();
    }

    public CalendarPieDataSet(List<CalendarPieEntry> entries) {
        calendarPieEntries = entries;
        mCurrentPaint = new Paint();
    }

    public void draw(Canvas canvas, float x, float y, float radius){

        RectF oval = new RectF(x - radius, y - radius, x + radius, y + radius);

        float startAngle = 0;
        for(CalendarPieEntry entry : calendarPieEntries){
            int arc = (int)(entry.getValue() * 360);

            mCurrentPaint.setColor(entry.getColor());
            canvas.drawArc(oval, startAngle, arc, true, mCurrentPaint);
            startAngle = startAngle + arc;
        }
    }

    public static class CalendarPieEntry {
        private float mValue;
        private int mColor;

        public CalendarPieEntry(float value, int color){
            mValue = value;
            mColor = color;
        }

        public static List<CalendarPieEntry> getDataSet(List<Float> values, List<Integer> colors){
            List<CalendarPieEntry> entries = new ArrayList<>(values.size());

            for(int i = 0; i < values.size(); i++){
                entries.add(new CalendarPieEntry(values.get(i), colors.get(i)));
            }

            return entries;
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
        public void setValue(float mValue) {
            this.mValue = mValue;
        }

        public void setColor(int mColor) {
            this.mColor = mColor;
        }
        //endregion
    }
}
