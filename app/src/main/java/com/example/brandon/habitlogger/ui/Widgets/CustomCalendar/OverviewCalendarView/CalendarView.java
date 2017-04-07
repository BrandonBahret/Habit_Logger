package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.OverviewCalendarView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewDataBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.TextElement;
import com.example.brandon.habitlogger.common.MyColorUtils;

import java.util.Calendar;

/**
 * A rootView to represent entry data of a habit in a month-long calendar space.
 */

public class CalendarView extends CalendarViewBase {

    private TextPaint mEmptyDateElementPaint;

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
    protected void onGatherAttributes(TypedArray attributes) {}

    @Override
    protected void onCreatePaintObjects() {
        mEmptyDateElementPaint = new TextPaint();
    }

    @Override
    protected void onInvalidateTextPaintAndMeasurements() {
        float emptyColorLight = MyColorUtils.getLightness(mEmptyDateColor);
        emptyColorLight = Math.max(emptyColorLight, emptyColorLight + 0.15f);
        int emptyColor = MyColorUtils.setLightness(mEmptyDateColor, emptyColorLight);
        mEmptyDateElementPaint.setColor(emptyColor);
    }
    //endregion -- end --

    //region Methods responsible for drawing the view
    @Override
    public void onDrawOverBase(Canvas canvas) {
        drawDateElements(canvas);
    }

    private void drawDateElements(Canvas canvas) {
        CalendarViewData calendarData = (CalendarViewData) mCalendarData;
        CalendarViewMonthModel model = (CalendarViewMonthModel) mModel;

        DateElement dateElements[] = calendarData.getDateElements();

        TextElement currentDayLabel = calendarData.getDayNameTextElements()[0];
        float yOrigin = currentDayLabel.getLastYValue() + currentDayLabel.getHeight() + calendarData.getContentMarginTop();
        float calendarHeight = mContentHeight - yOrigin;

        float totalCalendarElementsHeight = (dateElements[0].getDiameter()) * 6;
        float elementSpace = (calendarHeight - totalCalendarElementsHeight) / 6f;

        int firstDay = model.getFirstWeekDay();
        int totalDays = model.getCalendarMonth().getActualMaximum(Calendar.DAY_OF_MONTH);
        int maxCell = totalDays + firstDay;

        int modelMonth = model.getCalendarMonth().get(Calendar.MONTH);
        int modelYear = model.getCalendarMonth().get(Calendar.YEAR);

        Calendar c = Calendar.getInstance();
        int currentDate = c.get(Calendar.DAY_OF_MONTH);
        int currentMonth = c.get(Calendar.MONTH);
        int currentYear = c.get(Calendar.YEAR);

        boolean isCurrentMonth = (modelMonth == currentMonth) && (modelYear == currentYear);

        boolean pastCurrentDate = false;

        for (int day = 0; day < 7; day++) {
            currentDayLabel = calendarData.getDayNameTextElements()[day];

            // Draw the first date element
            float x = currentDayLabel.getLastXValue() + (currentDayLabel.getWidth() / 2);
            float y = yOrigin + elementSpace;
            DateElement currentElement = dateElements[day];

            if (day < firstDay - 1)
                currentElement.setTextPaint(mEmptyDatePaint);

            else {
                int thisDay = day - (firstDay - 2);

                currentElement.setTextPaint(mEmptyDateElementPaint);

                if (!pastCurrentDate && isCurrentMonth && thisDay == currentDate) {
                    pastCurrentDate = true;
                    currentElement.setIsCurrentDay(true);
                }

                mDateElementPaint.setAlpha(thisDay > currentDate && isCurrentMonth ? 77 : 255);
                TextElement text = new TextElement(String.valueOf(thisDay), mDateElementPaint);
                text.makeMeasurements();
                currentElement.setDateText(text);
                currentElement.setPieData(model.getPieDataSetForDate(thisDay));
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

                    currentElement.setTextPaint(mEmptyDateElementPaint);

                    mDateElementPaint.setAlpha(thisDay > currentDate && isCurrentMonth ? 77 : 255);
                    TextElement text = new TextElement(String.valueOf(thisDay), mDateElementPaint);
                    text.makeMeasurements();
                    currentElement.setDateText(text);
                    currentElement.setPieData(model.getPieDataSetForDate(thisDay));

                    if (!pastCurrentDate && isCurrentMonth && thisDay == currentDate) {
                        pastCurrentDate = true;
                        currentElement.setIsCurrentDay(true);
                    }
                }

                currentElement.draw(canvas, x, y);
            }
        }
    }
    //endregion -- end --

}