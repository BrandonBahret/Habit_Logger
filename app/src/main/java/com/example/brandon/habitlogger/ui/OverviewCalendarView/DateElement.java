package com.example.brandon.habitlogger.ui.OverviewCalendarView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;

import com.example.brandon.habitlogger.common.MyColorUtils;

/**
 * Created by Brandon on 3/16/2017.
 */

public class DateElement extends ViewElement {

    private float mRadius = 48;
    private float mDiameter = mRadius * 2;
    private float mStrokeRadius = calculateStrokeRadius();
    private float mSmallRadius = calculateSmallRadius();

    private boolean isEnabled = true;

    private TextElement mDateText;
    private boolean isCurrentDay = false;
    private int mDateTextColor = -1;
    private int mDateTextColorDark = -1;
    private int mDateTextColorLight = -1;

    private TextPaint mCurrentDatePaint;
    private Paint mStreakLinePaint;
    private boolean isAStreak = false;

    private CalendarPieDataSet mPieData;

    public DateElement(TextPaint paint, @Nullable TextElement dateText, @Nullable CalendarPieDataSet pieData) {
        super(paint);
        mDateText = dateText;
        mCurrentDatePaint = new TextPaint(paint);
        mCurrentDatePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mStreakLinePaint = new Paint(paint);
        mStreakLinePaint.setStrokeWidth(16);
        int currentDateColor = MyColorUtils.setLightness(paint.getColor(), 0.35f);
        mCurrentDatePaint.setColor(currentDateColor);

        mPieData = pieData;
    }

    @Override
    public ViewElement makeMeasurements() {
        mWidth = mDiameter;
        mHeight = mDiameter;

        if (mDateText != null)
            mDateText.makeMeasurements();

        return this;
    }

    @Override
    public void draw(Canvas canvas, float x, float y) {
        super.draw(canvas, x, y);

        // Draw the current day circle background
        if (isCurrentDay) {
            canvas.drawCircle(x, y, mStrokeRadius, mCurrentDatePaint);
        }

        // Draw the background for the date element
        canvas.drawCircle(x, y, mRadius, mTextPaint);

        if (mPieData != null)
            mPieData.draw(canvas, x, y, mSmallRadius);

        if (mDateText != null) { // Draw the date text
            mDateText.draw(canvas, x - mDateText.getWidth() / 2f, y - mDateText.getHeight() / 3f - mRadius);
        }

    }

    public void drawStreakLine(Canvas canvas, float y, float x1, float x2) {
        canvas.drawLine(x1, y, x2, y, mStreakLinePaint);
    }

    private float calculateStrokeRadius() {
        return mRadius * 1.1f;
    }

    private float calculateSmallRadius() {
        return mRadius * 0.85f;
    }

    //region Setters {}
    @Override
    public ViewElement setTextPaint(TextPaint paint) {
//        if (mDateText != null) {
//            mDateText.setPaintColor(Color.WHITE);
//        }

        mStreakLinePaint.setColor(paint.getColor());

        return super.setTextPaint(paint);
    }

    @Override
    public ViewElement setPaintColor(int color) {
        mStreakLinePaint.setColor(color);
        return super.setPaintColor(color);
    }

    public DateElement setRadius(float radius) {
        mRadius = radius;
        mDiameter = mRadius * 2;
        mStrokeRadius = calculateStrokeRadius();
        mSmallRadius = calculateSmallRadius();
        return this;
    }

    public DateElement setPieData(CalendarPieDataSet pieData) {
        mPieData = pieData;
        return this;
    }

    public DateElement setDiameter(float diameter) {
        mRadius = diameter / 2f;
        mDiameter = diameter;
        mStrokeRadius = calculateStrokeRadius();
        mSmallRadius = calculateSmallRadius();
        return this;
    }

    public void setDateText(TextElement text) {
        mDateText = text;
        mDateTextColor = text.getPaint().getColor();
        mDateTextColorDark = mDateTextColor;
        mDateTextColorLight = Color.WHITE;
    }

    public void setIsCurrentDay(boolean isCurrentDay) {
        this.isCurrentDay = isCurrentDay;
    }

    public void setIsAStreak(boolean isAStreak) {
        this.isAStreak = isAStreak;
    }
    //endregion

    //region Getters {}
    public float getRadius() {
        return mRadius;
    }

    public float getDiameter() {
        return mDiameter;
    }

    public boolean getIsAStreak() {
        return isAStreak;
    }
    //endregion

}
