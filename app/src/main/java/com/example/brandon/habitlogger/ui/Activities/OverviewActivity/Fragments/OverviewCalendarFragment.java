package com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.IDataOverviewCallback;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.OverviewCalendarView.CalendarViewAdapter;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

public class OverviewCalendarFragment extends Fragment implements IDataOverviewCallback.IUpdateHabitSample {

    //region (Member attributes)
    private IDataOverviewCallback mCallback;
    private RecyclerView mCalendarViewContainer;
    //endregion

    public OverviewCalendarFragment() {
        // Required empty public constructor
    }

    public static OverviewCalendarFragment newInstance() {
        return new OverviewCalendarFragment();
    }

    //region [ ---- Methods responsible for handling the fragment lifecycle ---- ]

    //region start lifetime (onAttach, onCreateView)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCallback = (IDataOverviewCallback) context;
        mCallback.addCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        mCalendarViewContainer = (RecyclerView) v.findViewById(R.id.calendar_view_container);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCalendarViewContainer.setLayoutManager(layoutManager);
        mCalendarViewContainer.setItemAnimator(new DefaultItemAnimator());
        mCalendarViewContainer.addItemDecoration(getSpaceDecoration());

        updateHabitDataSample(mCallback.getDataSample());

        return v;
    }

    private SpaceOffsetDecoration getSpaceDecoration() {
        float top = getContext().getResources().getDimension(R.dimen.small_top_offset_dp);
        float bottom = getContext().getResources().getDimension(R.dimen.bottom_offset_dp);
        return new SpaceOffsetDecoration((int) bottom, (int) top);
    }
    //endregion -- end --

    @Override
    public void onStop() {
        super.onStop();
        mCallback.removeCallback(this);
    }

    //endregion [ -------- end -------- ]

    @Override
    public void updateHabitDataSample(HabitDataSample dataSample) {
        if (mCalendarViewContainer != null) {
            CalendarViewAdapter adapter = new CalendarViewAdapter(dataSample, getContext());
            mCalendarViewContainer.setAdapter(adapter);
            mCalendarViewContainer.scrollToPosition(adapter.getAdapterPositionForCurrentMonth());
        }
    }

}