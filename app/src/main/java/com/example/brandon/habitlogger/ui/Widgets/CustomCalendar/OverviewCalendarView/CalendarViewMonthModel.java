package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.OverviewCalendarView;

import android.support.annotation.Nullable;

import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewModelBase;
import com.example.brandon.habitlogger.common.MyCollectionUtils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 * An extension of the base calendar view mModel to display pie charts
 */

public class CalendarViewMonthModel extends CalendarViewModelBase {

    private List<CalendarPieDataSet> mPieData;

    //region Constructors {}
    /**
     * @param calendar A calendar object representing the month to render
     * @param datesWithEntries A list of date's with entries available.
     */
    public CalendarViewMonthModel(Calendar calendar, Set<Integer> datesWithEntries, @Nullable List<CalendarPieDataSet> pieData){
        super(calendar, datesWithEntries);
        mPieData = pieData;
    }

    /**
     * @param calendar A calendar object representing the month to render
     */
    public CalendarViewMonthModel(Calendar calendar, @Nullable List<CalendarPieDataSet> pieData){
        super(calendar, getDatesWithEntries(pieData));
        mPieData = pieData;
    }

    private static Set<Integer> getDatesWithEntries(@Nullable List<CalendarPieDataSet> pieData){
        return new HashSet<>(MyCollectionUtils.collect(pieData, new MyCollectionUtils.IGetKey<CalendarPieDataSet, Integer>() {
            @Override
            public Integer get(CalendarPieDataSet dataSet) {
                return dataSet.getDate();
            }
        }));
    }
    //endregion

    public CalendarPieDataSet getPieDataSetForDate(int thisDay) {
        for(CalendarPieDataSet dataSet : mPieData){
            if(dataSet.getDate() == thisDay)
                return dataSet;
        }

        return null;
    }

}
