package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.OverviewCalendarView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewAdapterBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewModelBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 * Class for adapting this calendar view to a recycler view
 */

public class CalendarViewAdapter extends CalendarViewAdapterBase<CalendarViewAdapter.ViewHolder> {

    HabitDataSample mDataSample;

    //region Code responsible for creating and binding view holders
    public class ViewHolder extends RecyclerView.ViewHolder {
        CalendarView calendarView;

        public ViewHolder(View view) {
            super(view);
            calendarView = (CalendarView) view.findViewById(R.id.calendar_view);
        }
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater layoutInflater, ViewGroup parent) {
        View itemView = layoutInflater.inflate(R.layout.data_overview_calendar_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    protected void bindModel(ViewHolder holder, CalendarViewModelBase model) {
        holder.calendarView.setModel(model);
    }
    //endregion -- end --

    public CalendarViewAdapter(HabitDataSample habitDataSample, Context context) {
        super(context);
        mDataSample = habitDataSample;
        generateMonthDataFromEntries(mDataSample);
    }

    private void generateMonthDataFromEntries(HabitDataSample dataSample) {

        Calendar startCalendar = Calendar.getInstance();
        long minimumTime = dataSample.getMinimumTime();
        minimumTime = minimumTime == -1 ? System.currentTimeMillis() : minimumTime;
        startCalendar.setTimeInMillis(minimumTime);
//        startCalendar.setTimeInMillis(dataSample.getDateFrom());

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(DateUtils.YEAR_IN_MILLIS * 200);
//        endCalendar.setTimeInMillis(dataSample.getDateTo());

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = (diffYear * 12) + (endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)) + 1;

        mCalendarData = new ArrayList<>(diffMonth);

        int entryIndex = 0;
        List<SessionEntry> entries = dataSample.buildSessionEntriesList().asList();

        Set<Integer> dates;

        for (int month = 0; month < diffMonth; month++) {
            dates = new HashSet<>();

            int targetYear = startCalendar.get(Calendar.YEAR);
            int targetMonth = startCalendar.get(Calendar.MONTH);

            while (entryIndex < entries.size()) {
                SessionEntry entry = entries.get(entryIndex);

                if (entry.getStartingTimeMonth() == targetMonth && entry.getStartingTimeYear() == targetYear)
                    dates.add(entry.getStartingTimeDayOfMonth());

                else break;

                entryIndex++;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.YEAR, targetYear);
            calendar.set(Calendar.MONTH, targetMonth);

            List<CalendarPieDataSet> pieDataSet = getPieDataSets(targetMonth, targetYear, dates);
            mCalendarData.add(new CalendarViewMonthModel(calendar, dates, pieDataSet));

            startCalendar.add(Calendar.MONTH, 1);
        }
    }

    private List<CalendarPieDataSet> getPieDataSets(int month, int year, Set<Integer> dates) {
        List<CalendarPieDataSet> dataSets = new ArrayList<>(dates.size());

        Integer datesArray[] = new Integer[dates.size()];
        dates.toArray(datesArray);
        Arrays.sort(datesArray);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        MyTimeUtils.setTimePortion(c, true, 0, 0, 0, 0);

        for (int date : datesArray) {
            c.set(Calendar.DAY_OF_MONTH, date);
            dataSets.add(getPieDataSet(mDataSample.getDataSampleForDate(c.getTimeInMillis()), date));
        }

        return dataSets;
    }

    public CalendarPieDataSet getPieDataSet(HabitDataSample dataSample, int date) {
        final long totalDuration = dataSample.calculateTotalDuration();
        List<CalendarPieDataSet.CalendarPieEntry> entries = new ArrayList<>();

        for (CategoryDataSample categoryDataSample : dataSample.getData()) {
            float ratio = categoryDataSample.calculateTotalDuration() / (float) totalDuration;
            if (ratio > 0) {
                entries.add(new CalendarPieDataSet.CalendarPieEntry(ratio, categoryDataSample.getCategory().getColorAsInt()));
            }
        }

        return new CalendarPieDataSet(entries, date);
    }
}
