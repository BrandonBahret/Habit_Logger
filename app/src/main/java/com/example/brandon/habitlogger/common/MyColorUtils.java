package com.example.brandon.habitlogger.common;

import android.graphics.Color;
import android.support.annotation.FloatRange;
import android.support.v4.graphics.ColorUtils;

import java.util.Random;

/**
 * Created by Brandon on 3/11/2017.
 * Helper class for working with colors.
 */

public class MyColorUtils {

    /**
     * @return True if the color's lightness is greater than the provided threshold. (recommended: 0.5f)
     */
    public boolean isColorBright(int color, @FloatRange(from = 0.0, to = 1.0) float threshold) {
        return getLightness(color) > threshold;
    }

    /**
     * @return True if the color's lightness is greater than 0.5f
     */
    public boolean isColorBright(int color) {
        return getLightness(color) > 0.5f;
    }

    public static int getRandomColor() {
        Random r = new Random();
        return Color.argb(
                255,
                r.nextInt(255),
                r.nextInt(255),
                r.nextInt(255)
        );
    }

    //region Get color components {}
    public static int[] getRGBComponents(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return new int[]{red, green, blue};
    }

    public static float[] getHSLComponents(int color) {
        float hsl[] = new float[3];
        int rgb[] = getRGBComponents(color);
        ColorUtils.RGBToHSL(rgb[0], rgb[1], rgb[2], hsl);
        return hsl;
    }

    public static float getHue(int color) {
        return getHSLComponents(color)[0];
    }

    public static float getSaturation(int color) {
        return getHSLComponents(color)[1];
    }

    public static float getLightness(int color) {
        return getHSLComponents(color)[2];
    }
    //endregion

    //region Set color components {}
    public static int setHue(int color, float hue) {
        float[] hsl = getHSLComponents(color);
        hsl[0] = hue;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int setSaturation(int color, float saturation) {
        float[] hsl = getHSLComponents(color);
        hsl[1] = saturation;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int setLightness(int color, float lightness) {
        float[] hsl = getHSLComponents(color);
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }
    //endregion
}
