package com.example.brandon.habitlogger.CustomCalendar.OverviewCalendarView;


import android.text.TextPaint;

import com.example.brandon.habitlogger.CustomCalendar.CalendarViewDataBase;
import com.example.brandon.habitlogger.CustomCalendar.DateElementBase;

/**
 * Created by Brandon on 4/3/2017.
 * Class for defining the layout of this custom calendar view
 */

public class CalendarViewData extends CalendarViewDataBase {

    @Override
    protected DateElementBase getNewDateElement(TextPaint paint) {
        return new DateElement(paint, null, null);
    }

}
