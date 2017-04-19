package com.example.brandon.habitlogger.ui.Widgets;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
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
    private String mNoDataString;

    private long mDateFromTime, mDateToTime, mMinimumTime, mMaximumTime;
    public boolean mIsShown = true;
    private int mLastPosition = -1;

    private static final String KEY_DATE_FROM = "KEY_DATE_FROM";
    private static final String KEY_DATE_TO = "KEY_DATE_TO";
    private static final String KEY_MIN_TIME = "KEY_MIN_TIME";
    private static final String KEY_MAX_TIME = "KEY_MAX_TIME";
    private static final String KEY_IS_SHOWN = "KEY_IS_SHOWN";
    private static final String KEY_LAST_POS = "KEY_LAST_POS";
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
        mNoDataString = activity.getString(R.string.no_data_available_lower);

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
        setStartRange(true);
    }

    public void showDatePickerDialog(final boolean shouldSetDateFrom, long currentTime) {
        MyDatePickerDialog dialog;

        if (shouldSetDateFrom)
            dialog = MyDatePickerDialog.newInstance(-1, currentTime);
        else
            dialog = MyDatePickerDialog.newInstance(mDateFromTime + DateUtils.DAY_IN_MILLIS, currentTime);

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

    public boolean entryFitsRange(SessionEntry entry) {
        long startingTime = entry.getStartingTime();
        return (getDateTo() >= startingTime) && (startingTime >= getDateFrom());
    }

    //region Methods responsible for handling events
    private AdapterView.OnItemSelectedListener getOnDateRangeTypeSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                boolean lastPositionSet = mLastPosition != -1;
                onItemSelectedMethod(position, lastPositionSet);
                mLastPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };
    }

    private void onItemSelectedMethod(int position, boolean shouldNotify) {

        if (mMinimumTime != -1 && mMaximumTime != -1) {

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
        else updateDateRangeLabels(false);

    }
    //endregion -- end --

    //region Methods responsible for hiding/showing the widget
    public void setViewShown(boolean shouldShow) {
        if (shouldShow) showView(true);
        else hideView(true);
    }

    public void hideView(boolean animate) {
        if (animate) {
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
        if (animate) {
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

    public void adjustDateRangeForEntry(SessionEntry entry) {
        if (entry.getStartingTime() > mMaximumTime) {
            this.mMaximumTime = MyTimeUtils.setTimePortion(entry.getStartingTime(), false, 11, 59, 59, 999);
            refreshDateRange(false);
        }
        else if (entry.getStartingTime() < mMinimumTime) {
            this.mMinimumTime = entry.getStartingTimeIgnoreTimeOfDay();
            refreshDateRange(false);
        }
    }

//    public void entryAdded(SessionEntry newEntry) {
//        if (newEntry.getStartingTime() > mMaximumTime) {
//            this.mMaximumTime = MyTimeUtils.setTimePortion(newEntry.getStartingTime(), false, 11, 59, 59, 999);
//            refreshDateRange(false);
//        }
//        else if (newEntry.getStartingTime() < mMinimumTime) {
//            this.mMinimumTime = newEntry.getStartingTimeIgnoreTimeOfDay();
//            refreshDateRange(false);
//        }
//    }

    private void updateSessionEntries(List<SessionEntry> sessionEntries) {
        int numberOfEntries = sessionEntries.size();
        mViewHolder.entriesCountText.setText(String.valueOf(numberOfEntries));

        long totalDuration = (long) MyCollectionUtils.sum(sessionEntries, SessionEntry.IGetSessionDuration);
        String totalTimeString = SessionEntry.stringifyDuration(totalDuration);
        mViewHolder.totalTimeText.setText(totalTimeString);
        updateMinMaxTimestamps(sessionEntries);
    }

    public void updateSessionEntries(SessionEntryCollection sessionEntries) {
        updateSessionEntries(sessionEntries, sessionEntries.getMinimumTime(), sessionEntries.getMaximumTime());
        sessionEntries.setDateFrom(getDateFrom());
        sessionEntries.setDateTo(getDateTo());
    }

    public void updateSessionEntries(int numberOfEntries, long entriesDuration, long minimumTime, long maximumTime) {
        mViewHolder.entriesCountText.setText(String.valueOf(numberOfEntries));

        String totalTimeString = SessionEntry.stringifyDuration(entriesDuration);
        mViewHolder.totalTimeText.setText(totalTimeString);

        mMinimumTime = minimumTime;
        mMaximumTime = maximumTime;
        refreshDateRange(false);
    }

    public void updateSessionEntries(List<SessionEntry> sessionEntries, long minimumTime, long maximumTime) {
        int numberOfEntries = sessionEntries.size();
        mViewHolder.entriesCountText.setText(String.valueOf(numberOfEntries));

        long totalDuration = (long) MyCollectionUtils.sum(sessionEntries, SessionEntry.IGetSessionDuration);
        String totalTimeString = SessionEntry.stringifyDuration(totalDuration);
        mViewHolder.totalTimeText.setText(totalTimeString);

        mMinimumTime = minimumTime;
        mMaximumTime = maximumTime;
        refreshDateRange(false);
    }

    private void refreshDateRange(boolean shouldNotifyListeners) {
        int pos = mViewHolder.rangeType.getSelectedItemPosition();
        onItemSelectedMethod(pos, shouldNotifyListeners);
    }

    private void updateDateRangeLabels(boolean shouldNotifyListeners) {
        if (mMinimumTime == -1 || mMaximumTime == -1) {
            mViewHolder.dateTo.setText(mNoDataString);
            mViewHolder.dateFrom.setText(mNoDataString);
            setDateRangeEnabled(false);
        }
        else {
            mViewHolder.dateTo.setText(MyTimeUtils.stringifyTimestamp(mDateToTime, mPreferenceChecker.stringGetDateFormat()));
            mViewHolder.dateFrom.setText(MyTimeUtils.stringifyTimestamp(mDateFromTime, mPreferenceChecker.stringGetDateFormat()));

            if (shouldNotifyListeners)
                notifyDateRangeChanged(mDateFromTime, mDateToTime);
        }
    }

    private void updateMinMaxTimestamps(List<SessionEntry> sessionEntries) {
        if (sessionEntries != null && !sessionEntries.isEmpty()) {
            mMinimumTime = Collections.min(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            mMinimumTime = MyTimeUtils.setTimePortion(this.mMinimumTime, true, 0, 0, 0, 0);

            mMaximumTime = Collections.max(sessionEntries, SessionEntry.ICompareStartingTimes).getStartingTime();
            mMaximumTime = MyTimeUtils.setTimePortion(this.mMinimumTime, false, 11, 59, 59, 999);
        }
        else {
            mMinimumTime = -1;
            mMaximumTime = -1;
        }
    }
    //endregion -- end --

    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(KEY_DATE_FROM, mDateFromTime);
        outState.putLong(KEY_DATE_TO, mDateToTime);
        outState.putLong(KEY_MIN_TIME, mMinimumTime);
        outState.putLong(KEY_MAX_TIME, mMaximumTime);
        outState.putBoolean(KEY_IS_SHOWN, mIsShown);
        outState.putLong(KEY_LAST_POS, mLastPosition);
    }

    public void restoreState(Bundle savedInstanceState) {
        mDateFromTime = savedInstanceState.getLong(KEY_DATE_FROM);
        mDateToTime = savedInstanceState.getLong(KEY_DATE_TO);
        mMinimumTime = savedInstanceState.getLong(KEY_MIN_TIME);
        mMaximumTime = savedInstanceState.getLong(KEY_MAX_TIME);
        mIsShown = savedInstanceState.getBoolean(KEY_IS_SHOWN);
        mLastPosition = (int) savedInstanceState.getLong(KEY_LAST_POS);
    }

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

        mDateFromTime = mMinimumTime;
        mDateToTime = MyTimeUtils.setTimePortion(System.currentTimeMillis(), false, 11, 59, 59, 999);
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
