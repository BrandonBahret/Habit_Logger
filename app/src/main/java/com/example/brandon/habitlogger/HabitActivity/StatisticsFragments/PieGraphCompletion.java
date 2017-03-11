package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitActivity.UpdateEntriesInterface;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.FragmentPieGraphCompletionBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class PieGraphCompletion extends Fragment implements UpdateEntriesInterface {

    private FragmentPieGraphCompletionBinding ui;
    CallbackInterface callbackInterface;

    public PieGraphCompletion() {
        // Required empty public constructor
    }

    public static PieGraphCompletion newInstance() {
        return new PieGraphCompletion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_pie_graph_completion, container, false);
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
        updateEntries(callbackInterface.getSessionEntries());
    }

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {

        if (!dataSample.isEmpty()) {
            float ratio = 1.0f;
            int dayCount = 0;
            int totalDays = 0;

            List<SessionEntry> entries = dataSample.getSessionEntries();
            Collections.sort(entries, SessionEntry.StartingTimeComparator);
            final long DAY_IN_MILLI = 86400000L;

            long totalTime = dataSample.dateToTime - dataSample.dateFromTime;
            totalDays = (int) (totalTime / DAY_IN_MILLI);

            if (totalDays > 0) {
                long targetDate = dataSample.dateFromTime;

                for (SessionEntry entry : entries) {
                    long currentDate = entry.getStartingTimeDate();

                    if (currentDate == targetDate) {
                        dayCount++;
                        targetDate += DAY_IN_MILLI;
                    }
                    else if (currentDate > targetDate) {
                        dayCount++;
                        targetDate = currentDate + DAY_IN_MILLI;
                    }
                }

                ratio = dayCount / (float) totalDays;

            }
            else {
                ratio = 1;
            }

            setPieData(ratio);
            setTextLabels(dayCount, totalDays);
        }
    }

    private void setTextLabels(int dayCount, int totalDays) {
        ui.completedDaysText.setText(String.valueOf(dayCount));
        ui.totalDaysText.setText(String.valueOf(totalDays));
    }

    public void setPieData(float ratio){
        ui.chart.setUsePercentValues(true);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.setRotationEnabled(false);
        ui.chart.setExtraOffsets(4, 3, 0, 3);
        ui.chart.setCenterText(String.format(Locale.US, "%.2f%%", ratio * 100));
        ui.chart.setCenterTextSize(25);
        ui.chart.setRotationAngle(-195f);
        ui.chart.setHoleRadius(75f);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(ratio * 100));
        entries.add(new PieEntry((1-ratio) * 100));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(0f);

        int completedColor = callbackInterface.getDefaultColor();
        int skippedColor = ColorUtils.setAlphaComponent(callbackInterface.getDefaultColor(), (int)(255 * 0.3));
        int holeColor = ColorUtils.setAlphaComponent(callbackInterface.getDefaultColor(), (int)(255 * 0.03));

        dataSet.setColors(completedColor, skippedColor);
        ui.chart.setHoleColor(holeColor);

        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.TRANSPARENT);

        ui.chart.setData(data);
        ui.chart.getLegend().setEnabled(false);
    }
}