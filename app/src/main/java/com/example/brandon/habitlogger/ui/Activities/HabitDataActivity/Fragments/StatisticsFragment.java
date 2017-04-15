package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.NestedScrollObserver;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment implements IHabitDataCallback.IStatisticsFragment {

    //region (Member attributes)
    private View mFragmentView;
    private IScrollEvents mListener;
    private IHabitDataCallback mCallbackInterface;
    private Habit mHabit;
    private ThemeColorPalette mColorPalette;
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

        if (context instanceof IHabitDataCallback) {
            mCallbackInterface = (IHabitDataCallback) context;
            mCallbackInterface.setStatisticsFragmentCallback(this);
        }

        if (context instanceof IScrollEvents)
            mListener = (IScrollEvents) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mHabit = mCallbackInterface.getHabit();
        mColorPalette = mCallbackInterface.getColorPalette();

        mFragmentView = inflater.inflate(R.layout.fragment_habit_data_statistics, container, false);
//            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.statistics_container);
//            scrollView.setOnScrollChangeListener(getOnScrollChangeListener());

        boolean hasEntries = new HabitDatabase(getContext()).getNumberOfEntries(mHabit.getDatabaseId()) > 0;
        showNoDataScreen(hasEntries);

        return mFragmentView;
    }
    //endregion

    private void showNoDataScreen(boolean hasEntries) {

//        int mainLayoutVisibilityMode = hasEntries ? View.VISIBLE : View.GONE;
//        mFragmentView.findViewById(R.id.statistics_container)
//                .setVisibility(mainLayoutVisibilityMode);

        int noStatsLayoutVisibilityMode = hasEntries ? View.GONE : View.VISIBLE;
        View noStatisticsLayout = mFragmentView.findViewById(R.id.no_stats_layout);
        ((ImageView) noStatisticsLayout.findViewById(R.id.no_stats_available_icon))
                .setColorFilter(mColorPalette.getColorPrimaryDark());

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
    public void onUpdateEntries(SessionEntriesCollection dataSample) {

    }

    @Override
    public void onUpdateCategoryDataSample(CategoryDataSample dataSample) {

    }

    @Override
    public void onUpdateColorPalette(ThemeColorPalette colorPalette) {
        mColorPalette = colorPalette;

        if (mFragmentView != null) {
            View noStatisticsLayout = mFragmentView.findViewById(R.id.no_stats_layout);
            ((ImageView) noStatisticsLayout.findViewById(R.id.no_stats_available_icon))
                    .setColorFilter(mColorPalette.getColorAccentDark());
        }
    }

    @Override
    public void onTabReselected() {
        if (mFragmentView != null) {
            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.statistics_container);
            scrollView.smoothScrollTo(0, 0);
        }
    }
    //endregion

}
