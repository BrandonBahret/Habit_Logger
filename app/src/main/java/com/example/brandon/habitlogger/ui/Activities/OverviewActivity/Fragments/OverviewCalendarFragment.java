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
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.IDataOverviewCallback;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

public class OverviewCalendarFragment extends Fragment {

    //region (Member attributes)
    private IDataOverviewCallback mCallbackInterface;
    private RecyclerView mCalendarViewContainer;
//    private CalendarViewAdapter mAdapter;
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

        mCallbackInterface = (IDataOverviewCallback) context;
//        mCallbackInterface.addCallback(this);
//        mCallbackInterface.addOnTabReselectedCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mCallbackInterface.removeCallback(this);
//        mCallbackInterface.removeOnTabReselectedCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_overview_calendar, container, false);
        mCalendarViewContainer = (RecyclerView) v.findViewById(R.id.calendar_view_container);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCalendarViewContainer.setLayoutManager(layoutManager);
        mCalendarViewContainer.setItemAnimator(new DefaultItemAnimator());
        mCalendarViewContainer.addItemDecoration(getSpaceDecoration());

//        updateHabitDataSample(mCallbackInterface.getDataSample());

        return v;
    }

    private SpaceOffsetDecoration getSpaceDecoration() {
        float top = getContext().getResources().getDimension(R.dimen.small_top_offset_dp);
        float bottom = getContext().getResources().getDimension(R.dimen.bottom_offset_dp);
        return new SpaceOffsetDecoration((int) bottom, (int) top);
    }
    //endregion -- end --


    //endregion [ -------- end -------- ]

//    @Override
//    public void updateHabitDataSample(HabitDataCollection dataSample) {
//        if (mCalendarViewContainer != null) {
//            mAdapter = new CalendarViewAdapter(dataSample, getContext());
//            mCalendarViewContainer.setAdapter(mAdapter);
//            mCalendarViewContainer.scrollToPosition(mAdapter.getAdapterPositionForCurrentMonth());
//        }
//    }

//    @Override
//    public void onTabReselected(int position) {
//        if (position == 1)
//            mCalendarViewContainer.smoothScrollToPosition(mAdapter.getAdapterPositionForCurrentMonth());
//    }

}