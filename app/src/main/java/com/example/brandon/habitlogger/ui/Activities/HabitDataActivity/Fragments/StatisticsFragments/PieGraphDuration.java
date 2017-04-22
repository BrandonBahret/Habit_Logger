package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments;


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
import com.example.brandon.habitlogger.data.DataModels.DataCollections.CategoryDataCollection;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.databinding.FragmentPieGraphDurationBinding;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;


public class PieGraphDuration extends Fragment {

    //region (Member attributes)
    private FragmentPieGraphDurationBinding ui;
    private int mTextColor;
    IHabitDataCallback.IUpdateCategoryData mCallbackInterface;
    //endregion

    public PieGraphDuration() {
        // Required empty public constructor
    }

    public static PieGraphDuration newInstance() {
        return new PieGraphDuration();
    }

    //region Methods responsible for handling the fragment lifecycle
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IHabitDataCallback.IUpdateCategoryData) {
            mCallbackInterface = (IHabitDataCallback.IUpdateCategoryData) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement IHabitDataCallback.IUpdateCategoryData");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackInterface = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_pie_graph_duration, container, false);

        mTextColor = ContextCompat.getColor(getContext(), R.color.textColor3);
        ui.chart.setUsePercentValues(true);
        ui.chart.setExtraOffsets(4, 8, 0, 3);
        ui.chart.getDescription().setEnabled(false);
        ui.chart.setCenterTextSize(16);
        ui.chart.setCenterTextColor(mTextColor);
        ui.chart.setHoleRadius(55f);
        ui.chart.setHoleColor(Color.TRANSPARENT);
        ui.chart.setHighlightPerTapEnabled(true);
        ui.chart.getLegend().setWordWrapEnabled(true);
        ui.chart.getLegend().setTextColor(mTextColor);
        ui.chart.setDrawEntryLabels(false);

        return ui.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateCategoryDataSample(mCallbackInterface.getCategoryDataSample());
    }
    //endregion

    public void updateCategoryDataSample(CategoryDataCollection dataCollection) {
        float[] durationRatios = new float[dataCollection.getNumberOfHabits()];

        for (int i = 0; i < durationRatios.length; i++)
            durationRatios[i] = dataCollection.getHabitDuration(i) / (float) dataCollection.calculateTotalDuration() * 100;

        setPieData(durationRatios, dataCollection);
    }

    public void setPieData(float[] durationRatios, CategoryDataCollection dataSample) {
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
        dataSet.setValueTextColor(mTextColor);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineVariableLength(true);
        dataSet.setValueLineColor(mTextColor);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        ui.chart.setData(data);
    }

}
