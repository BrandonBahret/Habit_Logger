package com.example.brandon.habitlogger.ui.CalendarView;

import android.graphics.Canvas;
import android.support.annotation.Dimension;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/16/2017.
 *
 */

abstract public class ViewElement {
    protected TextPaint mPaint;
    protected float mWidth;
    protected float mHeight;
    protected float lastYValue = 0, lastXValue = 0;

    @Dimension protected float mMarginTop;

    public ViewElement(TextPaint paint) {
        mPaint = paint;
    }

    abstract public ViewElement makeMeasurements();

    public void draw(Canvas canvas, float x, float y) {
        lastXValue = x;
        lastYValue = y + getMarginTop();
    }

    //region Setters {}
    public ViewElement setPaint(TextPaint paint) {
        mPaint = paint;
        return this;
    }

    public ViewElement setPaintColor(int color) {
        mPaint.setColor(color);
        return this;
    }


    public ViewElement setMarginTop(float dp) {
        mMarginTop = dp;
        return this;
    }
    //endregion

    //region Getters {}
    public TextPaint getPaint() {
        return mPaint;
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