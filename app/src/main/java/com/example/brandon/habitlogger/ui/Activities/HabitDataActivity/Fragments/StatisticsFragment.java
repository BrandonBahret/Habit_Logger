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
import com.example.brandon.habitlogger.data.DataModels.DataCollections.CategoryDataCollection;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments.DistributionStartingTime;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments.LineGraphCompletion;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments.PieGraphCompletion;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragments.PieGraphDuration;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.NestedScrollObserver;

public class StatisticsFragment extends Fragment implements IHabitDataCallback.IStatisticsFragment{

    //region (Member attributes)
    private View mFragmentView;
    private IScrollEvents mScrollListener;
    private IHabitDataCallback mCallbackInterface;
    private Habit mHabit;
    private ThemeColorPalette mColorPalette;

    private PieGraphCompletion mPieCompletion;
    private LineGraphCompletion mLineCompletion;
    private DistributionStartingTime mDistributionStartingTime;
    private PieGraphDuration mPieDuration;
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
            mScrollListener = (IScrollEvents) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mHabit = mCallbackInterface.getHabit();
        mColorPalette = mCallbackInterface.getColorPalette();

        mFragmentView = inflater.inflate(R.layout.fragment_habit_data_statistics, container, false);

        SessionEntryCollection entries = mCallbackInterface.getSessionEntries();
//        CategoryDataSample categoryDataSample = mCallbackInterface.getCategoryDataSample();
        mPieCompletion = PieGraphCompletion.newInstance(mColorPalette);
        mLineCompletion = LineGraphCompletion.newInstance(mColorPalette);
        mDistributionStartingTime = DistributionStartingTime.newInstance(mColorPalette);
        mPieDuration = PieGraphDuration.newInstance();

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_pie_completion, mPieCompletion)
                .add(R.id.fragment_line_completion, mLineCompletion)
                .add(R.id.fragment_distribution_starting_time, mDistributionStartingTime)
//                .add(R.id.fragment_pie_duration, mPieDuration)
                .commit();

        NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.scroll_container);
        scrollView.setOnScrollChangeListener(getOnScrollChangeListener());

        showNoDataScreen(!entries.isEmpty());

        return mFragmentView;
    }
    //endregion

    private void showNoDataScreen(boolean hasEntries) {

        if (mFragmentView != null) {
            int mainLayoutVisibilityMode = hasEntries ? View.VISIBLE : View.GONE;
            mFragmentView.findViewById(R.id.scroll_container)
                    .setVisibility(mainLayoutVisibilityMode);

            int noStatsLayoutVisibilityMode = hasEntries ? View.GONE : View.VISIBLE;
            View noStatisticsLayout = mFragmentView.findViewById(R.id.no_stats_layout);
            ((ImageView) noStatisticsLayout.findViewById(R.id.no_stats_available_icon))
                    .setColorFilter(mColorPalette.getColorPrimaryDark());

            noStatisticsLayout.setVisibility(noStatsLayoutVisibilityMode);
        }
    }

    //region Methods responsible for handling events
    private NestedScrollObserver getOnScrollChangeListener() {
        return new NestedScrollObserver() {
            @Override
            public void onScrollUp() {
                if (mScrollListener != null)
                    mScrollListener.onScrollUp();
            }

            @Override
            public void onScrollDown() {
                if (mScrollListener != null)
                    mScrollListener.onScrollDown();
            }
        };
    }

    @Override
    public void onUpdateEntries(SessionEntryCollection dataCollection) {
        showNoDataScreen(!dataCollection.isEmpty());
        if (mPieCompletion != null)
            mPieCompletion.updateEntries(dataCollection);
        if (mLineCompletion != null)
            mLineCompletion.updateEntries(dataCollection);
        if (mDistributionStartingTime != null)
            mDistributionStartingTime.updateEntries(dataCollection);
    }

    @Override
    public void onUpdateCategoryDataSample(CategoryDataCollection dataSample) {
        if (mPieDuration != null)
            mPieDuration.updateCategoryDataSample(dataSample);
    }

    @Override
    public void onUpdateColorPalette(ThemeColorPalette colorPalette) {
        mColorPalette = colorPalette;

        if (mFragmentView != null) {
            mPieCompletion.updateColor(colorPalette);
            mLineCompletion.updateColor(colorPalette);
            mDistributionStartingTime.updateColor(colorPalette);

            View noStatisticsLayout = mFragmentView.findViewById(R.id.no_stats_layout);
            ((ImageView) noStatisticsLayout.findViewById(R.id.no_stats_available_icon))
                    .setColorFilter(mColorPalette.getColorAccentDark());
        }
    }

    @Override
    public void onTabReselected() {
        if (mFragmentView != null) {
            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.scroll_container);
            scrollView.smoothScrollTo(0, 0);
        }
    }
    //endregion

}
