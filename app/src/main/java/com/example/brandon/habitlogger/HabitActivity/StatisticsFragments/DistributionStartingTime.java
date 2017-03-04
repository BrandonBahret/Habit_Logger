package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitActivity.UpdateEntriesInterface;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.TimeDisplay;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.FragmentDistributionStartingTimeBinding;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DistributionStartingTime extends Fragment implements UpdateEntriesInterface {
    CallbackInterface callbackInterface;
    FragmentDistributionStartingTimeBinding ui;

    //region // Methods responsible for handling the fragment lifecycle

    //region // Methods (onCreateView - onDestroyView)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_distribution_starting_time, container, false);
        return ui.getRoot();
    }

    //endregion // Methods (onCreateView - onDestroyView)

    //region // Methods (onAttach - onDetach)

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbackInterface = (CallbackInterface) context;
        callbackInterface.addCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackInterface.removeCallback(this);
    }

    //endregion // Methods (onAttach - onDetach)

    //region // Methods (onStart - onStop)

    @Override
    public void onStart() {
        super.onStart();
        updateEntries(callbackInterface.getSessionEntries());
    }

    //endregion // Methods (onStart - onStop)

    //endregion // Methods responsible for handling the fragment lifecycle

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {

        if (!dataSample.isEmpty()) {
            int interval = 60;
            TimeIntervalEntryCounter entryCounter = new TimeIntervalEntryCounter(interval);

            for (SessionEntry entry : dataSample.getSessionEntries())
                entryCounter.incrementCounter(entry.getStartingTimePortion());


            final int totalEntriesCount = dataSample.getSessionEntries().size();

            List<BarEntry> entries = new ArrayList<>();
            for (int counterIndex = 0; counterIndex < entryCounter.COUNTERS_LENGTH; counterIndex++) {
                float ratio = entryCounter.getCounter(counterIndex) / (float) totalEntriesCount * 100;
                entries.add(new BarEntry(counterIndex, ratio));
            }

            setDistributionData(entries, interval);
        }
    }

    private void setDistributionData(List<BarEntry> entries, final int interval) {
        BarDataSet dataSet = new BarDataSet(entries, "label");
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new MyPercentFormatter());
        ui.chart.setData(data);

        // Enable features
//        ui.chart.setPinchZoom(true);

        // Disable features
        ui.chart.getLegend().setEnabled(false);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.setHighlightFullBarEnabled(false);

        // Set Axis settings
        // Y-Axis
        ui.chart.getAxisLeft().setValueFormatter(new PercentFormatter());
        ui.chart.getAxisLeft().setAxisMinimum(0.0f);
        ui.chart.getAxisRight().setEnabled(false);

        // X-Axis
        ui.chart.getXAxis().setLabelRotationAngle(-45);
        ui.chart.getXAxis().setDrawGridLines(false);
        ui.chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float counterIndex, AxisBase axis) {
                long time = (long) counterIndex * interval * DateUtils.MINUTE_IN_MILLIS;
                return TimeDisplay.getTimeAsString(time, "h a");
            }
        });

        ui.chart.getXAxis().setSpaceMax(1f);
        ui.chart.getXAxis().setGranularity(1f);
        ui.chart.getXAxis().setGranularityEnabled(true);
        ui.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        ui.chart.invalidate();
    }

    class TimeIntervalEntryCounter {
        private final int DAY_IN_MINUTES = 1439;
        private final int COUNTERS_LENGTH;
        private int[] counters;
        private int timeIntervalInMinutes;

        public TimeIntervalEntryCounter(int timeIntervalInMinutes) {
            this.timeIntervalInMinutes = timeIntervalInMinutes;
            this.COUNTERS_LENGTH = (int) (DAY_IN_MINUTES / (float) timeIntervalInMinutes);
            this.counters = new int[COUNTERS_LENGTH];
        }

        private int getIndexForTimestamp(long timestampInMillis) {
            int timestampInMinutes = (int) (timestampInMillis / DateUtils.MINUTE_IN_MILLIS);
            return Math.min(COUNTERS_LENGTH - 1, (int) Math.floor(timestampInMinutes / timeIntervalInMinutes));
        }

        public int incrementCounter(long timestampInMillis) {
            return ++counters[getIndexForTimestamp(timestampInMillis)];
        }

        //region // Getters
        public int getTimeIntervalInMinutes() {
            return this.timeIntervalInMinutes;
        }

        public int getCounter(int counterIndex) {
            return this.counters[counterIndex];
        }
        //endregion
    }

    class MyPercentFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            if (value > 0)
                return new DecimalFormat("0.0").format(value);
            else
                return "";
        }
    }
}
