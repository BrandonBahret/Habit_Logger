package com.example.brandon.habitlogger.common;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;

/**
 * Created by Brandon on 3/11/2017.
 *
 */

public class MyColorUtils {

    public static int[] getRGBComponents(int color){
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return new int[]{red, green, blue};
    }

    public static float[] getHSLComponents(int color){
        float hsl[] = new float[3];
        int rgb[] = getRGBComponents(color);
        ColorUtils.RGBToHSL(rgb[0], rgb[1], rgb[2], hsl);
        return hsl;
    }

    public static float getHue(int color){
        return getHSLComponents(color)[0];
    }

    public static float getLightness(int color) {
        return getHSLComponents(color)[2];
    }

    public static int setHue(int colorBase, float hue){
        int[] rgb = MyColorUtils.getRGBComponents(colorBase);
        float[] hsl = new float[3];
        ColorUtils.RGBToHSL(rgb[0], rgb[1], rgb[2], hsl);
        hsl[0] = hue;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int setSaturation(int colorBase, float saturation) {
        int[] rgb = MyColorUtils.getRGBComponents(colorBase);
        float[] hsl = new float[3];
        ColorUtils.RGBToHSL(rgb[0], rgb[1], rgb[2], hsl);
        hsl[1] = saturation;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int setLightness(int colorBase, float lightness) {
        int[] rgb = MyColorUtils.getRGBComponents(colorBase);
        float[] hsl = new float[3];
        ColorUtils.RGBToHSL(rgb[0], rgb[1], rgb[2], hsl);
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }
}
