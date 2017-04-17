package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.databinding.FragmentDistributionStartingTimeBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DistributionStartingTime extends Fragment {

    private static String KEY_CALLBACK = "KEY_CALLBACK";
    private static String KEY_COLOR = "KEY_COLOR";

    //region (Member attributes)
    IHabitDataCallback.IUpdateEntries mCallbackInterface;
    FragmentDistributionStartingTimeBinding ui;
    private ThemeColorPalette mColorPalette;
    private int mTextColor;
    private List<BarEntry> mEntries = new ArrayList<>();
    private final int INTERVAL = 60;
    //endregion

    public static DistributionStartingTime newInstance(IHabitDataCallback.IUpdateEntries callback, ThemeColorPalette colorPalette) {

        DistributionStartingTime fragment = new DistributionStartingTime();

        Bundle args = new Bundle();
        args.putSerializable(KEY_CALLBACK, callback);
        args.putSerializable(KEY_COLOR, colorPalette);
        fragment.setArguments(args);

        return fragment;
    }

    //region [ ---- Methods responsible for handling the fragment lifecycle ---- ]

    //region Methods (onCreateView - onDestroyView)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallbackInterface = (IHabitDataCallback.IUpdateEntries) getArguments().getSerializable(KEY_CALLBACK);
        mColorPalette = (ThemeColorPalette) getArguments().getSerializable(KEY_COLOR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_distribution_starting_time, container, false);
        ui.chart.setScaleEnabled(true);
        ui.chart.setScaleYEnabled(false);
        ui.chart.getViewPortHandler().setMaximumScaleX(4f);
        mTextColor = ContextCompat.getColor(getContext(), R.color.textColor3);
        return ui.getRoot();
    }
    //endregion -- end --

    //region Methods (onStart - onStop)
    @Override
    public void onStart() {
        super.onStart();
        updateEntries(mCallbackInterface.getSessionEntries());
    }
    //endregion -- end --

    //endregion [ ---------------- end ---------------- ]

    public void updateEntries(SessionEntryCollection dataSample) {

        if (!dataSample.isEmpty()) {
            TimeIntervalEntryCounter entryCounter = new TimeIntervalEntryCounter(INTERVAL);

            for (SessionEntry entry : dataSample.asList())
                entryCounter.incrementCounter(entry.getStartingTimePortion());


            int totalEntriesCount = dataSample.asList().size();
            List<BarEntry> entries = new ArrayList<>();
            for (int counterIndex = 0; counterIndex < entryCounter.COUNTERS_LENGTH; counterIndex++) {
                float ratio = entryCounter.getCounter(counterIndex) / (float) totalEntriesCount * 100;
                entries.add(new BarEntry(counterIndex, ratio));
            }

            setDistributionData(entries);
        }
    }

    private void setDistributionData(List<BarEntry> entries) {

        //region // Disable features

        ui.chart.getLegend().setEnabled(false);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.setHighlightFullBarEnabled(false);

        //endregion // Disable features

        //region Y-Axis
        ui.chart.getAxisLeft().setValueFormatter(new PercentFormatter());
        ui.chart.getAxisLeft().setTextColor(mTextColor);
        ui.chart.getAxisLeft().setAxisMinimum(0.0f);
        ui.chart.getAxisRight().setEnabled(false);
        //endregion -- end --

        //region X-Axis
        ui.chart.getXAxis().setLabelRotationAngle(-45);
        ui.chart.getXAxis().setTextColor(mTextColor);
        ui.chart.getXAxis().setLabelCount(8, false);
        ui.chart.getXAxis().setDrawGridLines(false);
        ui.chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float counterIndex, AxisBase axis) {
                long time = (long) counterIndex * INTERVAL * DateUtils.MINUTE_IN_MILLIS;
                return MyTimeUtils.stringifyTimePortion(time, "h a");
            }
        });
        ui.chart.getXAxis().setGranularity(1f);
        ui.chart.getXAxis().setGranularityEnabled(true);
        ui.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        //endregion -- end --

        ui.chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        CombinedData data = new CombinedData();

        //region // Add data to CombinedData
        mEntries = entries;
        BarDataSet dataSet = new BarDataSet(entries, "label");
        dataSet.setColor(mColorPalette.getBaseColor());
        dataSet.setValueTextColor(mTextColor);
        dataSet.setHighlightEnabled(false);
        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (value > 0)
                    return String.format(Locale.getDefault(), "%d%%", (int) value);
                else return "";
            }
        });

        data.setData(barData);

//        data.setData(generateBellCurveData(entries, INTERVAL));
        //endregion

        ui.chart.setData(data);
        ui.chart.invalidate();
    }

    public void updateColor(ThemeColorPalette colorPalette) {
        mColorPalette = colorPalette;
        setDistributionData(mEntries);
    }

    class TimeIntervalEntryCounter {
        private final int DAY_IN_MINUTES = 1440;
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
}
