package com.example.brandon.habitlogger.OverviewActivity.CalendarView;

import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 *
 */

public class CalendarViewMonthModel {

    private Calendar mCalendarMonth;
    private Set<Integer> mDatesWithEntries;
    private List<CalendarPieDataSet> mPieData;
    private SimpleDateFormat titleFormat = new SimpleDateFormat("MMMM, yyyy", Locale.US);

    /**
     * @param calendar A calendar object representing the month to render
     * @param datesWithEntries A list of date's with entries available.
     */
    public CalendarViewMonthModel(Calendar calendar, Set<Integer> datesWithEntries, @Nullable List<CalendarPieDataSet> pieData){
        mCalendarMonth = calendar;
        mDatesWithEntries = datesWithEntries;
        mPieData = pieData;
    }

    public CalendarPieDataSet getPieDataSetForDate(int thisDay) {
        for(CalendarPieDataSet dataSet : mPieData){
            if(dataSet.getDate() == thisDay)
                return dataSet;
        }

        return null;
    }

    //region Getters ()
    public Calendar getCalendarMonth(){
        return mCalendarMonth;
    }

    public int getFirstWeekDay(){
        mCalendarMonth.set(Calendar.DAY_OF_MONTH, mCalendarMonth.getActualMinimum(Calendar.DAY_OF_MONTH));
        return mCalendarMonth.get(Calendar.DAY_OF_WEEK);
    }

    public String getMonthTitle() {
        return titleFormat.format(mCalendarMonth.getTimeInMillis());
    }

    public Set<Integer> getDatesWithEntries(){
        return mDatesWithEntries;
    }
    //endregion

    //region Setters ()
    public void setCalendarMonth(Calendar calendarMonth){
        mCalendarMonth = calendarMonth;
    }

    public void setDatesWithEntries(Set<Integer> datesWithEntries){
        mDatesWithEntries = datesWithEntries;
    }
    //endregion

}
