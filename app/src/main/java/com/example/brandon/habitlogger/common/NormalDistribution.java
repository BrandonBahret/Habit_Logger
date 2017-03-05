package com.example.brandon.habitlogger.common;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 3/4/2017.
 */

public class NormalDistribution {
    private final double mean;
    private final double standardDeviation;

    public NormalDistribution(final List<Double> values) {
        this.mean = calculateMean(values);
        this.standardDeviation = calculateStandardDeviation(values);
    }

    public static List<Entry> getLineEntries(List<BarEntry> data) {
        List<Double> values = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            values.add((double) data.get(i).getY());
        }

        NormalDistribution normalDistribution = new NormalDistribution(values);

        final int size = data.size();
        List<Entry> entries = new ArrayList<>(size);

        for (BarEntry entry : data) {
            float x = entry.getX();
            float value = entry.getY();
            float y = size * (float) normalDistribution.curveFormula(value);
            entries.add(new Entry(x, y));
        }

        return entries;
    }

    private double curveFormula(double value) {
        final double SD_TIMES_SQRT_TWO_PI = this.standardDeviation * Math.sqrt(2 * Math.PI);
        final double VALUE_MINUS_MEAN_SQUARED = Math.pow((value - this.mean), 2);
        final double TWO_TIMES_SD_SQUARED = 2 * Math.pow(this.standardDeviation, 2);

        return (1 / SD_TIMES_SQRT_TWO_PI) * Math.pow(Math.E, -(VALUE_MINUS_MEAN_SQUARED / TWO_TIMES_SD_SQUARED));
    }

    /**
     * To calculate the standard deviation of those numbers:
     * Work out the Mean (the simple average of the numbers)
     * Then for each number: subtract the Mean and square the result.
     * Then work out the mean of those squared differences.
     * Take the square root of that and we are done!
     *
     * @return standard deviation
     */
    private Double calculateStandardDeviation(List<Double> values) {
        double sumOfDeviationsFromTheMean = 0;
        for (Double value : values)
            sumOfDeviationsFromTheMean += Math.pow((value - this.mean), 2);

        double meanOfDeviations = sumOfDeviationsFromTheMean / (float) values.size();
        return Math.sqrt(meanOfDeviations);
    }

    private Double calculateMean(List<Double> values) {
        return calculateSum(values) / (double) values.size();
    }

    private Double calculateSum(List<Double> values) {
        double sum = 0;
        for (Double value : values)
            sum += value;

        return sum;
    }
}
