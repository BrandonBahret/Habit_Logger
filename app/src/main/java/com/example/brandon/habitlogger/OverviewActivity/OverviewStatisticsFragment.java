package com.example.brandon.habitlogger.OverviewActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.GetScrollEventsFromFragmentsInterface;
import com.example.brandon.habitlogger.R;

public class OverviewStatisticsFragment extends Fragment {

    private static View view;
    private GetScrollEventsFromFragmentsInterface listener;

    public OverviewStatisticsFragment() {
        // Required empty public constructor
    }

    public static OverviewStatisticsFragment newInstance() {
        return new OverviewStatisticsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_overall_statistics, container, false);

            NestedScrollView scrollView = (NestedScrollView) view.findViewById(R.id.statistics_container);
            scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

                int threshold = 2;
                boolean control = false;

                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    int current = scrollY - oldScrollY;

                    if (current > threshold && !control) {
                        if(OverviewStatisticsFragment.this.listener != null)
                            OverviewStatisticsFragment.this.listener.onScrollDown();
                        control = true;
                    }
                    else if (current < -threshold && control) {
                        if(OverviewStatisticsFragment.this.listener != null)
                            OverviewStatisticsFragment.this.listener.onScrollUp();
                        control = false;
                    }
                }
            });

        } catch (InflateException e) {
            // empty stub
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof GetScrollEventsFromFragmentsInterface){
            this.listener = (GetScrollEventsFromFragmentsInterface)context;
        }
    }
}
