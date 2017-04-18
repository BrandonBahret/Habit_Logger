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
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.IDataOverviewCallback;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.OverviewCalendarView.CalendarViewAdapter;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

public class OverviewCalendarFragment extends Fragment implements IDataOverviewCallback.ICalendarFragment {

    //region (Member attributes)
    private IDataOverviewCallback mCallbackInterface;
    private RecyclerView mCalendarViewContainer;
    private CalendarViewAdapter mAdapter;
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
        mCallbackInterface.setCalendarFragmentCallback(this);
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

        return v;
    }

    private SpaceOffsetDecoration getSpaceDecoration() {
        float top = getContext().getResources().getDimension(R.dimen.small_top_offset_dp);
        float bottom = getContext().getResources().getDimension(R.dimen.bottom_offset_dp);
        return new SpaceOffsetDecoration((int) bottom, (int) top);
    }

    @Override
    public void onUpdateEntries(SessionEntryCollection dataSample) {
//        if (mCalendarViewContainer != null) {
//            mAdapter = new CalendarViewAdapter(dataSample.buildHabitDataCollection(), getContext());
//            mCalendarViewContainer.setAdapter(mAdapter);
//            mCalendarViewContainer.scrollToPosition(mAdapter.getAdapterPositionForCurrentMonth());
//        }
    }

    @Override
    public void onTabReselected() {
        mCalendarViewContainer.smoothScrollToPosition(mAdapter.getAdapterPositionForCurrentMonth());
    }
    //endregion -- end --

    //endregion [ -------- end -------- ]

}