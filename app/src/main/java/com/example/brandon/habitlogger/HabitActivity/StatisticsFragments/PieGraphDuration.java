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
import com.example.brandon.habitlogger.HabitActivity.UpdateCategorySampleInterface;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.databinding.FragmentPieGraphDurationBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;


public class PieGraphDuration extends Fragment implements UpdateCategorySampleInterface {

    private FragmentPieGraphDurationBinding ui;
    CallbackInterface callbackInterface;

    public PieGraphDuration() {
        // Required empty public constructor
    }

    public static PieGraphDuration newInstance() {
        return new PieGraphDuration();
    }

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
        Legend legend = ui.chart.getLegend();
        legend.setWordWrapEnabled(true);
        ui.chart.setDrawEntryLabels(false);
//        ui.chart.setEntryLabelColor(Color.BLACK);

        return ui.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        callbackInterface = (CallbackInterface) context;
        callbackInterface.addOnNewCategoryDataSampleCallback(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        CategoryDataSample sample = callbackInterface.getCategoryDataSample();
        updateCategoryDataSample(sample, sample.getDateFromTime(), sample.getDateToTime());
    }

    @Override
    public void updateCategoryDataSample(CategoryDataSample dataSample, long dateFrom, long dateTo) {
        float[] durationRatios = new float[dataSample.getNumberOfHabits()];

        for (int i = 0; i < durationRatios.length; i++) {
            durationRatios[i] = dataSample.getHabitDuration(i) / (float) dataSample.calculateTotalDuration() * 100;
        }

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
        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(0f);

        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        dataSet.setValueLineVariableLength(true);

//        int colors[] = genColors(category.getColorAsInt(), durationRatios.length);

//        dataSet.setColors(genColors(callbackInterface.getDefaultColor(), entries));
//        dataSet.setColors(genColors2(callbackInterface.getDefaultColor(), durationRatios.length));
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);

        ui.chart.setData(data);
    }

    private int[] genColors2(int color, int length) {
        int colors[] = new int[length];

        float hue = MyColorUtils.getHue(color);
        for (int i = 0; i < length; i++) {
            int thisColor = ColorUtils.blendARGB(ColorTemplate.MATERIAL_COLORS[i], ColorTemplate.PASTEL_COLORS[i], 0.75f);
            colors[i] = MyColorUtils.setHue(thisColor, hue);
        }

        return colors;
    }

    private int[] genColors(int color, List<PieEntry> entries) {
        int length = entries.size();
        int[] colors = new int[length];

        for (int i = 0; i < length; i++) {
            float y = entries.get(i).getValue();
            colors[i] = genColor(color, i, length, y);
        }

        return colors;
    }

    private int genColor(int color, int i, int max, float y) {

        int red = Color.red(color);//(int) (Color.red(color)   - (i / (float) max * BASE_VALUE));
        int green = Color.green(color);//(int) (Color.green(color) - (i / (float) max * BASE_VALUE));
        int blue = Color.blue(color);//(int) (Color.blue(color)  - (i / (float) max * BASE_VALUE));

        float hsl[] = new float[3];
        ColorUtils.RGBToHSL(red, green, blue, hsl);
        float ratio = (max - i) / (float) max * hsl[1];
        hsl[1] = ratio;
        hsl[2] = Math.max(0.5f, y / 100f * 0.75f);
        return ColorUtils.HSLToColor(hsl);
    }
}
