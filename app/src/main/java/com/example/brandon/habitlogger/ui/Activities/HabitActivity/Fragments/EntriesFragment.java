package com.example.brandon.habitlogger.ui.Activities.HabitActivity.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.SessionEntriesCollection;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.EntryViewAdapter;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.IHabitCallback;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.RecyclerViewScrollObserver;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.EditEntryForm;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.GroupDecoration;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

import java.util.List;

import static com.example.brandon.habitlogger.R.id.no_entries_available_icon;
import static com.example.brandon.habitlogger.R.id.no_result_icon;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EntriesFragment extends Fragment implements IHabitCallback.IUpdateEntries,
        IHabitCallback.IOnTabReselected, IHabitCallback.IUpdateColor, EntryViewAdapter.OnClickListeners {

    //region (Member attributes)
    private View mView;

    private IHabitCallback mCallbackInterface;
    private IScrollEvents mListener;

    private String mDateFormat;
    private boolean mMakeHeadersSticky;
    private int mColor;
    private Habit mHabit;
    private HabitDatabase mHabitDatabase;
    private List<SessionEntry> mSessionEntries;

    private EntryViewAdapter mEntryAdapter;
    private RecyclerView mEntriesContainer;
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

        mCallbackInterface = (IHabitCallback) context;
        mCallbackInterface.addUpdateEntriesCallback(this);
        mCallbackInterface.addOnTabReselectedCallback(this);
        mCallbackInterface.addUpdateColorCallback(this);

        if (context instanceof IScrollEvents)
            this.mListener = (IScrollEvents) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackInterface.removeUpdateEntriesCallback(this);
        mCallbackInterface.removeOnTabReselectedCallback(this);
        mCallbackInterface.removeUpdateColorCallback(this);
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_entries, container, false);

        mColor = mCallbackInterface.getDefaultColor();
        mHabit = mCallbackInterface.getHabit();

        Context context = getContext();
        PreferenceChecker preferenceChecker = new PreferenceChecker(context);
        mDateFormat = preferenceChecker.stringGetDateFormat();
        mMakeHeadersSticky = preferenceChecker.makeDateHeadersSticky();
        mHabitDatabase = new HabitDatabase(context);
        mSessionEntries = mCallbackInterface.getSessionEntries().getSessionEntries();
        mEntryAdapter = new EntryViewAdapter(mSessionEntries, context, mColor, this);

        mEntriesContainer = (RecyclerView) mView.findViewById(R.id.entries_holder);

        //region Add item decorations
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
        //endregion

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mEntriesContainer.setLayoutManager(layoutManager);
        mEntriesContainer.setItemAnimator(new DefaultItemAnimator());
        mEntriesContainer.setAdapter(mEntryAdapter);

        mEntriesContainer.addOnScrollListener(new RecyclerViewScrollObserver() {
            @Override
            public void onScrollUp() {
                if (EntriesFragment.this.mListener != null)
                    EntriesFragment.this.mListener.onScrollUp();
            }

            @Override
            public void onScrollDown() {
                if (EntriesFragment.this.mListener != null)
                    EntriesFragment.this.mListener.onScrollDown();
            }
        });

        updateEntries(mCallbackInterface.getSessionEntries());

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

    public void updateSessionEntryById(long databaseId, SessionEntry oldEntry, SessionEntry newEntry) {
        int index = getSessionEntryIndex(databaseId);
        if (index != mSessionEntries.size()) {
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

            int color = MyColorUtils.darkenColorBy(mColor, 0.20f);
            ImageView icon;
            View noDataLayout = mView.findViewById(R.id.no_data_layout);
            icon = (ImageView) mView.findViewById(no_entries_available_icon);
            icon.setColorFilter(color);
            noDataLayout.setVisibility(noEntriesVisibilityMode);

            View noResultsLayout = mView.findViewById(R.id.no_results_layout);
            icon = (ImageView) mView.findViewById(no_result_icon);
            icon.setColorFilter(color);
            noResultsLayout.setVisibility(noResultsVisibilityMode);

            int entryContainerVisibilityMode = showNoDataLayout ? View.GONE : View.VISIBLE;
            mEntriesContainer.setVisibility(entryContainerVisibilityMode);
        }
    }
    //endregion

    @Override
    public void onEntryViewClick(long habitId, long entryId) {
        SessionEntry oldEntry = mHabitDatabase.getEntry(entryId);

        EditEntryForm dialog = EditEntryForm.newInstance(oldEntry, ContextCompat.getColor(getContext(), R.color.textColorContrastBackground));
        dialog.setOnFinishedListener(new EditEntryForm.OnFinishedListener() {
            @Override
            public void onPositiveClicked(SessionEntry entry) {
                if (entry != null) {
                    SessionEntry oldEntry = mHabitDatabase.getEntry(entry.getDatabaseId());
                    mHabitDatabase.updateEntry(entry.getDatabaseId(), entry);
                    updateSessionEntryById(entry.getDatabaseId(), oldEntry, entry);
                }
            }

            @Override
            public void onNegativeClicked(SessionEntry entry) {
                mHabitDatabase.deleteEntry(entry.getDatabaseId());
                removeSessionEntryById(entry.getDatabaseId());
            }
        });

        dialog.show(getFragmentManager(), "edit-entry");
    }

    @Override
    public void updateEntries(SessionEntriesCollection sessionEntries) {
        if (mEntryAdapter != null) {
            mSessionEntries = sessionEntries;
            mEntryAdapter = new EntryViewAdapter(mSessionEntries, getContext(), mColor, mEntryAdapter.getListener());
            mEntriesContainer.setAdapter(mEntryAdapter);
        }

        boolean showNoDataLayout = mSessionEntries == null || mSessionEntries.isEmpty();
        showNoDataLayout(showNoDataLayout);
    }

    @Override
    public void updateColor(int color) {
        mColor = color;

        if (mEntryAdapter != null) {
            mEntryAdapter = new EntryViewAdapter(mSessionEntries, getContext(), mColor, mEntryAdapter.getListener());
            mEntriesContainer.setAdapter(mEntryAdapter);
        }

        if(mView != null) {
            color = MyColorUtils.darkenColorBy(mColor, 0.15f);
            View noDataLayout = mView.findViewById(R.id.no_data_layout);
            ImageView icon = (ImageView) mView.findViewById(no_entries_available_icon);
            icon.setColorFilter(color);
        }
    }

    @Override
    public void onTabReselected(int position) {
        if (position == 0)
            mEntriesContainer.smoothScrollToPosition(0);
    }

}
