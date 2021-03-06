package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/15/2017.
 *
 */

public class TextElement extends ViewElement {

    //region (Member attributes)
    private String mText;
    //endregion

    public TextElement(Context context, String text, TextPaint paint) {
        super(paint, context);
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
        canvas.drawText(mText, mLastXValue, mLastYValue, mTextPaint);
    }

    public void draw(Canvas canvas, TextPaint paint,  float x, float y) {
        super.draw(canvas, x, y);
        canvas.drawText(mText, mLastXValue, mLastYValue, paint);
    }

    @Override
    public String toString() {
        return mText;
    }

    //region Setters {}
    public TextElement setText(String text) {
        mText = text;
        return this;
    }
    //endregion
}