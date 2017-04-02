package com.example.brandon.habitlogger.HabitActivity;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.IScrollEvents;
import com.example.brandon.habitlogger.ui.NestedScrollObserver;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {

    private static View mFragmentView;
    private IScrollEvents mListener;

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
        if (context instanceof IScrollEvents)
            this.mListener = (IScrollEvents) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mFragmentView != null) {
            ViewGroup parent = (ViewGroup) mFragmentView.getParent();
            if (parent != null)
                parent.removeView(mFragmentView);
        }
        try {
            mFragmentView = inflater.inflate(R.layout.fragment_statistics, container, false);
            NestedScrollView scrollView = (NestedScrollView) mFragmentView.findViewById(R.id.statistics_container);
            scrollView.setOnScrollChangeListener(getOnScrollChangeListener());

        } catch (InflateException e) {
            // empty stub
        }

        return mFragmentView;
    }
    //endregion

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
    //endregion

}
