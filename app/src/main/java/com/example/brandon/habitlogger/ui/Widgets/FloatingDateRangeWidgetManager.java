package com.example.brandon.habitlogger.ui.Widgets;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.ui.Dialogs.MyDatePickerDialog;

import java.util.Collections;
import java.util.List;

/**
 * Created by Brandon on 1/29/2017.
 * Manages the functionality of the date range widget.
 */

public class FloatingDateRangeWidgetManager {

    //region (Member attributes)
    private AppCompatActivity mActivity;
    private ViewHolder mViewHolder;

    private PreferenceChecker mPreferenceChecker;

    private long mDateFromTime, mDateToTime, mMinimumTime, mMaximumTime;
    public boolean mIsShown = true;
    //endregion

    //region Code responsible for providing an interface
    private DateRangeChangeListener mListener;

    public interface DateRangeChangeListener {
        void onDateRangeChanged(long dateFrom, long dateTo);
    }

    public void setDateRangeChangeListener(DateRangeChangeListener listener) {
        this.mListener = listener;
    }

    public void notifyDateRangeChanged(long timeFrom, long timeTo) {
        if (mListener != null) {
            mListener.onDateRangeChanged(timeFrom, timeTo);
        }
    }
    //endregion

    public class ViewHolder {
        public Spinner rangeType;
        public EditText dateFrom, dateTo;
        public TextView entriesCountText, totalTimeText;
        public CardView rootView;

        public ViewHolder(View view) {
            rangeType = (Spinner) view.findViewById(R.id.date_range_type_spinner);
            dateFrom = (EditText) view.findViewById(R.id.date_from);
            dateTo = (EditText) view.findViewById(R.id.date_to);
            entriesCountText = (TextView) view.findViewById(R.id.entries_count_text);
            totalTimeText = (TextView) view.findViewById(R.id.total_time_text);
            rootView = (CardView) view;
        }
    }

