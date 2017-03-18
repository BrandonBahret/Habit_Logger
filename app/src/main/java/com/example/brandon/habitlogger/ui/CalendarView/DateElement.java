package com.example.brandon.habitlogger.ui.CalendarView;

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
    private boolean isEnabled = true;

    private TextElement mDateText;
    private boolean isCurrentDay = false;
    private int mDateTextColor = -1;
    private int mDateTextColorDark = -1;
    private int mDateTextColorLight = -1;

    private Paint mCurrentDatePaint;
    private Paint mStreakLinePaint;
    private boolean isAStreak = false;

    public DateElement(TextPaint paint, @Nullable TextElement dateText) {
        super(paint);
        mDateText = dateText;
        mCurrentDatePaint = new Paint(paint);
        mCurrentDatePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mStreakLinePaint = new Paint(paint);
        mStreakLinePaint.setStrokeWidth(16);
        int currentDateColor = MyColorUtils.setLightness(mCurrentDatePaint.getColor(), 0.35f);
        mCurrentDatePaint.setColor(currentDateColor);
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
        if(isCurrentDay){
            canvas.drawCircle(x, y, mRadius * 1.2f, mCurrentDatePaint);
        }

        // Draw the background for the date element
        canvas.drawCircle(x, y, mRadius, mTextPaint);

        if (mDateText != null) { // Draw the date text
            TextPaint paint = new TextPaint(mDateText.getPaint());

            int backgroundColor = mTextPaint.getColor();
            float lightness = MyColorUtils.getLightness(backgroundColor);

            if(lightness < 0.6f){
                paint.setColor(Color.WHITE);
            }

            mDateText.draw(canvas, paint, x - mDateText.getWidth() / 2f, y + mDateText.getHeight() / 2f);
        }

    }

    public void drawStreakLine(Canvas canvas, float y, float x1, float x2) {
        canvas.drawLine(x1, y, x2, y, mStreakLinePaint);
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
        return this;
    }

    public DateElement setDiameter(float diameter) {
        mRadius = diameter / 2f;
        mDiameter = diameter;
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
