package com.example.brandon.habitlogger.CustomCalendar.OverviewCalendarView;

import android.support.annotation.Nullable;

import com.example.brandon.habitlogger.CustomCalendar.CalendarViewModelBase;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 * An extension of the base calendar view model to display pie charts
 */

public class CalendarViewMonthModel extends CalendarViewModelBase {

    private List<CalendarPieDataSet> mPieData;

    /**
     * @param calendar A calendar object representing the month to render
     * @param datesWithEntries A list of date's with entries available.
     */
    public CalendarViewMonthModel(Calendar calendar, Set<Integer> datesWithEntries, @Nullable List<CalendarPieDataSet> pieData){
        super(calendar, datesWithEntries);
        mPieData = pieData;
    }

    public CalendarPieDataSet getPieDataSetForDate(int thisDay) {
        for(CalendarPieDataSet dataSet : mPieData){
            if(dataSet.getDate() == thisDay)
                return dataSet;
        }

        return null;
    }

}
