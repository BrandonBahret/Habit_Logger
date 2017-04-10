package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewAdapterBase;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarViewModelBase;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.SessionEntriesSample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Brandon on 3/17/2017.
 * Class for adapting this calendar view to a recycler view
 */

public class CalendarViewAdapter extends CalendarViewAdapterBase<CalendarViewAdapter.ViewHolder> {

    SessionEntriesSample mEntriesSample;
    private int mStreakColor = -1;

    public CalendarViewAdapter(SessionEntriesSample entriesSample, int streakColor, Context context) {
        super(context);
        mEntriesSample = entriesSample;
        mStreakColor = streakColor;
        generateMonthDataFromEntries(mEntriesSample);
    }

    public void setColor(int color) {
        mStreakColor = color;
        this.notifyDataSetChanged();
    }

    //region Code responsible for creating and binding view holders
    public class ViewHolder extends RecyclerView.ViewHolder {
        CalendarView calendarView;

        public ViewHolder(View view) {
            super(view);
            calendarView = (CalendarView) view.findViewById(R.id.calendar_view);
            if (mStreakColor != -1)
                calendarView.setStreakColor(mStreakColor);
        }
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater layoutInflater, ViewGroup parent) {
        View itemView = layoutInflater.inflate(R.layout.calendar_view, parent, false);

        if (mStreakColor != -1)
            ((CalendarView) itemView).setStreakColor(mStreakColor);

        return new ViewHolder(itemView);
    }

    @Override
    protected void bindModel(ViewHolder holder, CalendarViewModelBase model) {
        holder.calendarView.setModel(model);
    }
    //endregion -- end --

    private void generateMonthDataFromEntries(SessionEntriesSample entriesSample) {
        Calendar startCalendar = Calendar.getInstance();
        long minimumTime = entriesSample.getMinimumTime();
        minimumTime = minimumTime == -1 ? System.currentTimeMillis() : minimumTime;
        startCalendar.setTimeInMillis(minimumTime);
//        startCalendar.setTimeInMillis(entriesSample.getDateFromTime());

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(DateUtils.YEAR_IN_MILLIS * 200);
//        endCalendar.setTimeInMillis(entriesSample.getDateToTime());

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH) + 1;

        mCalendarData = new ArrayList<>(diffMonth);

        int entryIndex = 0;
        List<SessionEntry> entries = entriesSample.getSessionEntries();

        Set<Integer> dates;

        for (int month = 0; month < diffMonth; month++) {
            dates = new HashSet<>();

            int targetMonth = startCalendar.get(Calendar.MONTH);
            int targetYear = startCalendar.get(Calendar.YEAR);

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

            mCalendarData.add(new CalendarViewModelBase(calendar, dates));

            startCalendar.add(Calendar.MONTH, 1);
        }
    }

}
