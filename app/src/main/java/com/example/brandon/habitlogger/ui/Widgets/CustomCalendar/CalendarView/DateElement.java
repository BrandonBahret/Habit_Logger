package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;

import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.DateElementBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.TextElement;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.ViewElement;
import com.example.brandon.habitlogger.common.MyColorUtils;

/**
 * Created by Brandon on 3/16/2017.
 * Class representing each date in this calendar view
 */

public class DateElement extends DateElementBase {

    //region (Member attributes)
    private boolean mIsAStreak = false;
    protected Paint mStreakLinePaint;
    //endregion

    public DateElement(TextPaint paint, @Nullable TextElement dateText) {
        super(paint, dateText);

        mStreakLinePaint = new Paint(paint);
        mStreakLinePaint.setStrokeWidth(16);
    }

    @Override
    public void onDrawOverBase(Canvas canvas, float x, float y) {
        if (mDateText != null) { // Draw the date text
            TextPaint paint = new TextPaint(mDateText.getPaint());

            int backgroundColor = mTextPaint.getColor();
            if(!MyColorUtils.isColorBright(backgroundColor, 0.6f))
                paint.setColor(Color.WHITE);

            mDateText.draw(canvas, paint, x - mDateText.getWidth() / 2f, y + mDateText.getHeight() / 2f);
        }
    }

    public void drawStreakLine(Canvas canvas, float y, float x1, float x2) {
        canvas.drawLine(x1, y, x2, y, mStreakLinePaint);
    }

    //region Setters {}
    public void setIsAStreak(boolean isAStreak) {
        mIsAStreak = isAStreak;
    }

    @Override
    public ViewElement setTextPaint(TextPaint paint) {
        mStreakLinePaint.setColor(paint.getColor());
        return super.setTextPaint(paint);
    }

    @Override
    public ViewElement setPaintColor(int color) {
        mStreakLinePaint.setColor(color);
        return super.setPaintColor(color);
    }
    //endregion

    //region Getters {}
    public boolean getIsAStreak() {
        return mIsAStreak;
    }
    //endregion

}
