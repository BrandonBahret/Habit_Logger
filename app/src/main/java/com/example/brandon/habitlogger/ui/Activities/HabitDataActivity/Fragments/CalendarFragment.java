package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments;

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
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarView.CalendarViewAdapter;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

public class CalendarFragment extends Fragment implements IHabitDataCallback.ICalendarFragment {

    //region (Member attributes)
    IHabitDataCallback mCallbackInterface;

    private RecyclerView mCalendarViewContainer;
    private CalendarViewAdapter mAdapter;
    private int mDefaultColor;
    //endregion

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    //region Methods responsible for handling fragment lifecycle
    //region (onAttach - onDestroy)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCallbackInterface = (IHabitDataCallback) context;
        mCallbackInterface.setCalendarFragmentCallback(this);
//        callbackInterface.addOnTabReselectedCallback(this);
//        callbackInterface.addUpdateColorCallback(this);
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

//        mDefaultColor = callbackInterface.getDefaultColor();
        mCalendarViewContainer = (RecyclerView) v.findViewById(R.id.calendar_view_container);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mCalendarViewContainer.setLayoutManager(layoutManager);
        mCalendarViewContainer.setItemAnimator(new DefaultItemAnimator());
        mCalendarViewContainer.addItemDecoration(getSpaceDecoration());

//        updateEntries(callbackInterface.getSessionEntries());
//        mCalendarViewContainer.scrollToPosition(mAdapter.getAdapterPositionForCurrentMonth());

        return v;
    }

    private SpaceOffsetDecoration getSpaceDecoration() {
        float top = getContext().getResources().getDimension(R.dimen.small_top_offset_dp);
        float bottom = getContext().getResources().getDimension(R.dimen.bottom_offset_dp);
        return new SpaceOffsetDecoration((int) bottom, (int) top);
    }
    //endregion

    @Override
    public void onUpdateEntries(SessionEntriesCollection dataSample) {
        mAdapter = new CalendarViewAdapter(dataSample, mDefaultColor, getContext());
        mCalendarViewContainer.setAdapter(mAdapter);
    }

    @Override
    public void onUpdateColorPalette(ThemeColorPalette palette) {
        mDefaultColor = palette.getColorPrimary();
        if (mAdapter != null) {
            mAdapter.setColor(mDefaultColor);
            mCalendarViewContainer.setAdapter(mAdapter);
        }
    }

    @Override
    public void onTabReselected() {
        mCalendarViewContainer.smoothScrollToPosition(mAdapter.getAdapterPositionForCurrentMonth());
    }
}