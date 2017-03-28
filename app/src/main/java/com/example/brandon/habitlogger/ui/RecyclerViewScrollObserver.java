package com.example.brandon.habitlogger.ui;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Brandon on 3/8/2017.
 */

public abstract class RecyclerViewScrollObserver extends RecyclerView.OnScrollListener {

    public interface IScrollEvents {
        void onScrollUp();

        void onScrollDown();
    }

    int threshold = 2;
    boolean control = false;

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int current) {
        super.onScrolled(recyclerView, dx, current);

        if (current > threshold && !control) {
            onScrollDown();
            control = true;
        }

        else if (current < -threshold && control) {
            onScrollUp();
            control = false;
        }
    }
}
