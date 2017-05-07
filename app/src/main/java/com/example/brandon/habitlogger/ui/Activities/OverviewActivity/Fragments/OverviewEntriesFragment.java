package com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.EntriesFragment.EntryViewAdapter;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.IDataOverviewCallback;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.RecyclerViewScrollObserver;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.GroupDecoration;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OverviewEntriesFragment extends Fragment implements
        EntryViewAdapter.OnClickListeners, IDataOverviewCallback.IEntriesFragment {

    //region (Member attributes)
    private View mView;

    private IDataOverviewCallback mCallbackInterface;
    private IScrollEvents mScrollListener;

    private String mDateFormat;
    private boolean mMakeHeadersSticky;
    private HabitDatabase mHabitDatabase;
    private SessionEntryCollection mSessionEntries;

    private EntryViewAdapter mEntryAdapter;
    private RecyclerView mEntriesContainer;
    //endregion

    //region Code responsible for providing events to the parent activity
    private IEntriesEvents mEntryViewListener;

    public interface IEntriesEvents {
        void onEntryViewClicked(long entryId, SessionEntry entry);
    }

    @Override
    public void onEntryViewClick(long habitId, long entryId) {
        SessionEntry entry = mHabitDatabase.getEntry(entryId);
        mEntryViewListener.onEntryViewClicked(entryId, entry);
    }
    //endregion -- end --

    public OverviewEntriesFragment() {
        // Required empty public constructor
    }

    public static OverviewEntriesFragment newInstance() {
        return new OverviewEntriesFragment();
    }

    //region Methods responsible for handling fragment lifecycle

    //region (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCallbackInterface = (IDataOverviewCallback) context;
        mCallbackInterface.setEntriesFragmentCallback(this);

        mScrollListener = (IScrollEvents) context;

        mEntryViewListener = (IEntriesEvents) context;
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_entries, container, false);

        Context context = getContext();
        PreferenceChecker preferenceChecker = new PreferenceChecker(context);
        mDateFormat = preferenceChecker.stringGetDateFormat();
        mMakeHeadersSticky = preferenceChecker.makeDateHeadersSticky();
        mHabitDatabase = new HabitDatabase(context);

        mSessionEntries = mCallbackInterface.getSessionEntries();
        mEntryAdapter = new EntryViewAdapter(mSessionEntries, context, this);

        mEntriesContainer = (RecyclerView) mView.findViewById(R.id.entries_holder);

        // Add item decorations
        mEntriesContainer.addItemDecoration(new GroupDecoration(getContext(), R.dimen.entries_section_text_size, mMakeHeadersSticky, new GroupDecoration.Callback() {
            @Override
            public long getGroupId(int position) {
                if (position >= 0 && position < mSessionEntries.size())
                    return mSessionEntries.get(position).getStartingTimeIgnoreTimeOfDay();

                else return -1;
            }

            @Override
            @NonNull
            public String getGroupFirstLine(int position) {
                if (position >= 0 && position < mSessionEntries.size())
                    return mSessionEntries.get(position).stringifyStartingTime(mDateFormat);

                else return "";
            }
        }));

        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);
        int topOffset = (int) (getResources().getDimension(R.dimen.extra_large_top_offset_dp)) + (int) (getResources().getDimension(R.dimen.sections_top_offset_dp));
        SpaceOffsetDecoration spaceOffsetDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
        mEntriesContainer.addItemDecoration(spaceOffsetDecoration);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mEntriesContainer.setLayoutManager(layoutManager);
        mEntriesContainer.setItemAnimator(new DefaultItemAnimator());
        mEntriesContainer.setAdapter(mEntryAdapter);

        if (mScrollListener != null) {
            mEntriesContainer.addOnScrollListener(
                    new RecyclerViewScrollObserver() {
                        @Override
                        public void onScrollUp() {
                            mScrollListener.onScrollUp();
                        }

                        @Override
                        public void onScrollDown() {
                            mScrollListener.onScrollDown();
                        }
                    }
            );
        }

        onUpdateEntries(mCallbackInterface.getSessionEntries());

        return mView;
    }

    //endregion

    //region Methods responsible for updating the ui
    private void showNoDataLayout(boolean showNoDataLayout) {
        if (mView != null) {

            boolean hasEntries = mHabitDatabase.getNumberOfEntries() > 0;
            int noEntriesVisibilityMode = showNoDataLayout && !hasEntries ? View.VISIBLE : View.GONE;
            int noResultsVisibilityMode = showNoDataLayout && hasEntries ? View.VISIBLE : View.GONE;

            View noDataLayout = mView.findViewById(R.id.no_data_layout);
            noDataLayout.setVisibility(noEntriesVisibilityMode);

            View noResultsLayout = mView.findViewById(R.id.no_results_layout);
            noResultsLayout.setVisibility(noResultsVisibilityMode);

            int entryContainerVisibilityMode = showNoDataLayout ? View.GONE : View.VISIBLE;
            mEntriesContainer.setVisibility(entryContainerVisibilityMode);
        }
    }
    //endregion

    //region Methods responsible for handling events
    @Override
    public void onUpdateEntries(SessionEntryCollection dataCollection) {
        if (mEntryAdapter != null) {
            mSessionEntries = dataCollection;
            mEntryAdapter = new EntryViewAdapter(dataCollection, getContext(), mEntryAdapter.getListener());
            mEntriesContainer.setAdapter(mEntryAdapter);
            mEntriesContainer.invalidateItemDecorations();
        }

        showNoDataLayout(mSessionEntries == null || mSessionEntries.isEmpty());
    }

    @Override
    public void onNotifyEntryRemoved(int adapterPosition) {
        mEntryAdapter.notifyItemRemoved(adapterPosition);
        showNoDataLayout(mSessionEntries == null || mSessionEntries.isEmpty());
        mEntriesContainer.invalidate();
    }

    @Override
    public void onNotifyEntryAdded(int adapterPosition) {
        // Todo this doesn't work well with item decorations
        mEntryAdapter.notifyItemInserted(adapterPosition);
        showNoDataLayout(mSessionEntries == null || mSessionEntries.isEmpty());
//        mEntryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNotifyEntryUpdated(int oldPosition, int newPosition) {
        mEntryAdapter.notifyItemMoved(oldPosition, newPosition);
        mEntryAdapter.notifyItemChanged(newPosition);
    }

    @Override
    public void onTabReselected() {
        mEntriesContainer.smoothScrollToPosition(0);
    }
    //endregion -- end --

}
