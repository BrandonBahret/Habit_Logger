package com.example.brandon.habitlogger.ui.Activities.HabitActivity.Fragments.StatisticsFragments;

/**
 * Chart description:
 * The x-axis: Starts and ends following the set date range
 * The y-axis: Maps values from 0% to the highest percentage in the data set.
 * <p>
 * The data set is generated via an iteration between the starting date to the ending date.
 * The value stored on each date is the "habit score" that would've been displayed for that day.
 */

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.FragmentLineGraphCompletionBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.IHabitCallback;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class LineGraphCompletion extends Fragment implements IHabitCallback.IUpdateEntries {

    private FragmentLineGraphCompletionBinding ui;
    IHabitCallback callbackInterface;
    private int mColor;

    public LineGraphCompletion() {
        // Required empty public constructor
    }

    public static LineGraphCompletion newInstance() {
        return new LineGraphCompletion();
    }

    //region Methods responsible for handling the fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_line_graph_completion, container, false);

        ui.chart.getLegend().setEnabled(false);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.setPinchZoom(true);
        ui.chart.getViewPortHandler().setMaximumScaleX(8.5f);
        ui.chart.getViewPortHandler().setMaximumScaleY(8.5f);

        //region Stylize the axes
        YAxis yAxis = ui.chart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setGridColor(ColorUtils.setAlphaComponent(yAxis.getGridColor(), 50));
        yAxis.setValueFormatter(new PercentFormatter());

        yAxis = ui.chart.getAxisRight();
        yAxis.setEnabled(false);

        XAxis xAxis = ui.chart.getXAxis();
        ui.chart.setVisibleXRangeMaximum(5);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setGridColor(ColorUtils.setAlphaComponent(xAxis.getGridColor(), 50));
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long timestamp = (long) (value * DateUtils.HOUR_IN_MILLIS);
                return MyTimeUtils.stringifyTimestamp(timestamp, "d/MMM");
            }
        });
        //endregion

        return ui.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (IHabitCallback) context;
        callbackInterface.addUpdateEntriesCallback(this);

        mColor = callbackInterface.getDefaultColor();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateEntries(callbackInterface.getSessionEntries());
    }
    //endregion

    //region Methods responsible for generating the line chart data.
    List<Entry> calculateDataSet(SessionEntriesSample dataSample) {

        int totalDays = dataSample.calculateTotalDaysLength();
        List<Entry> values = new ArrayList<>(totalDays);

        long targetDate = dataSample.getDateFromTime();
        int dayCounter = 0; // The number of mDays performed.

        for (int dateIndex = 0; dateIndex < totalDays; dateIndex++) {

            if (hasIndexForDate(dataSample.getSessionEntries(), targetDate))
                dayCounter++;

            float ratio = dayCounter / (float) totalDays * 100;
            values.add(new Entry(targetDate / DateUtils.HOUR_IN_MILLIS, ratio));

            targetDate += DateUtils.DAY_IN_MILLIS;
        }

        return values;
    }

    public boolean hasIndexForDate(List<SessionEntry> sessionEntries, final long searchDate) {
        return MyCollectionUtils.binarySearch(sessionEntries, searchDate, new MyCollectionUtils.KeyComparator() {
            @Override
            public int compare(Object element, Object key) {
                return Long.compare(((SessionEntry) element).getStartingTimeIgnoreTimeOfDay(), (long) key);
            }
        }) >= 0;
    }
    //endregion

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {
        if (!dataSample.isEmpty())
            setLineChartData(calculateDataSet(dataSample));
    }

    private void setLineChartData(List<Entry> values) {
        LineDataSet dataSet = new LineDataSet(values, "");
        //region Stylize the data set
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        dataSet.setFillColor(mColor);
        dataSet.setColor(mColor);
        //endregion

        LineData data = new LineData(dataSet);
        data.setDrawValues(false);
        data.setHighlightEnabled(false);

        ui.chart.setData(data);
    }

}
