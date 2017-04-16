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
import android.widget.ImageView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.EntryViewAdapter;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.IHabitDataCallback;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.RecyclerViewScrollObserver;

import static com.example.brandon.habitlogger.R.id.no_entries_available_icon;
import static com.example.brandon.habitlogger.R.id.no_result_icon;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EntriesFragment extends Fragment implements
        EntryViewAdapter.OnClickListeners, IHabitDataCallback.IEntriesFragment {

    //region (Member attributes)
    private View mView;

    private IHabitDataCallback mCallbackInterface;
    private IScrollEvents mScrollListener;

    private String mDateFormat;
    private boolean mMakeHeadersSticky;
    private ThemeColorPalette mColorPalette;
    private Habit mHabit;
    private HabitDatabase mHabitDatabase;
    private SessionEntriesCollection mSessionEntries;

    private EntryViewAdapter mEntryAdapter;
    private RecyclerView mEntriesContainer;
    //endregion

    //region Code responsible for providing events to the parent activity
    private IEntriesEvents mEntryViewListener;

    public interface IEntriesEvents {
        void onEntryViewClicked(SessionEntry entry);
    }
    //endregion

    public EntriesFragment() {
        // Required empty public constructor
    }

    public static EntriesFragment newInstance() {
        return new EntriesFragment();
    }

    //region Methods responsible for handling fragment lifecycle

    //region (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCallbackInterface = (IHabitDataCallback) context;
        mCallbackInterface.setEntriesFragmentCallback(this);

        if (context instanceof IScrollEvents)
            mScrollListener = (IScrollEvents) context;

        if (context instanceof IEntriesEvents)
            mEntryViewListener = (IEntriesEvents) context;
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_entries, container, false);

        mColorPalette = mCallbackInterface.getColorPalette();
        mHabit = mCallbackInterface.getHabit();

        Context context = getContext();
        PreferenceChecker preferenceChecker = new PreferenceChecker(context);
        mDateFormat = preferenceChecker.stringGetDateFormat();
        mMakeHeadersSticky = preferenceChecker.makeDateHeadersSticky();
        mHabitDatabase = new HabitDatabase(context);

        mSessionEntries = mCallbackInterface.getSessionEntries();
        mEntryAdapter = new EntryViewAdapter(mSessionEntries, context, mColorPalette, this);

        mEntriesContainer = (RecyclerView) mView.findViewById(R.id.entries_holder);

        // Add item decorations
