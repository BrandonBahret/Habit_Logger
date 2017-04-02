package com.example.brandon.habitlogger.ui;

import android.support.v4.widget.NestedScrollView;

/**
 * Created by Brandon on 3/28/2017.
 * Class to easily watch scroll events in nested scroll views
 */

public abstract class NestedScrollObserver implements NestedScrollView.OnScrollChangeListener {

    private static final int THRESHOLD = 2;
    private boolean mControl = false;

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    public void onScrolled(int dy) {
        if (dy > THRESHOLD && !mControl) {
            onScrollDown();
            mControl = true;
        }

        else if (dy < -THRESHOLD && mControl) {
            onScrollUp();
            mControl = false;
        }
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        onScrolled(scrollY - oldScrollY);
    }

}
