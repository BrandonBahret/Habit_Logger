package com.example.brandon.habitlogger.ui.CalendarView;

import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/16/2017.
 */

public class DateElement extends ViewElement {

    private float mRadius = 48;
    private float mDiameter = mRadius * 2;
    private boolean isEnabled = true;

    private TextElement mDateText;

    public DateElement(TextPaint paint, @Nullable TextElement dateText) {
        super(paint);
        mDateText = dateText;
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

        if (mDateText == null || isEnabled)
            canvas.drawCircle(x, y, mRadius, mTextPaint);

        if (mDateText != null) {// Draw the date text
            mDateText.draw(canvas, x - mDateText.getWidth() / 4f, y + mDateText.getHeight() / 4f);
        }

    }

    //region Setters {}


    @Override
    public ViewElement setTextPaint(TextPaint paint) {
        if (mDateText != null) {
            mDateText.setPaintColor(0xffffffff);
        }

        return super.setTextPaint(paint);
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
