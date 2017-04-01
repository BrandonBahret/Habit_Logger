package com.example.brandon.habitlogger.ui.OverviewCalendarView;

import android.graphics.Canvas;
import android.support.annotation.Dimension;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/16/2017.
 *
 */

abstract public class ViewElement {
    protected TextPaint mTextPaint;
    protected float mWidth;
    protected float mHeight;
    protected float lastYValue = 0, lastXValue = 0;

    @Dimension protected float mMarginTop;

    public ViewElement(TextPaint paint) {
        mTextPaint = paint;
    }

    abstract public ViewElement makeMeasurements();

    public void draw(Canvas canvas, float x, float y) {
        lastXValue = x;
        lastYValue = y + getMarginTop();
    }

    //region Setters {}
    public ViewElement setTextPaint(TextPaint paint) {
        mTextPaint = paint;
        return this;
    }

    public ViewElement setPaintColor(int color) {
        mTextPaint.setColor(color);
        return this;
    }


    public ViewElement setMarginTop(float dp) {
        mMarginTop = dp;
        return this;
    }
    //endregion

    //region Getters {}
    public TextPaint getPaint() {
        return mTextPaint;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    @Dimension
    public float getMarginTop() {
        return mMarginTop;
    }

    public float getLastXValue() {
        return lastXValue;
    }

    public float getLastYValue() {
        return lastYValue;
    }
    //endregion
}