package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;

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

import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitActivity.UpdateEntriesInterface;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.FragmentLineGraphCompletionBinding;
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


public class LineGraphCompletion extends Fragment implements UpdateEntriesInterface {

    private FragmentLineGraphCompletionBinding ui;
    CallbackInterface callbackInterface;

    public LineGraphCompletion() {
        // Required empty public constructor
    }

    public static LineGraphCompletion newInstance() {
        return new LineGraphCompletion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_line_graph_completion, container, false);
        return ui.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (CallbackInterface) context;
        callbackInterface.addCallback(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        SessionEntriesSample sample = callbackInterface.getSessionEntries();
        updateEntries(sample.sessionEntries, sample.dateFromTime, sample.dateToTime);
    }

    @Override
    public void updateEntries(List<SessionEntry> sessionEntries, long dateFrom, long dateTo) {
        if (!sessionEntries.isEmpty()) {
            setLineChartData(calculateDataSet(sessionEntries, dateFrom, dateTo));
        }
    }

    private void setLineChartData(List<Entry> values) {
        LineDataSet dataSet = new LineDataSet(values, "label");
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        int color = callbackInterface.getDefaultColor();
        dataSet.setFillColor(color);
        dataSet.setColor(color);

        LineData data = new LineData(dataSet);
        data.setDrawValues(false);
        data.setHighlightEnabled(false);

        ui.chart.setData(data);
        ui.chart.getLegend().setEnabled(false);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.getAxisRight().setMaxWidth(0f);
        ui.chart.getAxisRight().setEnabled(false);
        ui.chart.setPinchZoom(true);
        ui.chart.getViewPortHandler().setMaximumScaleX(10f);
        ui.chart.getViewPortHandler().setMaximumScaleY(10f);

        YAxis yAxis = ui.chart.getAxisLeft();
        XAxis xAxis = ui.chart.getXAxis();
        xAxis.setValueFormatter(new DateXAxisValueFormatter());
        xAxis.setLabelCount(8, true);
        yAxis.setAxisMinimum(0f); // start at zero
        yAxis.setGridColor(ColorUtils.setAlphaComponent(yAxis.getGridColor(), 50));
        xAxis.setGridColor(ColorUtils.setAlphaComponent(xAxis.getGridColor(), 50));
        yAxis.setValueFormatter(new PercentFormatter());
    }

    List<Entry> calculateDataSet(List<SessionEntry> sessionEntries, long dateFrom, long dateTo) {
        int totalDays = (int) ((dateTo - dateFrom) / DateUtils.DAY_IN_MILLIS);
        List<Entry> values = new ArrayList<>(totalDays);
        int dayCount = 0; // The number of days performed.
        int entryIndex = 0;
        long targetDate = dateFrom;

        for (int dateIndex = 0; dateIndex < totalDays; dateIndex++) {
            int index = findIndexForDate(sessionEntries, targetDate, entryIndex);
            if (index != -1) {
                entryIndex = index;
                dayCount++;
            }

            values.add(new HabitValue(targetDate, dayCount, totalDays));
            targetDate += DateUtils.DAY_IN_MILLIS;
        }

        return values;
    }

    public int findIndexForDate(List<SessionEntry> sessionEntries, long date, int entryIndexStart) {
        for (int index = entryIndexStart; index < sessionEntries.size(); index++) {
            long entryTime = sessionEntries.get(index).getStartingTimeDate();

            if (entryTime == date)
                return index;

            else if (entryTime > date)
                return -1;
        }

        return -1;
    }

    class HabitValue extends Entry {
        public long date;
        public int dateCount;
        public float ratio;

        public HabitValue(long date, int dateCount, int totalDays) {
            this.date = date;
            this.dateCount = dateCount;
            this.ratio = 100 * dateCount / (float) totalDays;

            setX(date / 3600000L);
            setY(ratio);
        }
    }

    public class DateXAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long millis = (long) (value * 3600000L);
            return SessionEntry.getDate(millis, "d/MMM");
        }
    }
}
