package com.example.brandon.habitlogger.ui.Activities.ScrollObservers;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Brandon on 3/8/2017.
 * Class to easily watch scroll events in recycler views
 */

public abstract class RecyclerViewScrollObserver extends RecyclerView.OnScrollListener {

    private static final int THRESHOLD = 2;
    private boolean mControl = false;

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int current) {
        super.onScrolled(recyclerView, dx, current);

//        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//        int visibleItemCount = layoutManager.getChildCount();
//        int totalItemCount = layoutManager.getItemCount();

//        if (totalItemCount > visibleItemCount) {
            if (current > THRESHOLD && !mControl) {
                onScrollDown();
                mControl = true;
            }
            else if (current < -THRESHOLD && mControl) {
                onScrollUp();
                mControl = false;
            }
//        }
    }

    public void setControlState(boolean state){
        mControl = state;
    }

}
