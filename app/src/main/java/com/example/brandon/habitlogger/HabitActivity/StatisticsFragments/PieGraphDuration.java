package com.example.brandon.habitlogger.HabitActivity.StatisticsFragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.IHabitCallback;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.databinding.FragmentPieGraphDurationBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;


public class PieGraphDuration extends Fragment implements IHabitCallback.IUpdateCategorySample {

    private FragmentPieGraphDurationBinding ui;
    IHabitCallback callbackInterface;

    public PieGraphDuration() {
        // Required empty public constructor
    }

    public static PieGraphDuration newInstance() {
        return new PieGraphDuration();
    }

    //region Methods responsible for handling the fragment lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_pie_graph_duration, container, false);

        ui.chart.setUsePercentValues(true);
        ui.chart.setExtraOffsets(4, 8, 0, 3);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.setCenterTextSize(15);
        ui.chart.setHoleRadius(65f);
        ui.chart.setHoleColor(Color.TRANSPARENT);
        ui.chart.setHighlightPerTapEnabled(true);
        ui.chart.getLegend().setWordWrapEnabled(true);
        ui.chart.setDrawEntryLabels(false);

        return ui.getRoot();
    }

    //region (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (IHabitCallback) context;
        callbackInterface.addUpdateCategoryDataSampleCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackInterface.removeUpdateCategoryDataSampleCallback(this);
    }
    //endregion

    @Override
    public void onStart() {
        super.onStart();
        updateCategoryDataSample(callbackInterface.getCategoryDataSample());
    }
    //endregion

    @Override
    public void updateCategoryDataSample(CategoryDataSample dataSample) {
        float[] durationRatios = new float[dataSample.getNumberOfHabits()];

        for (int i = 0; i < durationRatios.length; i++)
            durationRatios[i] = dataSample.getHabitDuration(i) / (float) dataSample.calculateTotalDuration() * 100;

        setPieData(durationRatios, dataSample);
    }

    public void setPieData(float[] durationRatios, CategoryDataSample dataSample) {
        HabitCategory category = dataSample.getCategory();
        ui.chart.setCenterText(category.getName());

        List<PieEntry> entries = new ArrayList<>(durationRatios.length);

        for (int i = 0; i < durationRatios.length; i++) {
            float durationRatio = durationRatios[i];

            String label = dataSample.getHabit(i).getName();
            entries.add(new PieEntry(durationRatio, label));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(0f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineVariableLength(true);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        ui.chart.setData(data);
    }

}
