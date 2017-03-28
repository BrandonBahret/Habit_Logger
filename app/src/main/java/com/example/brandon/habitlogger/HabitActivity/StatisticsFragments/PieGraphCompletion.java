package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.CallbackInterface;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.FragmentPieGraphCompletionBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class PieGraphCompletion extends Fragment implements CallbackInterface.IUpdateEntries {

    private FragmentPieGraphCompletionBinding ui;
    CallbackInterface callbackInterface;

    private int mChartHoleColor;
    private int mCompletedColor;
    private int mSkippedColor;

    public PieGraphCompletion() {
        // Required empty public constructor
    }

    public static PieGraphCompletion newInstance() {
        return new PieGraphCompletion();
    }

    //region Methods responsible for handling fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_pie_graph_completion, container, false);

        ui.chart.setUsePercentValues(true);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.setRotationEnabled(false);
        ui.chart.setExtraOffsets(4, 3, 0, 3);
        ui.chart.setCenterTextSize(25);
        ui.chart.setRotationAngle(-195f);
        ui.chart.setHoleRadius(75f);
        ui.chart.getLegend().setEnabled(false);

        return ui.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (CallbackInterface) context;
        callbackInterface.addUpdateEntriesCallback(this);

        mCompletedColor = callbackInterface.getDefaultColor();
        mSkippedColor = ColorUtils.setAlphaComponent(mCompletedColor, 85);
        mChartHoleColor = ColorUtils.setAlphaComponent(mCompletedColor, 10);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateEntries(callbackInterface.getSessionEntries());
    }
    //endregion

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {
        if (!dataSample.isEmpty()) {
            Set<Long> uniqueEntryStartDates = MyCollectionUtils.listToSet(
                    dataSample.getSessionEntries(), SessionEntry.IGetSessionStartDate
            );

            int totalDaysWithEntries = uniqueEntryStartDates.size();
            int totalDays = dataSample.calculateTotalDaysLength() + 1;
            float ratio = totalDaysWithEntries / (float) totalDays * 100;

            setPieData(totalDaysWithEntries, totalDays, ratio);
        }
    }

    public void setPieData(int totalDaysWithEntries, int totalDays, float ratio) {
        ui.completedDaysText.setText(String.valueOf(totalDaysWithEntries));
        ui.totalDaysText.setText(String.valueOf(totalDays));
        ui.chart.setCenterText(String.format(Locale.US, "%.2f%%", ratio));

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(ratio));
        entries.add(new PieEntry(100 - ratio));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(0f);

        ui.chart.setHoleColor(mChartHoleColor);
        dataSet.setColors(mCompletedColor, mSkippedColor);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        ui.chart.setData(data);
    }
}
