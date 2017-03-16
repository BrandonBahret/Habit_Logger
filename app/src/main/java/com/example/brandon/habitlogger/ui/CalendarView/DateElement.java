package com.example.brandon.habitlogger.ui.CalendarView;

import android.graphics.Canvas;
import android.text.TextPaint;

/**
 * Created by Brandon on 3/16/2017.
 *
 */

public class DateElement extends ViewElement {

    private float mRadius = 48;
    private float mDiameter = mRadius * 2;

    public DateElement(TextPaint paint) {
        super(paint);
    }

    @Override
    public ViewElement makeMeasurements() {
        mWidth = mDiameter;
        mHeight = mDiameter;
        return this;
    }

    @Override
    public void draw(Canvas canvas, float x, float y) {
        super.draw(canvas, x, y);
        canvas.drawCircle(x, y, mRadius, mPaint);
    }

    //region Setters {}
    public DateElement setRadius(float radius){
        mRadius = radius;
        mDiameter = mRadius * 2;
        return this;
    }

    public DateElement setDiameter(float diameter){
        mRadius = diameter / 2f;
        mDiameter = diameter;
        return this;
    }
    //endregion

    //region Getters {}
    public float getRadius(){
        return mRadius;
    }

    public float getDiameter() {
        return mDiameter;
    }
    //endregion

}
