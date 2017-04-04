package com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.NestedScrollObserver;

public class OverviewStatisticsFragment extends Fragment {

    //region (Member attributes)
    private static View mContentView;
    private IScrollEvents mListener;
    //endregion

    public OverviewStatisticsFragment() {
        // Required empty public constructor
    }

    public static OverviewStatisticsFragment newInstance() {
        return new OverviewStatisticsFragment();
    }

    //region Methods responsible for handling the fragment lifecycle
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IScrollEvents)
            mListener = (IScrollEvents) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mContentView != null) {
            ViewGroup parent = (ViewGroup) mContentView.getParent();
            if (parent != null)
                parent.removeView(mContentView);
        }
        try {
            mContentView = inflater.inflate(R.layout.fragment_overall_statistics, container, false);
            NestedScrollView scrollView = (NestedScrollView) mContentView.findViewById(R.id.statistics_container);

            scrollView.setOnScrollChangeListener(getScrollEventListener());

        } catch (InflateException e) {
            // empty stub
        }

        return mContentView;
    }
    //endregion -- end --

    //region Methods responsible for handling events
    private NestedScrollObserver getScrollEventListener() {
        return new NestedScrollObserver() {
            @Override
            public void onScrollUp() {
                if (OverviewStatisticsFragment.this.mListener != null)
                    OverviewStatisticsFragment.this.mListener.onScrollUp();
            }

            @Override
            public void onScrollDown() {
                if (OverviewStatisticsFragment.this.mListener != null)
                    OverviewStatisticsFragment.this.mListener.onScrollDown();
            }
        };
    }
    //endregion -- end --

}
