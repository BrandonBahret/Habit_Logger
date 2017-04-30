package com.example.brandon.habitlogger.common;

import android.support.v7.app.AppCompatDelegate;

import java.io.Serializable;

/**
 * Created by Brandon on 4/14/2017.
 * A class to calculate a palette used to programmatically change the app theme
 */

public class ThemeColorPalette implements Serializable {

    //region (Member attributes)
    private final int mBaseColor;
    private final int mColorPrimary;
    private final int mColorPrimaryDark;
    private final int mColorAccent;
    private final int mColorAccentDark;
    //endregion

    public ThemeColorPalette(int baseColor) {
        mBaseColor = baseColor;
        int color = baseColor;
        int darkerColor = MyColorUtils.darkenColorBy(baseColor, 0.05f);
        int accentColor = baseColor;
        int accentDarkerColor = accentColor;

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            // Apply a different filter for night mode
            if (MyColorUtils.getLightness(color) > 0.40) {
                darkerColor = MyColorUtils.setLightness(darkerColor, 0.40f);
                accentDarkerColor = MyColorUtils.setLightness(accentDarkerColor, 0.45f);
                color = MyColorUtils.setLightness(color, 0.45f);
                accentColor = MyColorUtils.setLightness(accentColor, 0.50f);
            }
        }
        else {
            // Apply a different filter for day mode
            if (MyColorUtils.getLightness(color) > 0.40) {
                darkerColor = MyColorUtils.setLightness(darkerColor, 0.60f);
                accentDarkerColor = MyColorUtils.setLightness(accentDarkerColor, 0.65f);
                color = MyColorUtils.setLightness(color, 0.65f);
                accentColor = MyColorUtils.setLightness(accentColor, 0.70f);
            }
        }

        if (MyColorUtils.getSaturation(color) > 0.45) {
            darkerColor = MyColorUtils.setSaturation(darkerColor, 0.45f);
            accentDarkerColor = MyColorUtils.setSaturation(accentDarkerColor, 0.45f);
            color = MyColorUtils.setSaturation(color, 0.45f);
            accentColor = MyColorUtils.setSaturation(accentColor, 0.45f);
        }

        mColorPrimary = color;
        mColorPrimaryDark = darkerColor;
        mColorAccent = accentColor;
        mColorAccentDark = accentDarkerColor;
    }

    //region Getters {}
    public int getBaseColor() {
        return mBaseColor;
    }

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
