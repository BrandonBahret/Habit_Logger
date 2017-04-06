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

import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.IDataOverviewCallback;
import com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog.EditEntryForm;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.Activities.HabitActivity.EntryViewAdapter;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.GroupDecoration;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.RecyclerViewScrollObserver;

import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OverviewEntriesFragment extends Fragment implements IDataOverviewCallback.IUpdateHabitSample {

    //region (Member attributes)
    private HabitDatabase mHabitDatabase;
    private List<SessionEntry> mSessionEntries;

    private IScrollEvents mListener;
    private IDataOverviewCallback mCallback;

    private RecyclerView mEntriesContainer;
    private EntryViewAdapter mEntryAdapter;
    private String mDateFormat;
    private boolean mMakeDateHeadersSticky;
    //endregion

    public OverviewEntriesFragment() {
        // Required empty public constructor
    }

    public static OverviewEntriesFragment newInstance() {
        return new OverviewEntriesFragment();
    }

    //region [ ---- Methods responsible for handling the fragment lifecycle ---- ]

    //region entire lifetime (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (IDataOverviewCallback) context;
        mCallback.addCallback(this);

        if (context instanceof IScrollEvents)
            mListener = (IScrollEvents) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback.removeCallback(this);
    }
    //endregion -- end --

    //region [ -- on create methods (onCreate, onCreateView) -- ]

    //region Methods responsible constructing item decorations
    private SpaceOffsetDecoration getSpaceOffsetDecoration() {
        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);
        int topOffset = (int) getResources().getDimension(R.dimen.extra_large_top_offset_dp) + (int) (getResources().getDimension(R.dimen.sections_top_offset_dp));
        return new SpaceOffsetDecoration(bottomOffset, topOffset);
    }

    private GroupDecoration getGroupDecoration() {
        return new GroupDecoration(getContext(), R.dimen.entries_section_text_size, mMakeDateHeadersSticky, new GroupDecoration.Callback() {
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
        });
    }
    //endregion -- end --

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceChecker preferenceChecker = new PreferenceChecker(getContext());
        mDateFormat = preferenceChecker.stringGetDateFormat();
        mMakeDateHeadersSticky = preferenceChecker.makeDateHeadersSticky();
        mHabitDatabase = new HabitDatabase(getContext());
        mSessionEntries = mHabitDatabase.getEntries();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_entries, container, false);
        mEntriesContainer = (RecyclerView) v.findViewById(R.id.entries_holder);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mEntriesContainer.setLayoutManager(layoutManager);
        mEntriesContainer.setItemAnimator(new DefaultItemAnimator());
        mEntriesContainer.addOnScrollListener(getOnScrollListener());

        mEntryAdapter = new EntryViewAdapter(mSessionEntries, getContext(), getEntryViewListener());
        mEntriesContainer.setAdapter(mEntryAdapter);
        mEntriesContainer.addItemDecoration(getSpaceOffsetDecoration());
        mEntriesContainer.addItemDecoration(getGroupDecoration());

        return v;
    }

    //endregion [ -- end -- ]

    //endregion [ ---------------- end ---------------- ]

    //region Methods responsible for updating the ui
    public void removeSessionEntryById(long databaseId) {
        int index = getSessionEntryIndex(databaseId);
        if (index != -1) {
            mSessionEntries.remove(index);
            mEntryAdapter.notifyItemRemoved(index);
        }
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

    private int getPositionForEntry(SessionEntry newEntry) {
        int foundPosition = MyCollectionUtils.binarySearch(mSessionEntries, newEntry.getStartingTime(), SessionEntry.IKeyCompareStartingTime);
        return foundPosition < 0 ? (-foundPosition) - 1 : foundPosition;
    }

    private int getSessionEntryIndex(long entryId) {
        for (int index = 0; index < mSessionEntries.size(); index++) {
            if (mSessionEntries.get(index).getDatabaseId() == entryId)
                return index;
        }

        return -1;
    }
    //endregion -- end --

    //region Methods responsible for handling events
    private RecyclerViewScrollObserver getOnScrollListener() {
        return new RecyclerViewScrollObserver() {
            @Override
            public void onScrollUp() {
                if (mListener != null) mListener.onScrollUp();
            }

            @Override
            public void onScrollDown() {
                if (mListener != null) mListener.onScrollDown();
            }
        };
    }

    private EntryViewAdapter.OnClickListeners getEntryViewListener() {
        return new EntryViewAdapter.OnClickListeners() {
            @Override
            public void onEntryViewClick(long habitId, long entryId) {
                EditEntryForm dialog = EditEntryForm.newInstance(mHabitDatabase.getEntry(entryId));
                dialog.setOnFinishedListener(getEntryFormEventListener());
                dialog.show(getFragmentManager(), "edit-entry");
            }
        };
    }

    private EditEntryForm.OnFinishedListener getEntryFormEventListener() {
        return new EditEntryForm.OnFinishedListener() {
            @Override
            public void onPositiveClicked(SessionEntry newEntry) {
                if (newEntry != null) {
                    SessionEntry oldEntry = mHabitDatabase.getEntry(newEntry.getDatabaseId());
                    mHabitDatabase.updateEntry(newEntry.getDatabaseId(), newEntry);
                    updateSessionEntryById(newEntry.getDatabaseId(), oldEntry, newEntry);
                }
            }

            @Override
            public void onNegativeClicked(SessionEntry entryToRemove) {
                mHabitDatabase.deleteEntry(entryToRemove.getDatabaseId());
                removeSessionEntryById(entryToRemove.getDatabaseId());
            }
        };
    }
    //endregion -- end --

    @Override
    public void updateHabitDataSample(HabitDataSample dataSample) {
        if (mEntryAdapter != null) {
            mSessionEntries = dataSample.buildSessionEntriesList().getSessionEntries();
            mEntryAdapter = new EntryViewAdapter(mSessionEntries, getContext(), mEntryAdapter.getListener());
            mEntriesContainer.setAdapter(mEntryAdapter);
        }
    }

}
