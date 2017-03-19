package com.example.brandon.habitlogger.OverviewActivity.CalendarView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 */

public class CalendarViewAdapter extends RecyclerView.Adapter<CalendarViewAdapter.ViewHolder> {

    Context mContext;
    SessionEntriesSample mEntriesSample;
    List<CalendarViewMonthModel> monthData;
    private int mStreakColor = -1;

    public class ViewHolder extends RecyclerView.ViewHolder {
        CalendarView calendarView;

        public ViewHolder(View view) {
            super(view);
            calendarView = (CalendarView) view.findViewById(R.id.calendar_view);
            if (mStreakColor != -1)
                calendarView.setStreakColor(mStreakColor);
        }
    }

    public CalendarViewAdapter(SessionEntriesSample entriesSample, int streakColor, Context context) {
        mEntriesSample = entriesSample;
        mContext = context;
        setStreakColor(streakColor);

        generateMonthDataFromEntries(mEntriesSample);
    }

    private void generateMonthDataFromEntries(SessionEntriesSample entriesSample) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(entriesSample.dateFromTime);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(entriesSample.dateToTime);

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH) + 1;

        monthData = new ArrayList<>(diffMonth);

        int entryIndex = 0;
        List<SessionEntry> entries = entriesSample.getSessionEntries();

        Set<Integer> dates;
        List<SessionEntry> monthEntries;

        for (int month = 0; month < diffMonth; month++) {
            Calendar calendar = Calendar.getInstance();
            dates = new HashSet<>();
            monthEntries = new ArrayList<>();


            int targetMonth = startCalendar.get(Calendar.MONTH);
            int targetYear = startCalendar.get(Calendar.YEAR);
            calendar.set(Calendar.MONTH, targetMonth);
            calendar.set(Calendar.YEAR, targetYear);

            while (entryIndex < entries.size()) {
                SessionEntry entry = entries.get(entryIndex);

                if (entry.getStartingTimeMonth() == targetMonth && entry.getStartingTimeYear() == targetYear) {
                    dates.add(entry.getDateOfEntry());
                    monthEntries.add(entry);
                }
                else break;

                entryIndex++;
            }

            List<CalendarPieDataSet> pieDataSet = getPieDataSets(monthEntries, dates);

            monthData.add(new CalendarViewMonthModel(calendar, dates, pieDataSet));

            startCalendar.add(Calendar.MONTH, 1);
        }
    }

    private List<CalendarPieDataSet> getPieDataSets(List<SessionEntry> monthEntries, Set<Integer> dates) {
        List<CalendarPieDataSet> dataSets = new ArrayList<>(dates.size());

        for(int date : dates){
            dataSets.add(getPieDataSet(monthEntries, date));
        }

        return dataSets;
    }

    private CalendarPieDataSet getPieDataSet(List<SessionEntry> monthEntries, int date) {
        List<CalendarPieDataSet.CalendarPieEntry> entries = new ArrayList<>();

        float numberOfWedges = 3f;

        float value = 1 / numberOfWedges;

        for(float totalValue = 0; totalValue < 1f; totalValue+=value) {
            entries.add(new CalendarPieDataSet.CalendarPieEntry(value, MyColorUtils.getRandomColor()));
        }

        return new CalendarPieDataSet(entries, date);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_overview_calendar_view, parent, false);

        if (mStreakColor != -1)
            ((CalendarView) itemView).setStreakColor(mStreakColor);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarViewMonthModel model = monthData.get(position);
        holder.calendarView.bindModel(model);
    }

    public void setStreakColor(int color) {
        mStreakColor = color;
    }

    @Override
    public int getItemCount() {
        return monthData.size();
    }
}
