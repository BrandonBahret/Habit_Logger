package com.example.brandon.habitlogger.ui;

import android.support.v4.widget.NestedScrollView;

/**
 * Created by Brandon on 3/28/2017.
 * Base class for scroll observers
 */

public abstract class NestedScrollObserver implements NestedScrollView.OnScrollChangeListener {

    final int threshold = 2;
    boolean control = false;

    public abstract void onScrollUp();
    public abstract void onScrollDown();

    public void onScrolled(int dy) {
        if (dy > threshold && !control) {
            onScrollDown();
            control = true;
        }

        else if (dy < -threshold && control) {
            onScrollUp();
            control = false;
        }
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        onScrolled(scrollY - oldScrollY);
    }

}
