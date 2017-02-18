package com.example.brandon.habitlogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitActivity.StartingDateDialog;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.TimeFormatUtils.TimeDisplay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Brandon on 1/29/2017.
 * Manages the functionality of the date range widget.
 */

public class FloatingDateRangeWidgetManager {

    private AppCompatActivity activity;

    private ViewHolder viewHolder;
    private PreferenceChecker preferenceChecker;

    private long dateFromTime, dateToTime, minimumTime;

    private DateRangeChangeListener listener;
    public boolean isShown = true;


    public interface DateRangeChangeListener{
        void dateRangeChanged(long dateFrom, long dateTo);
    }

    public class ViewHolder{
        public Spinner rangeType;
        public EditText dateFrom, dateTo;
        public TextView entriesCountText, totalTimeText;
        public CardView view;

        public ViewHolder(View view){
            rangeType = (Spinner)view.findViewById(R.id.date_range_type_spinner);
            dateFrom  = (EditText)view.findViewById(R.id.date_from);
            dateTo    = (EditText)view.findViewById(R.id.date_to);
            entriesCountText = (TextView)view.findViewById(R.id.entries_count_text);
            totalTimeText = (TextView)view.findViewById(R.id.total_time_text);
            this.view = (CardView)view;
        }
    }

    public FloatingDateRangeWidgetManager(AppCompatActivity activity_, View floatingDateRangeCardView,
                                          List<SessionEntry> sessionEntries){

        if(floatingDateRangeCardView instanceof CardView) {
            this.viewHolder = new ViewHolder(floatingDateRangeCardView);
            this.activity = activity_;
            this.preferenceChecker = new PreferenceChecker(activity);

            viewHolder.dateFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(true, dateFromTime);
                }
            });

            viewHolder.dateTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(false, dateToTime);
                }
            });

            viewHolder.rangeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    // Time presets: Year, Month, Week, Day in milliseconds
                    long timePresets[] = new long[]{31536000000L, 2592000000L, 604800000L, 86400000L};
                    if(i !=0 && i < 5){
                        setPresetDateRange(timePresets[i - 1]);
                    }

                    else{
                        switch(i){
                            case 0:{
                                setStartRange();
                            }break;

                            case 5:{
                                setCustomRange();
                            }break;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            updateSessionEntries(sessionEntries);
        }

        else {
            throw new IllegalArgumentException(
                    "FloatingDateRangeWidgetManager wasn't given a card view"
            );
        }
    }

    public void showDialog(final boolean setDateFromTime, long currentTime){
        StartingDateDialog dialog = new StartingDateDialog();

        Bundle args = new Bundle();
        args.putLong("date_in_milliseconds", currentTime);

        if(setDateFromTime)
            args.putLong("date_max", dateToTime - 86400000L);
        else
            args.putLong("date_min", dateFromTime + 86400000L);

        dialog.setArguments(args);

        dialog.setOnFinishedListener(new StartingDateDialog.OnFinishedListener() {
            @Override
            public void onFinishedWithResult(String monthName, int day, int year, long time) {
                if(setDateFromTime) {
                    if(time < dateToTime)
                        dateFromTime = time;
                }
                else{
                    if(time > dateFromTime)
                        dateToTime = time;
                }
                updateDateRangeLabels();
            }
        });
        dialog.show(activity.getSupportFragmentManager(), "text");
    }

    public void setDateRangeChangeListener(DateRangeChangeListener listener){
        this.listener = listener;
    }

    public void notifyDateRangeChanged(long timeFrom, long timeTo){
        if(listener != null){
            listener.dateRangeChanged(timeFrom, timeTo);
        }
    }

    private long getCurrentTime(){
        return System.currentTimeMillis();
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return formatter.format(new Date(milliSeconds));
    }

    public void hideView(){
        viewHolder.view.animate()
                .setStartDelay(0)
                .setDuration(250)
                .alpha(0)
                .translationY(-viewHolder.view.getHeight());

        isShown = false;
    }

    public void showView(){
        viewHolder.view.animate()
                .setStartDelay(0)
                .setDuration(250)
                .alpha(1)
                .translationY(0);
        isShown = true;
    }

    private void setDateRangeEnabled(boolean state){
        viewHolder.dateFrom.setEnabled(state);
        viewHolder.dateTo.setEnabled(state);
    }

    private void updateDateRangeLabels(){
        viewHolder.dateTo.setText(getDate(dateToTime, preferenceChecker.stringGetDateFormat()));
        viewHolder.dateFrom.setText(getDate(dateFromTime, preferenceChecker.stringGetDateFormat()));

        notifyDateRangeChanged(dateFromTime, dateToTime);
    }

    public void updateSessionEntries(List<SessionEntry> sessionEntries) {
        int numberOfEntries = sessionEntries.size();

        this.minimumTime = sessionEntries.isEmpty()? 0 :
                sessionEntries.get(0).getStartTime();

        long totalDuration = 0;
        for (SessionEntry entry : sessionEntries) {
            totalDuration += entry.getDuration();
        }

        viewHolder.entriesCountText.setText(String.valueOf(numberOfEntries));
        String totalTimeString = TimeDisplay.getDisplay(totalDuration);
        viewHolder.totalTimeText.setText(totalTimeString);
    }

    public void setPresetDateRange(long time){
        setDateRangeEnabled(false);

        dateToTime = getCurrentTime();
        dateFromTime = dateToTime - time;
        updateDateRangeLabels();
    }

    public void setStartRange(){
        setDateRangeEnabled(false);

        dateToTime = getCurrentTime();
        dateFromTime = minimumTime;
        updateDateRangeLabels();
    }

    public void setCustomRange(){
        setDateRangeEnabled(true);
        updateDateRangeLabels();
    }

    public void setDateRangeForDate(int year, int month, int dayOfMonth) {
        viewHolder.rangeType.setSelection(5);

        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);

        dateToTime = c.getTimeInMillis();
        dateFromTime = dateToTime - 86400000L;

        updateDateRangeLabels();
    }

    public long getDateFrom(){
        return this.dateFromTime;
    }

    public long getDateTo(){
        return this.dateToTime;
    }
}
