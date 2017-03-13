package com.example.brandon.habitlogger.OverviewActivity.OverviewStatisticsFragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.OverviewActivity.CallbackInterface;
import com.example.brandon.habitlogger.OverviewActivity.UpdateHabitDataSampleInterface;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.databinding.FragmentPieChartCategoriesDurationBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PieChartCategoriesDuration extends Fragment implements UpdateHabitDataSampleInterface {

    FragmentPieChartCategoriesDurationBinding ui;
    CallbackInterface callbackInterface;

    public PieChartCategoriesDuration() {
        // Required empty public constructor
    }

    //region // Methods responsible for handling the lifecycle of the fragment

    //region // Methods (onCreateView - onDestroyView)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_pie_chart_categories_duration, container, false);
        return ui.getRoot();
    }
    //endregion

    //region // methods (onAttach - onDetach)
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
    //endregion

    //region // methods (onStart - onStop)
    @Override
    public void onStart() {
        super.onStart();
        updateDataSample(callbackInterface.getDataSample());
    }
    //endregion // methods (onStart - onStop)

    //endregion

    @Override
    public void updateDataSample(HabitDataSample data) {
        final long totalDuration = data.calculateTotalDuration();
        List<PieEntry> entries = new ArrayList<>();

        for (CategoryDataSample categoryDataSample : data.getData()) {
            float ratio = 100 * categoryDataSample.calculateTotalDuration() / (float) totalDuration;
            if (ratio > 0) {
                entries.add(new PieEntry(ratio, categoryDataSample.getName(), categoryDataSample.getCategory().getColorAsInt()));
            }
        }

        Collections.sort(entries, new Comparator<PieEntry>() {
            @Override
            public int compare(PieEntry o1, PieEntry o2) {
                return Float.compare(o2.getValue(), o1.getValue());
            }
        });

        setPieData(entries);
    }

    private int[] getColorsFromEntries(List<PieEntry> entries) {
        int colors[] = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            colors[i] = (Integer)entries.get(i).getData();
        }
        return colors;
    }

    public void setPieData(List<PieEntry> entries) {
        ui.chart.setCenterText("All Categories");
        ui.chart.setEntryLabelColor(Color.TRANSPARENT);
        ui.chart.getDescription().setEnabled(false);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(0f);

        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        dataSet.setValueLineVariableLength(true);

        dataSet.setColors(getColorsFromEntries(entries));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        ui.chart.setData(data);
        Legend legend = ui.chart.getLegend();
        legend.setWordWrapEnabled(true);
    }
}
