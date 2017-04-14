package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.databinding.FragmentPieGraphCompletionBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.IHabitCallback;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class PieGraphCompletion extends Fragment implements IHabitCallback.IUpdateEntries, IHabitCallback.IUpdateColor {

    private FragmentPieGraphCompletionBinding ui;
//    IHabitCallback callbackInterface;

    private int mChartHoleColor;
    private int mCompletedColor;
    private int mSkippedColor;
    private StatisticData mStatisticData;

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
        ui.chart.setRotationAngle(-180f);
        ui.chart.setHoleRadius(75f);
        ui.chart.getLegend().setEnabled(false);

        return ui.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        callbackInterface = (IHabitCallback) context;
//        callbackInterface.addUpdateEntriesCallback(this);
//        callbackInterface.addUpdateColorCallback(this);
//
//        updateColor(callbackInterface.getDefaultColor());
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        callbackInterface.removeUpdateEntriesCallback(this);
//        callbackInterface.removeUpdateColorCallback(this);
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateEntries(callbackInterface.getSessionEntries());
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

            mStatisticData = new StatisticData(totalDaysWithEntries, totalDays, ratio);
            setPieData(mStatisticData);
        }
    }

    public void setPieData(StatisticData data) {
        ui.completedDaysText.setText(String.valueOf(data.mTotalDaysWithEntries));
        ui.totalDaysText.setText(String.valueOf(data.mTotalDays));
        ui.chart.setCenterText(String.format(Locale.US, "%.2f%%", data.mRatio));
        ui.chart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.textColor3));

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(data.mRatio));
        entries.add(new PieEntry(100 - data.mRatio));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(0f);

        ui.chart.setHoleColor(mChartHoleColor);
        dataSet.setColors(mCompletedColor, mSkippedColor);

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(false);

        ui.chart.setData(pieData);
        ui.chart.invalidate();
    }

    @Override
    public void updateColor(int color) {
        mCompletedColor = color;
        mSkippedColor = ColorUtils.setAlphaComponent(mCompletedColor, 85);
        mChartHoleColor = ColorUtils.setAlphaComponent(mCompletedColor, 10);

        if (mStatisticData != null)
            setPieData(mStatisticData);
    }

    private class StatisticData {
        int mTotalDaysWithEntries;
        int mTotalDays;
        float mRatio;

        StatisticData(int totalDaysWithEntries, int totalDays, float ratio) {
            mTotalDaysWithEntries = totalDaysWithEntries;
            mTotalDays = totalDays;
            mRatio = ratio;
        }
    }
}
