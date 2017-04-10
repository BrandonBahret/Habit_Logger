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
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.IDataOverviewCallback;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.NestedScrollObserver;

public class OverviewStatisticsFragment extends Fragment implements IDataOverviewCallback.IOnTabReselected {

    //region (Member attributes)
    private static View mFragmentView;
    private IDataOverviewCallback mCallbackInterface;
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

        if (context instanceof IDataOverviewCallback) {
            mCallbackInterface = (IDataOverviewCallback) context;
            mCallbackInterface.addOnTabReselectedCallback(this);
        }

        if (context instanceof IScrollEvents)
            mListener = (IScrollEvents) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mCallbackInterface != null) mCallbackInterface.removeOnTabReselectedCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mFragmentView != null) {
            ViewGroup parent = (ViewGroup) mFragmentView.getParent();
            if (parent != null)
                parent.removeView(mFragmentView);
        }
        try {
            mFragmentView = inflater.inflate(R.layout.fragment_overall_statistics, container, false);
            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.statistics_container);

            scrollView.setOnScrollChangeListener(getScrollEventListener());

        } catch (InflateException e) {
            // empty stub
        }

        boolean hasEntries = new HabitDatabase(getContext()).getNumberOfEntries() > 0;
        showNoDataScreen(hasEntries);

        return mFragmentView;
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

    private void showNoDataScreen(boolean hasEntries) {
        int mainLayoutVisibilityMode = hasEntries ? View.VISIBLE : View.GONE;
        mFragmentView.findViewById(R.id.statistics_container)
                .setVisibility(mainLayoutVisibilityMode);

        int noStatsLayoutVisibilityMode = hasEntries ? View.GONE : View.VISIBLE;
        View noStatisticsLayout = mFragmentView.findViewById(R.id.no_stats_layout);
        noStatisticsLayout.setVisibility(noStatsLayoutVisibilityMode);
    }

    @Override
    public void onTabReselected(int position) {
        if (mFragmentView != null && position == 2) {
            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.statistics_container);
            scrollView.smoothScrollTo(0, 0);
        }
    }
}