    public FloatingDateRangeWidgetManager(AppCompatActivity activity, View floatingDateRangeCardView,
                                          List<SessionEntry> sessionEntries) {

        mViewHolder = new ViewHolder(floatingDateRangeCardView);
        mActivity = activity;
        mPreferenceChecker = new PreferenceChecker(activity);

        mViewHolder.dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(true, mDateFromTime);
            }
        });

        mViewHolder.dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(false, mDateToTime);
            }
        });

        mViewHolder.rangeType.setOnItemSelectedListener(getOnDateRangeTypeSelectedListener());

        updateSessionEntries(sessionEntries);
        updateMinMaxTimestamps(sessionEntries);
        setStartRange(true);
    }

    public void showDatePickerDialog(final boolean shouldSetDateFrom, long currentTime) {
        MyDatePickerDialog dialog;

        if (shouldSetDateFrom)
            dialog = MyDatePickerDialog.newInstance(-1, mDateToTime - DateUtils.DAY_IN_MILLIS, currentTime);
        else
            dialog = MyDatePickerDialog.newInstance(mDateFromTime + DateUtils.DAY_IN_MILLIS, -1, currentTime);

        dialog.setOnFinishedListener(new MyDatePickerDialog.OnFinishedListener() {
            @Override
            public void onFinished(long time) {
                if (shouldSetDateFrom) {
                    if (time < mDateToTime)
                        mDateFromTime = time;
                }
                else {
                    if (time > mDateFromTime)
                        mDateToTime = time;
                }
                updateDateRangeLabels(true);
            }
        });
        dialog.show(mActivity.getSupportFragmentManager(), "text");
    }

    //region Methods responsible for handling events
    private AdapterView.OnItemSelectedListener getOnDateRangeTypeSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                onItemSelectedMethod(position, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };
    }

    private void onItemSelectedMethod(int position, boolean shouldNotify) {
        // Time presets: Year, Month, Week, Day in milliseconds
        final long MONTH_IN_MILLIS = DateUtils.YEAR_IN_MILLIS / 12;
        long timePresets[] = new long[]{DateUtils.YEAR_IN_MILLIS, MONTH_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0};
        if (position != 0 && position < 5) {
            setPresetDateRange(timePresets[position - 1], shouldNotify);
        }

        else {
            switch (position) {
                case 0: {
                    setStartRange(shouldNotify);
                }
                break;

                case 5: {
                    setCustomRange(shouldNotify);
                }
                break;
            }
        }
    }
    //endregion -- end --

    //region Methods responsible for hiding/showing the widget
    public void setViewShown(boolean shouldShow) {
        if (shouldShow) showView(true);
        else hideView(true);
    }

    public void hideView(boolean animate) {
        if (animate){
            mViewHolder.rootView.animate()
                    .setStartDelay(0)
                    .setDuration(250)
                    .alpha(0)
                    .translationY(-mViewHolder.rootView.getHeight());
        }
        else {
            mViewHolder.rootView.setTranslationY(mViewHolder.rootView.getHeight());
            mViewHolder.rootView.setAlpha(0);
        }

        mIsShown = false;
    }

    public void showView(boolean animate) {
        if (animate){
            mViewHolder.rootView.animate()
                    .setStartDelay(0)
                    .setDuration(250)
                    .alpha(1)
                    .translationY(0);
        }
        else {
            mViewHolder.rootView.setTranslationY(0);
            mViewHolder.rootView.setAlpha(1);
        }

        mIsShown = true;
    }
    //endregion -- end --

    //region Methods responsible for updating the ui
    public void entryChanged(SessionEntry oldEntry, SessionEntry newEntry) {
        if (newEntry.getStartingTime() > mMaximumTime) {
            this.mMaximumTime = MyTimeUtils.setTimePortion(newEntry.getStartingTime(), false, 11, 59, 59, 999);
            refreshDateRange(false);
        }
        else if (newEntry.getStartingTime() < mMinimumTime) {
            this.mMinimumTime = MyTimeUtils.setTimePortion(newEntry.getStartingTime(), true, 0, 0, 0, 0);
            refreshDateRange(false);
        }
    }

    public void updateSessionEntries(List<SessionEntry> sessionEntries) {
        int numberOfEntries = sessionEntries.size();
        mViewHolder.entriesCountText.setText(String.valueOf(numberOfEntries));

        long totalDuration = (long) MyCollectionUtils.sum(sessionEntries, SessionEntry.IGetSessionDuration);
        String totalTimeString = SessionEntry.stringifyDuration(totalDuration);
        mViewHolder.totalTimeText.setText(totalTimeString);

        updateMinMaxTimestamps(sessionEntries);
        refreshDateRange(false);
    }

    private void refreshDateRange(boolean shouldNotifyListeners) {
        int pos = mViewHolder.rangeType.getSelectedItemPosition();
        onItemSelectedMethod(pos, shouldNotifyListeners);
    }

    private void updateDateRangeLabels(boolean shouldNotifyListeners) {
        mViewHolder.dateTo.setText(MyTimeUtils.stringifyTimestamp(mDateToTime, mPreferenceChecker.stringGetDateFormat()));
        mViewHolder.dateFrom.setText(MyTimeUtils.stringifyTimestamp(mDateFromTime, mPreferenceChecker.stringGetDateFormat()));

        if (shouldNotifyListeners)
            notifyDateRangeChanged(mDateFromTime, mDateToTime);
    }

    private void updateMinMaxTimestamps(List<SessionEntry> sessionEntries) {
        if (sessionEntries != null && !sessionEntries.isEmpty()) {
            mMinimumTime = Collections.min(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            mMinimumTime = MyTimeUtils.setTimePortion(this.mMinimumTime, true, 0, 0, 0, 0);

            mMaximumTime = Collections.max(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            mMaximumTime = MyTimeUtils.setTimePortion(this.mMinimumTime, false, 11, 59, 59, 999);
        }
        else {
            this.mMinimumTime = 0;
            this.mMaximumTime = 0;
        }
    }
    //endregion -- end --

    //region Setters {}
    private void setDateRangeEnabled(boolean state) {
        mViewHolder.dateFrom.setEnabled(state);
        mViewHolder.dateTo.setEnabled(state);
    }

    public void setPresetDateRange(long time, boolean shouldNotifyListeners) {
        setDateRangeEnabled(false);

        mDateToTime = MyTimeUtils.setTimePortion(System.currentTimeMillis(), false, 11, 59, 59, 999);
        mDateFromTime = MyTimeUtils.setTimePortion(mDateToTime - time, true, 0, 0, 0, 0);
        updateDateRangeLabels(shouldNotifyListeners);
    }

    public void setStartRange(boolean shouldNotifyListeners) {
        setDateRangeEnabled(false);

        mDateToTime = MyTimeUtils.setTimePortion(System.currentTimeMillis(), false, 11, 59, 59, 999);
        mDateFromTime = mMinimumTime;
        updateDateRangeLabels(shouldNotifyListeners);
    }

    public void setCustomRange(boolean shouldNotifyListeners) {
        setDateRangeEnabled(true);
        updateDateRangeLabels(shouldNotifyListeners);
    }
    //endregion

    //region Getters {}
    public long getDateFrom() {
        return mDateFromTime;
    }

    public long getDateTo() {
        return mDateToTime;
    }
    //endregion

}
