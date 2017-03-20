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
    SessionEntriesSample mEntriesSample;
    List<CalendarViewMonthModel> calendarData;
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

        calendarData = new ArrayList<>(diffMonth);

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

            if (!monthEntries.isEmpty()) {
                List<CalendarPieDataSet> pieDataSet = getPieDataSets(monthEntries, dates);
                calendarData.add(new CalendarViewMonthModel(calendar, dates, pieDataSet));
            }

            startCalendar.add(Calendar.MONTH, 1);
        }
    }

    private List<CalendarPieDataSet> getPieDataSets(List<SessionEntry> monthEntries, Set<Integer> dates) {
        List<CalendarPieDataSet> dataSets = new ArrayList<>(dates.size());

//        Collections.sort(monthEntries, SessionEntry.StartingTimeComparator);

        int entryIndex = 0;

        Integer datesArray[] = new Integer[dates.size()];
        dates.toArray(datesArray);
        Arrays.sort(datesArray);

        for (int date : datesArray) {

            List<SessionEntry> dateEntries = new ArrayList<>();

            while (entryIndex < monthEntries.size()) {
                SessionEntry entry = monthEntries.get(entryIndex);
                if (entry.getStartingTimeDayOfMonth() == date) {
                    dateEntries.add(entry);
                    entryIndex++;
                }
                else break;
            }

            if (!dateEntries.isEmpty())
                dataSets.add(getPieDataSet(dateEntries, date));
        }

        return dataSets;
    }

    private CalendarPieDataSet getPieDataSet(List<SessionEntry> dateEntries, int date) {

        /* todo implement this method
        iterate the monthEntries and calculate a duration pie chart of all the categories
        */

        List<CalendarPieDataSet.CalendarPieEntry> entries = new ArrayList<>();

//        HabitDataSample dataSample = new HabitDataSample(dateEntries);

        SessionEntriesSample sample = new SessionEntriesSample(dateEntries);

        long totalDuration = sample.calculateDuration();



        for (SessionEntry entry : dateEntries) {

            entries.add(new CalendarPieDataSet.CalendarPieEntry(entry.getDuration() / (float) totalDuration, MyColorUtils.getRandomColor()));
        }

//        float numberOfWedges = 3f;
//
//        float value = 1 / numberOfWedges;
//
//        for(float totalValue = 0; totalValue < 1f; totalValue+=value) {
//            entries.add(new CalendarPieDataSet.CalendarPieEntry(value, MyColorUtils.getRandomColor()));
//        }

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
        CalendarViewMonthModel model = calendarData.get(position);
        holder.calendarView.bindModel(model);
    }

    public void setStreakColor(int color) {
        mStreakColor = color;
    }

    @Override
    public int getItemCount() {
        return calendarData.size();
    }
}
