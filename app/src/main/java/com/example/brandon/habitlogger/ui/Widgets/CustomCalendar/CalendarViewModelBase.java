package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar;

import com.example.brandon.habitlogger.common.MyTimeUtils;

import java.util.Calendar;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 * The base data model for displaying custom calendar views
 */

public class CalendarViewModelBase {

    //region (Member attributes)
    private Calendar mCalendarMonth;
    private Set<Integer> mDatesWithEntries;
    //endregion

    /**
     * @param calendar A calendar object representing the month to render
     * @param datesWithEntries A list of date's with entries available.
     */
    public CalendarViewModelBase(Calendar calendar, Set<Integer> datesWithEntries){
        mCalendarMonth = calendar;
        mDatesWithEntries = datesWithEntries;
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
        return MyTimeUtils.stringifyTimestamp(mCalendarMonth.getTimeInMillis(), "MMMM, yyyy");
    }

    public Set<Integer> getDatesWithEntries(){
        return mDatesWithEntries;
    }
    //endregion

}
