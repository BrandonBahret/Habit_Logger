package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewDataBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.TextElement;
import com.example.brandon.habitlogger.R;

import java.util.Calendar;

/**
 * A view to represent entry data of a habit in a month-long calendar space.
 */

public class CalendarView extends CalendarViewBase {

    //region (Member attributes)
    private int mStreakColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private TextPaint mStreakPaint;
    //endregion

    //region Constructors {}
    public CalendarView(Context context) {
        super(context);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //endregion

    //region Methods responsible for initializing the view
    @Override
    protected CalendarViewDataBase onCreateCalenderModel() {
        CalendarViewData data = new CalendarViewData();
        data.setTitle("January 2017", mTitlePaint);
        return data;
    }

    @Override
    protected void onCreatePaintObjects() {
        mStreakPaint = new TextPaint();
    }

    @Override
    protected void onGatherAttributes(TypedArray attributes) {
        mStreakColor = attributes.getColor(
                R.styleable.CalendarView_date_circle_streak_color,
                mStreakColor);
    }
    //endregion -- end --

    //region Methods responsible for drawing the view
    @Override
    protected void onInvalidateTextPaintAndMeasurements() {
        mStreakPaint.setColor(mStreakColor);
    }

    @Override
    public void onDrawOverBase(Canvas canvas) {
        drawDateElements(canvas);
    }

    private void drawDateElements(Canvas canvas) {

        DateElement dateElements[] = (DateElement[]) mCalendarData.getDateElements();

        TextElement currentDayLabel = mCalendarData.getDayNameTextElements()[0];
        float yOrigin = currentDayLabel.getLastYValue() + currentDayLabel.getHeight();
        float calendarHeight = mContentHeight - yOrigin;

        float totalCalendarElementsHeight = dateElements[0].getDiameter() * 6;
        float elementSpace = (calendarHeight - totalCalendarElementsHeight) / 6f;

        int firstDay = mModel.getFirstWeekDay();
        int totalDays = mModel.getCalendarMonth().getActualMaximum(Calendar.DAY_OF_MONTH);
        int maxCell = totalDays + firstDay;

        int modelMonth = mModel.getCalendarMonth().get(Calendar.MONTH);
        int modelYear = mModel.getCalendarMonth().get(Calendar.YEAR);

        Calendar c = Calendar.getInstance();
        int currentDate = c.get(Calendar.DAY_OF_MONTH);
        int currentMonth = c.get(Calendar.MONTH);
        int currentYear = c.get(Calendar.YEAR);

        boolean isCurrentMonth = (modelMonth == currentMonth) && (modelYear == currentYear);

        boolean pastCurrentDate = false;


        for (int day = 0; day < 7; day++) {
            currentDayLabel = mCalendarData.getDayNameTextElements()[day];

            // Draw the first date element
            float x = currentDayLabel.getLastXValue() + (currentDayLabel.getWidth() / 2);
            float y = yOrigin + elementSpace;
            DateElement currentElement = dateElements[day];

            if (day < firstDay - 1)
                currentElement.setTextPaint(mEmptyDatePaint);
            else {
                int thisDay = day - (firstDay - 2);

                if (mModel.getDatesWithEntries().contains(thisDay)) {
                    currentElement.setHasEntries(true);
                    currentElement.setTextPaint(mStreakPaint);

                    boolean isAStreak = (day == 0 && mModel.getDatesWithEntries().contains(thisDay - 1)) ||
                            mModel.getDatesWithEntries().contains(thisDay + 1);

                    currentElement.setIsAStreak(isAStreak);
                }
                else
                    currentElement.setTextPaint(mCalendarBackgroundPaint);

                if (!pastCurrentDate && isCurrentMonth && thisDay == currentDate) {
                    pastCurrentDate = true;
                    currentElement.setIsCurrentDay(true);
                }

                mDateElementPaint.setAlpha(thisDay > currentDate && isCurrentMonth ? 77 : 255);
                TextElement text = new TextElement(String.valueOf(thisDay), mDateElementPaint);
                text.makeMeasurements();
                currentElement.setDateText(text);
            }

            if (currentElement.getIsAStreak()) {
                float x1 = x;
                float x2 = mContentWidth;

                if (day + 1 < mCalendarData.getDayNameTextElements().length) {
                    TextElement element = mCalendarData.getDayNameTextElements()[day + 1];
                    x2 = element.getLastXValue();
                }
                int thisDay = day - (firstDay - 2);
                if (day == 0 && mModel.getDatesWithEntries().contains(thisDay - 1)) {
                    x1 = 0;
                    x2 = mModel.getDatesWithEntries().contains(thisDay + 1) ? x2 : x;
                }

                currentElement.drawStreakLine(canvas, y, x1, x2);
            }

            currentElement.draw(canvas, x, y);

            // Draw the rest of the date elements in this column
            for (int row = 1; row < 6; row++) {
                y = currentElement.getLastYValue() + currentElement.getHeight() + elementSpace;
                int dayIndex = day + (row * 7);
                currentElement = dateElements[dayIndex];

                if (dayIndex >= maxCell - 1)
                    currentElement.setTextPaint(mEmptyDatePaint);
                else {
                    int thisDay = dayIndex - (firstDay - 2);
                    if (mModel.getDatesWithEntries().contains(thisDay)) {
                        currentElement.setHasEntries(true);
                        currentElement.setTextPaint(mStreakPaint);

                        boolean isAStreak = (day == 0 && mModel.getDatesWithEntries().contains(thisDay - 1)) ||
                                mModel.getDatesWithEntries().contains(thisDay + 1);

                        currentElement.setIsAStreak(isAStreak);
                    }
                    else
                        currentElement.setTextPaint(mCalendarBackgroundPaint);

                    mDateElementPaint.setAlpha(thisDay > currentDate && isCurrentMonth ? 77 : 255);
                    TextElement text = new TextElement(String.valueOf(thisDay), mDateElementPaint);
                    text.makeMeasurements();
                    currentElement.setDateText(text);

                    if (!pastCurrentDate && isCurrentMonth && thisDay == currentDate) {
                        pastCurrentDate = true;
                        currentElement.setIsCurrentDay(true);
                    }
                }

                if (currentElement.getIsAStreak()) {
                    float x1 = x;
                    float x2 = mContentWidth;

                    if (day + 1 < mCalendarData.getDayNameTextElements().length) {
                        TextElement element = mCalendarData.getDayNameTextElements()[day + 1];
                        x2 = element.getLastXValue();
                    }
                    int thisDay = dayIndex - (firstDay - 2);
                    if (day == 0 && mModel.getDatesWithEntries().contains(thisDay - 1)) {
                        x1 = 0;
                        x2 = mModel.getDatesWithEntries().contains(thisDay + 1) ? x2 : x;
                    }

                    currentElement.drawStreakLine(canvas, y, x1, x2);
                }
                currentElement.draw(canvas, x, y);
            }
        }
    }
    //endregion

    //region Setters {}
    public void setStreakColor(int streakColor) {
        mStreakColor = streakColor;
        if (mStreakPaint != null)
            mStreakPaint.setColor(streakColor);
    }
    //endregion -- end --

}
