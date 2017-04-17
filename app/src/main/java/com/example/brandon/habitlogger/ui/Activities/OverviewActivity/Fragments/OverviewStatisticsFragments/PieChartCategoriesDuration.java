package com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewStatisticsFragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.CategoryDataCollection;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.HabitDataCollection;
import com.example.brandon.habitlogger.databinding.FragmentPieChartCategoriesDurationBinding;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.IDataOverviewCallback;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PieChartCategoriesDuration extends Fragment implements IDataOverviewCallback.IUpdateHabitSample {

    //region (Member attributes)
    FragmentPieChartCategoriesDurationBinding ui;
    IDataOverviewCallback ICallback;
    private int mTextColor;
    //endregion

    public PieChartCategoriesDuration() {
        // Required empty public constructor
    }

    //region Methods responsible for handling the lifecycle of the fragment

    //region Methods (onCreateView - onDestroyView)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_pie_chart_categories_duration, container, false);

        mTextColor = ContextCompat.getColor(getContext(), R.color.textColor3);
        ui.chart.setCenterText("All Categories");
        ui.chart.setCenterTextColor(mTextColor);
        ui.chart.setCenterTextSize(16);
        ui.chart.setHoleRadius(55f);
        ui.chart.setEntryLabelColor(Color.TRANSPARENT);
        ui.chart.setHoleColor(Color.TRANSPARENT);
        ui.chart.setExtraOffsets(4, 8, 0, 3);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.getLegend().setWordWrapEnabled(true);
        ui.chart.getLegend().setTextColor(mTextColor);

        return ui.getRoot();
    }
    //endregion -- end --

    //region methods (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ICallback = (IDataOverviewCallback) context;
        ICallback.addCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ICallback.removeCallback(this);
    }
    //endregion -- end --

    //region methods (onStart - onStop)
    @Override
    public void onStart() {
        super.onStart();
        updateHabitDataSample(ICallback.getDataSample());
    }
    //endregion -- end --

    //endregion --- end ---

    @Override
    public void updateHabitDataSample(HabitDataCollection dataSample) {
        List<PieEntry> entries = new ArrayList<>();
        final long totalDuration = dataSample.calculateTotalDuration();

        for (CategoryDataCollection categoryDataSample : dataSample.getData()) {
            float ratio = 100 * categoryDataSample.calculateTotalDuration() / (float) totalDuration;
            if (ratio > 0) {
                String categoryName = categoryDataSample.getCategoryName();
                int categoryColor = categoryDataSample.getCategory().getColorAsInt();
                entries.add(new PieEntry(ratio, categoryName, categoryColor));
            }
        }

        Collections.sort(entries, new Comparator<PieEntry>() {
            @Override
            public int compare(PieEntry entryOne, PieEntry entryTwo) {
                return entryOne.getLabel().compareTo(entryTwo.getLabel());
            }
        });

        setPieData(entries);
    }

    public void setPieData(List<PieEntry> entries) {

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(0f);
        dataSet.setValueTextColor(mTextColor);
        dataSet.setValueLineColor(mTextColor);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineVariableLength(true);
        dataSet.setColors(collectColorsFromEntries(entries));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        ui.chart.setData(data);
    }

    private List<Integer> collectColorsFromEntries(List<PieEntry> entries) {
        return MyCollectionUtils.collect(entries, new MyCollectionUtils.IGetKey<PieEntry, Integer>() {
            @Override
            public Integer get(PieEntry entry) {
                return (Integer) entry.getData();
            }
        });
    }

}
