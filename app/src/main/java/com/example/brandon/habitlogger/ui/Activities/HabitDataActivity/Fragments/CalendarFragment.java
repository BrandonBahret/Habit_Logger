package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.CenterLinearLayoutManager;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.example.brandon.habitlogger.ui.Widgets.CustomCalendar.CalendarView.CalendarViewAdapter;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

public class CalendarFragment extends Fragment implements IHabitDataCallback.ICalendarFragment {

    //region (Member attributes)
    IHabitDataCallback mCallbackInterface;

    private RecyclerView mCalendarViewContainer;
    private CalendarViewAdapter mCalendarAdapter;
    private ThemeColorPalette mColorPalette;
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
    }
    //endregion -- end --

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mColorPalette = mCallbackInterface.getColorPalette();

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        mCalendarViewContainer = (RecyclerView) v.findViewById(R.id.calendar_view_container);

        CenterLinearLayoutManager layoutManager = new CenterLinearLayoutManager(getContext());
        mCalendarViewContainer.setLayoutManager(layoutManager);
        mCalendarViewContainer.setItemAnimator(new DefaultItemAnimator());
        mCalendarViewContainer.addItemDecoration(getSpaceDecoration());

        onUpdateEntries(mCallbackInterface.getSessionEntries());
//        mCalendarViewContainer.scrollToPosition(mCalendarAdapter.getAdapterPositionForCurrentMonth());

        if (savedInstanceState == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager()
                    .getDefaultDisplay()
                    .getMetrics(displayMetrics);
            int windowOffset = (int) (displayMetrics.heightPixels / 3.0f);
            int viewOffset = (int) (getResources().getDimensionPixelSize(R.dimen.calendar_view_height) / 3.0f);
            int adapterPositionForCurrentMonth = mCalendarAdapter.getAdapterPositionForCurrentMonth();

            layoutManager.scrollToPositionWithOffset(adapterPositionForCurrentMonth, windowOffset - viewOffset);
            mCalendarViewContainer.smoothScrollToPosition(adapterPositionForCurrentMonth);
        }

        return v;
    }

    private SpaceOffsetDecoration getSpaceDecoration() {
        float top = getContext().getResources().getDimension(R.dimen.small_top_offset_dp);
        float bottom = getContext().getResources().getDimension(R.dimen.bottom_offset_dp);
        return new SpaceOffsetDecoration((int) bottom, (int) top);
    }
    //endregion

    //region Methods responsible for handling events
    @Override
    public void onUpdateEntries(SessionEntryCollection dataCollection) {
        mCalendarAdapter = new CalendarViewAdapter(dataCollection, mColorPalette.getBaseColor(), getContext());
        mCalendarViewContainer.setAdapter(mCalendarAdapter);
    }

    @Override
    public void onUpdateColorPalette(ThemeColorPalette palette) {
        mColorPalette = palette;
        if (mCalendarAdapter != null) {
            mCalendarAdapter.setColor(mColorPalette.getBaseColor());
            mCalendarViewContainer.setAdapter(mCalendarAdapter);
        }
    }

    @Override
    public void onTabReselected() {
        mCalendarViewContainer.smoothScrollToPosition(mCalendarAdapter.getAdapterPositionForCurrentMonth());
    }
    //endregion -- end --

}