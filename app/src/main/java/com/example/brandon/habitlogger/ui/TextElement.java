package com.example.brandon.habitlogger.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Dimension;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/15/2017.
 *
 */

public class TextElement{
    private String mText;
    private TextPaint mPaint;
    private float  mWidth;
    private float  mHeight;

    private float lastYValue = 0, lastXValue = 0;

    @Dimension private float mMarginTop;

    @Override
    public String toString() {
        return mText;
    }

    public TextElement(String text, TextPaint paint){
        mText = text;
        mPaint = paint;
    }

    public TextElement makeMeasurements(){
        Rect bounds = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), bounds);

        mWidth = bounds.width();
        mHeight = bounds.height();

        return this;
    }

    public void draw(Canvas canvas, float x, float y) {
        lastXValue = x;
        lastYValue =  y + getMarginTop();
        canvas.drawText(mText, lastXValue, lastYValue, mPaint);
    }

    //region Setters {}
    public TextElement setText(String text){
        mText = text;
        return this;
    }

    public TextElement setTextPaint(TextPaint paint){
        mPaint = paint;
        return this;
    }

    public TextElement setMarginTop(float dp){
        mMarginTop = dp;
        return this;
    }
    //endregion

    //region Getters {}
    public TextPaint getPaint(){
        return mPaint;
    }

    public float getWidth(){
        return mWidth;
    }

    public float getHeight(){
        return mHeight;
    }

    @Dimension
    public float getMarginTop(){
        return mMarginTop;
    }

    public float getLastXValue(){
        return lastXValue;
    }

    public float getLastYValue(){
        return lastYValue;
    }
    //endregion
}