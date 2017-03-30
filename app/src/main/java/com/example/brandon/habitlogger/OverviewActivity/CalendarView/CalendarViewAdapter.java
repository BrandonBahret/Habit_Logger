package com.example.brandon.habitlogger.OverviewActivity.CalendarView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDataSample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 */

public class CalendarViewAdapter extends RecyclerView.Adapter<CalendarViewAdapter.ViewHolder> {

    Context mContext;
    HabitDataSample mDataSample;
    List<CalendarViewMonthModel> calendarData;

    public class ViewHolder extends RecyclerView.ViewHolder {
        CalendarView calendarView;

        public ViewHolder(View view) {
            super(view);
            calendarView = (CalendarView) view.findViewById(R.id.calendar_view);
        }
    }

    public CalendarViewAdapter(HabitDataSample habitDataSample, Context context) {
        mDataSample = habitDataSample;
        mContext = context;

        generateMonthDataFromEntries(mDataSample);
    }

    private void generateMonthDataFromEntries(HabitDataSample dataSample) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(dataSample.getDateFrom());

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(dataSample.getDateTo());

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = (diffYear * 12) + (endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)) + 1;

        calendarData = new ArrayList<>(diffMonth);

        int entryIndex = 0;
        List<SessionEntry> entries = dataSample.buildSessionEntriesList().getSessionEntries();

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
            calendarData.add(new CalendarViewMonthModel(calendar, dates, pieDataSet));

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_overview_calendar_view, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarViewMonthModel model = calendarData.get(position);
        holder.calendarView.bindModel(model);
    }

    @Override
    public int getItemCount() {
        return calendarData.size();
    }
}
