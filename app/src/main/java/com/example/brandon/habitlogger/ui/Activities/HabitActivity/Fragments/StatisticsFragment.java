package com.example.brandon.habitlogger.ui.Activities.HabitActivity.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.IHabitCallback;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.NestedScrollObserver;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment implements IHabitCallback.IOnTabReselected {

    //region (Member attributes)
    private static View mFragmentView;
    private IScrollEvents mListener;
    private IHabitCallback mCallbackInterface;
    private Habit mHabit;
    private int mColor;
    //endregion

    public StatisticsFragment() {
        // Required empty public constructor
    }

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    //region Methods responsible for handling fragment lifecycle
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof IHabitCallback) {
            mCallbackInterface = (IHabitCallback) context;
            mCallbackInterface.addOnTabReselectedCallback(this);
        }

        if (context instanceof IScrollEvents)
            mListener = (IScrollEvents) context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackInterface.removeOnTabReselectedCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mHabit = mCallbackInterface.getHabit();
        mColor = mCallbackInterface.getDefaultColor();

        if (mFragmentView != null) {
            ViewGroup parent = (ViewGroup) mFragmentView.getParent();
            if (parent != null)
                parent.removeView(mFragmentView);
        }
        try {
            mFragmentView = inflater.inflate(R.layout.fragment_statistics, container, false);
            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.statistics_container);
            scrollView.setOnScrollChangeListener(getOnScrollChangeListener());

            boolean hasEntries = new HabitDatabase(getContext()).getNumberOfEntries(mHabit.getDatabaseId()) > 0;
            showNoDataScreen(hasEntries);

        } catch (InflateException e) {
            // empty stub
        }

        return mFragmentView;
    }
    //endregion

    private void showNoDataScreen(boolean hasEntries) {
        float lightness = MyColorUtils.getLightness(mColor) - 0.15f;
        int color = MyColorUtils.setLightness(mColor, lightness);

        int mainLayoutVisibilityMode = hasEntries ? View.VISIBLE : View.GONE;
        mFragmentView.findViewById(R.id.statistics_container)
                .setVisibility(mainLayoutVisibilityMode);

        int noStatsLayoutVisibilityMode = hasEntries ? View.GONE : View.VISIBLE;
        View noStatisticsLayout = mFragmentView.findViewById(R.id.no_stats_layout);
        ((ImageView) noStatisticsLayout.findViewById(R.id.no_stats_available_icon))
                .setColorFilter(color);

        noStatisticsLayout.setVisibility(noStatsLayoutVisibilityMode);
    }

    //region Methods responsible for handling events
    private NestedScrollObserver getOnScrollChangeListener() {
        return new NestedScrollObserver() {
            @Override
            public void onScrollUp() {
                if (mListener != null)
                    mListener.onScrollUp();
            }

            @Override
            public void onScrollDown() {
                if (mListener != null)
                    mListener.onScrollDown();
            }
        };
    }

    @Override
    public void onTabReselected(int position) {
        if (mFragmentView != null && position == 2) {
            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.statistics_container);
            scrollView.smoothScrollTo(0, 0);
        }
    }
    //endregion

}
