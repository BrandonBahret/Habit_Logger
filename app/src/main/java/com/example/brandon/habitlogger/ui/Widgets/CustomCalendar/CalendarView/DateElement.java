package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.DateElementBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.TextElement;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.ViewElement;

/**
 * Created by Brandon on 3/16/2017.
 * Class representing each date in this calendar view
 */

public class DateElement extends DateElementBase {

    //region (Member attributes)
    private boolean mIsAStreak = false;
    private boolean mHasEntries = false;
    protected Paint mStreakLinePaint;
    //endregion

    public DateElement(Context context, TextPaint paint, @Nullable TextElement dateText) {
        super(context, paint, dateText);

        mStreakLinePaint = new Paint(paint);

        float dpSize = 5.5f;
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        mStreakLinePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void onDrawOverBase(Canvas canvas, float x, float y) {
        if (mDateText != null) { // Draw the date text
            TextPaint paint = new TextPaint(mDateText.getPaint());

            if (mHasEntries) {
                int backgroundColor = mTextPaint.getColor();
                if (MyColorUtils.isColorBright(backgroundColor))
                    paint.setColor(Color.DKGRAY);
                else
                    paint.setColor(Color.WHITE);
            }

            mDateText.draw(canvas, paint, x, y + mDateText.getHeight() / 2f);
        }
    }

    public void drawStreakLine(Canvas canvas, float y, float x1, float x2) {
        canvas.drawLine(x1, y, x2, y, mStreakLinePaint);
    }

    //region Setters {}
    public void setIsAStreak(boolean isAStreak) {
        mIsAStreak = isAStreak;
    }

    public void setHasEntries(boolean hasEntries) {
        mHasEntries = hasEntries;
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
