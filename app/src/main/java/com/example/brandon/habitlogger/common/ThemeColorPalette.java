package com.example.brandon.habitlogger.common;

/**
 * Created by Brandon on 4/14/2017.
 * A class to calculate a palette used to programmatically change the app theme
 */

public class ThemeColorPalette {

    //region (Member attributes)
    private final int mColorPrimary;
    private final int mColorPrimaryDark;
    private final int mColorAccent;
    private final int mColorAccentDark;
    //endregion

    public static ThemeColorPalette newInstance(int baseColor, boolean isNightMode){
        return new ThemeColorPalette(baseColor, isNightMode);
    }

    private ThemeColorPalette(int baseColor, boolean isNightMode){
        int color = baseColor;
        int darkerColor = MyColorUtils.darkenColorBy(baseColor, 0.08f);
        int accentColor = baseColor;
        int accentDarkerColor = accentColor;

        if (isNightMode) {
            if (MyColorUtils.getLightness(color) > 0.40) {
                darkerColor = MyColorUtils.setLightness(darkerColor, 0.40f);
                accentDarkerColor = MyColorUtils.setLightness(accentDarkerColor, 0.45f);
                color = MyColorUtils.setLightness(color, 0.45f);
                accentColor = MyColorUtils.setLightness(accentColor, 0.50f);
            }

            if (MyColorUtils.getSaturation(color) > 0.45) {
                darkerColor = MyColorUtils.setSaturation(darkerColor, 0.45f);
                accentDarkerColor = MyColorUtils.setSaturation(accentDarkerColor, 0.45f);
                color = MyColorUtils.setSaturation(color, 0.45f);
                accentColor = MyColorUtils.setSaturation(accentColor, 0.45f);
            }
        }

        mColorPrimary = color;
        mColorPrimaryDark = darkerColor;
        mColorAccent = accentColor;
        mColorAccentDark = accentDarkerColor;
    }

    //region Getters {}
    public int getColorPrimary() {
        return mColorPrimary;
    }

    public int getColorPrimaryDark() {
        return mColorPrimaryDark;
    }

    public int getColorAccent() {
        return mColorAccent;
    }

    public int getColorAccentDark() {
        return mColorAccentDark;
    }
    //endregion

}