//        mEntriesContainer.addItemDecoration(new GroupDecoration(getContext(), R.dimen.entries_section_text_size, mMakeHeadersSticky, new GroupDecoration.Callback() {
//            @Override
//            public long getGroupId(int position) {
//                if (position >= 0 && position < mSessionEntries.size())
//                    return mSessionEntries.get(position).getStartingTimeIgnoreTimeOfDay();
//
//                else return -1;
//            }
//
//            @Override
//            @NonNull
//            public String getGroupFirstLine(int position) {
//                if (position >= 0 && position < mSessionEntries.size())
//                    return mSessionEntries.get(position).stringifyStartingTime(mDateFormat);
//
//                else return "";
//            }
//        }));
//
//        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);
//        int topOffset = (int) (getResources().getDimension(R.dimen.extra_large_top_offset_dp)) + (int) (getResources().getDimension(R.dimen.sections_top_offset_dp));
//        SpaceOffsetDecoration spaceOffsetDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
//        mEntriesContainer.addItemDecoration(spaceOffsetDecoration);

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

    //region Methods responsible for manipulating entries in the fragment
    public void removeSessionEntryById(long databaseId) {
        int index = getSessionEntryIndex(databaseId);
        if (index != -1) {
            mSessionEntries.remove(index);
            mEntryAdapter.notifyItemRemoved(index);
        }

        showNoDataLayout(mSessionEntries.isEmpty());
    }

    private int getSessionEntryIndex(long entryId) {
        for (int index = 0; index < mSessionEntries.size(); index++) {
            if (mSessionEntries.get(index).getDatabaseId() == entryId)
                return index;
        }

        return -1;
    }

    private int getPositionForEntry(SessionEntry newEntry) {
        int foundPosition = MyCollectionUtils.binarySearch(mSessionEntries, newEntry.getStartingTime(), SessionEntry.IKeyCompareStartingTime);
        return foundPosition < 0 ? (-foundPosition) - 1 : foundPosition;
    }
    //endregion

    //region Methods responsible for updating the ui
    private void showNoDataLayout(boolean showNoDataLayout) {
        if (mView != null) {

            boolean hasEntries = mHabitDatabase.getNumberOfEntries(mHabit.getDatabaseId()) > 0;
            int noEntriesVisibilityMode = showNoDataLayout && !hasEntries ? View.VISIBLE : View.GONE;
            int noResultsVisibilityMode = showNoDataLayout && hasEntries ? View.VISIBLE : View.GONE;

            ImageView icon;
            View noDataLayout = mView.findViewById(R.id.no_data_layout);
            icon = (ImageView) mView.findViewById(no_entries_available_icon);
            icon.setColorFilter(mColorPalette.getColorAccentDark());
            noDataLayout.setVisibility(noEntriesVisibilityMode);

            View noResultsLayout = mView.findViewById(R.id.no_results_layout);
            icon = (ImageView) mView.findViewById(no_result_icon);
            icon.setColorFilter(mColorPalette.getColorAccentDark());
            noResultsLayout.setVisibility(noResultsVisibilityMode);

            int entryContainerVisibilityMode = showNoDataLayout ? View.GONE : View.VISIBLE;
            mEntriesContainer.setVisibility(entryContainerVisibilityMode);
        }
    }
    //endregion

    @Override
    public void onEntryViewClick(long habitId, long entryId) {
        SessionEntry entry = mHabitDatabase.getEntry(entryId);

        mEntryViewListener.onEntryViewClicked(entry);

//        SessionEntry oldEntry = mHabitDatabase.getEntry(entryId);
//
//        EditEntryForm dialog = EditEntryForm.newInstance(oldEntry, ContextCompat.getColor(getContext(), R.color.textColorContrastBackground));
//        dialog.setOnFinishedListener(new EditEntryForm.OnFinishedListener() {
//            @Override
//            public void onPositiveClicked(SessionEntry entry) {
//                if (entry != null) {
//                    SessionEntry oldEntry = mHabitDatabase.getEntry(entry.getDatabaseId());
//                    mHabitDatabase.updateEntry(entry.getDatabaseId(), entry);
//                    updateSessionEntryById(entry.getDatabaseId(), oldEntry, entry);
//                }
//            }
//
//            @Override
//            public void onNegativeClicked(SessionEntry entry) {
//                mHabitDatabase.deleteEntry(entry.getDatabaseId());
//                removeSessionEntryById(entry.getDatabaseId());
//            }
//        });
//
//        dialog.show(getFragmentManager(), "edit-entry");
    }

    //region IEntriesFragmentCallback
    @Override
    public void onUpdateEntries(SessionEntriesCollection dataCollection) {
        if (mEntryAdapter != null) {
            mEntryAdapter = new EntryViewAdapter(dataCollection, getContext(), mColorPalette, mEntryAdapter.getListener());
            mEntriesContainer.setAdapter(mEntryAdapter);
        }

        showNoDataLayout(mSessionEntries == null || mSessionEntries.isEmpty());
    }

    @Override
    public void onRemoveEntry(SessionEntry removedEntry) {
        int index = getSessionEntryIndex(removedEntry.getDatabaseId());
        if (index != -1) {
            mSessionEntries.remove(index);
            mEntryAdapter.notifyItemRemoved(index);
        }

        showNoDataLayout(mSessionEntries.isEmpty());
    }

    @Override
    public void onNotifyEntryAdded(int adapterPosition) {
        // Todo this doesn't work well with item decorations
        mEntryAdapter.notifyItemInserted(adapterPosition);
//        mEntryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUpdateEntry(long databaseId, SessionEntry oldEntry, SessionEntry newEntry) {
        int index = getSessionEntryIndex(databaseId);
        if (index != mEntryAdapter.getItemCount()) {
            if (oldEntry.getStartingTime() == newEntry.getStartingTime()) {
                mSessionEntries.set(index, newEntry);
                mEntryAdapter.notifyItemChanged(index);
            }
            else {
                mSessionEntries.remove(index);
                int newIndex = getPositionForEntry(newEntry);
                mSessionEntries.add(newIndex, newEntry);
                mEntryAdapter.notifyItemMoved(index, newIndex);
                mEntryAdapter.notifyItemChanged(newIndex);
            }
        }
    }

    @Override
    public void onUpdateColorPalette(ThemeColorPalette palette) {
        mColorPalette = palette;

        if (mEntryAdapter != null) {
            mEntryAdapter.setColorPalette(mColorPalette);
            mEntriesContainer.invalidate();
//            mEntryAdapter = new EntryViewAdapter(mSessionEntries, getContext(), mColorPalette, mEntryAdapter.getListener());
//            mEntriesContainer.setAdapter(mEntryAdapter);
        }

        if (mView != null) {
            View noDataLayout = mView.findViewById(R.id.no_data_layout);
            ImageView icon = (ImageView) mView.findViewById(no_entries_available_icon);
            icon.setColorFilter(mColorPalette.getColorAccentDark());
        }
    }

    @Override
    public void onTabReselected() {
        mEntriesContainer.smoothScrollToPosition(0);
    }
    //endregion

}
