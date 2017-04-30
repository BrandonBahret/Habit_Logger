package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.databinding.FragmentPieGraphCompletionBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class PieGraphCompletion extends Fragment {

    private static String KEY_COLOR = "KEY_COLOR";

    //region (Member attributes)
    private ThemeColorPalette mColorPalette;
    private int mChartHoleColor;
    private int mCompletedColor;
    private int mSkippedColor;
    private StatisticData mStatisticData;

    private FragmentPieGraphCompletionBinding ui;
    IHabitDataCallback.IUpdateEntries mCallbackInterface;
    //endregion

    public PieGraphCompletion() {
        // Required empty public constructor
    }

    public static PieGraphCompletion newInstance(ThemeColorPalette colorPalette) {

        PieGraphCompletion fragment = new PieGraphCompletion();

        Bundle args = new Bundle();
        args.putSerializable(KEY_COLOR, colorPalette);
        fragment.setArguments(args);

        return fragment;
    }

    //region Methods responsible for handling fragment lifecycle

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IHabitDataCallback.IUpdateEntries) {
            mCallbackInterface = (IHabitDataCallback.IUpdateEntries) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement IHabitDataCallback.IUpdateEntries");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackInterface = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColorPalette = (ThemeColorPalette) getArguments().getSerializable(KEY_COLOR);
    }

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
    public void onStart() {
        super.onStart();
        updateEntries(mCallbackInterface.getSessionEntries());
        updateColor(mColorPalette);
    }
    //endregion

    public void updateEntries(SessionEntryCollection dataSample) {
        if (!dataSample.isEmpty()) {
            Set<Long> uniqueEntryStartDates = MyCollectionUtils.collectIntoSet(
                    dataSample.asList(), SessionEntry.IGetSessionStartDate
            );

            int totalDaysWithEntries = uniqueEntryStartDates.size();
            int totalDays = dataSample.calculateTotalDaysLength();
            float ratio = totalDaysWithEntries / (float) totalDays * 100;

            mStatisticData = new StatisticData(totalDaysWithEntries, totalDays, ratio);
            setPieData(mStatisticData);
        }
    }

    private void setPieData(StatisticData data) {
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

    public void updateColor(ThemeColorPalette colorPalette) {
        mColorPalette = colorPalette;
        mCompletedColor = colorPalette.getBaseColor();
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
