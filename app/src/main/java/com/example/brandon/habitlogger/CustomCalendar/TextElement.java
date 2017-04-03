package com.example.brandon.habitlogger.CustomCalendar;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/15/2017.
 *
 */

public class TextElement extends ViewElement {
    private String mText;

    @Override
    public String toString() {
        return mText;
    }

    public TextElement(String text, TextPaint paint) {
        super(paint);
        mText = text;
    }

    @Override
    public TextElement makeMeasurements() {
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), bounds);

        mWidth = bounds.width();
        mHeight = bounds.height();
        return this;
    }

    @Override
    public void draw(Canvas canvas, float x, float y) {
        super.draw(canvas, x, y);
        canvas.drawText(mText, lastXValue, lastYValue, mTextPaint);
    }

    public void draw(Canvas canvas, TextPaint paint,  float x, float y) {
        super.draw(canvas, x, y);
        canvas.drawText(mText, lastXValue, lastYValue, paint);
    }

    //region Setters {}
    public TextElement setText(String text) {
        mText = text;
        return this;
    }
    //endregion
}