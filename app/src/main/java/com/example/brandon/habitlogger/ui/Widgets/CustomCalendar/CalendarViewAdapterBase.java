package com.example.brandon.habitlogger.ui.Widgets.CustomCalendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.common.MyTimeUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Brandon on 3/17/2017.
 * Base class for all custom calendar view adapters
 */

public abstract class CalendarViewAdapterBase<ViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ViewHolder> {

    protected Context mContext;
    protected List<CalendarViewModelBase> mCalendarData;

    public CalendarViewAdapterBase(Context context) {
        mContext = context;
    }

    protected abstract ViewHolder onCreateViewHolder(LayoutInflater layoutInflater, ViewGroup parent);

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return onCreateViewHolder(layoutInflater, parent);
    }

    protected abstract void bindModel(ViewHolder holder, CalendarViewModelBase model);

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarViewModelBase model = mCalendarData.get(position);
        bindModel(holder, model);
    }

    @Override
    public int getItemCount() {
        return mCalendarData.size();
    }

    public int getAdapterPositionForCurrentMonth() {
        final Calendar currentMonth = Calendar.getInstance();

        for (int pos = 0; pos < mCalendarData.size(); pos++) {
            Calendar eachMonth = mCalendarData.get(pos).getCalendarMonth();
            if(MyTimeUtils.isSameMonthOfYear(eachMonth, currentMonth))
                return pos;
        }

        return -1;
    }

}
