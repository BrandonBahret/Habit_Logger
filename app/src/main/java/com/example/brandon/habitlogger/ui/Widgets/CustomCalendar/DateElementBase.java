package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;

/**
 * Created by Brandon on 4/3/2017.
 * The base class for all date element classes used in my custom calendar views.
 */

public abstract class DateElementBase extends ViewElement {

    //region (Member attributes)
    protected float mRadius = 48;
    protected float mStrokeRadius = -1;
    protected float mSmallRadius = -1;
    protected float mDiameter = mRadius * 2;

    protected TextElement mDateText;
    protected boolean mIsCurrentDay = false;
    protected Paint mCurrentDatePaint;
    //endregion

    public DateElementBase(Context context, TextPaint paint, @Nullable TextElement dateText) {
        super(paint, context);
        mDateText = dateText;
        mCurrentDatePaint = new Paint(paint);
        mCurrentDatePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

//        boolean isBright = MyColorUtils.isColorBright(paint.getColor(), 0.65f);
//        int currentDateColor = isBright ? Color.BLACK : Color.WHITE;
//        mCurrentDatePaint.setColor(currentDateColor);
    }

    @Override
    public ViewElement makeMeasurements() {
        mWidth = mDiameter;
        mHeight = mDiameter;

        if (mStrokeRadius == -1)
            mStrokeRadius = calculateStrokeRadius();

        if (mSmallRadius == -1)
            mSmallRadius = calculateSmallRadius();

        if (mDateText != null)
            mDateText.makeMeasurements();

        return this;
    }

    public abstract void onDrawOverBase(Canvas canvas, float x, float y);

    @Override
    public void draw(Canvas canvas, float x, float y) {
        super.draw(canvas, x, y);

        // Draw the current day circle background
        if (mIsCurrentDay)
            canvas.drawCircle(x, y, mStrokeRadius, mCurrentDatePaint);

        // Draw the background for the date element
        canvas.drawCircle(x, y, mRadius, mTextPaint);

        onDrawOverBase(canvas, x, y);
    }

    private float calculateStrokeRadius() {
        return mRadius * 1.1f;
    }

    private float calculateSmallRadius() {
        return mRadius * 0.85f;
    }

    //region Setters {}
    public DateElementBase setRadius(float radius) {
        mRadius = radius;
        mDiameter = mRadius * 2;
        return this;
    }

    public void setDateText(TextElement text) {
        mDateText = text;
    }

    public void setIsCurrentDay(boolean isCurrentDay) {
        this.mIsCurrentDay = isCurrentDay;
    }

    public void setCurrentDatePaint(Paint paint){
        mCurrentDatePaint = paint;
    }
    //endregion

    //region Getters {}
    public float getRadius() {
        return mRadius;
    }

    public float getDiameter() {
        return mDiameter;
    }
    //endregion

}
