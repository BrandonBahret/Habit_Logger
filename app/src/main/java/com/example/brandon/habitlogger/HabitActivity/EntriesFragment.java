package com.example.brandon.habitlogger.HabitActivity;

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

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerViewAdapters.EntryViewAdapter;
import com.example.brandon.habitlogger.RecyclerViewAdapters.GroupDecoration;
import com.example.brandon.habitlogger.RecyclerViewAdapters.SpaceOffsetDecoration;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.example.brandon.habitlogger.ui.RecyclerViewScrollObserver;

import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EntriesFragment extends Fragment implements CallbackInterface.IUpdateEntries, EntryViewAdapter.OnClickListeners {

    //region (Member attributes)
    private CallbackInterface mCallbackInterface;
    private RecyclerViewScrollObserver.IScrollEvents mListener;

    private String mDateFormat;
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

        mCallbackInterface = (CallbackInterface) context;
        mCallbackInterface.addUpdateEntriesCallback(this);

        if (context instanceof RecyclerViewScrollObserver.IScrollEvents)
            this.mListener = (RecyclerViewScrollObserver.IScrollEvents) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackInterface.removeUpdateEntriesCallback(this);
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_entries, container, false);

        Context context = getContext();
        mDateFormat = new PreferenceChecker(context).stringGetDateFormat();
        mHabitDatabase = new HabitDatabase(context);
        mSessionEntries = mCallbackInterface.getSessionEntries().getSessionEntries();
        mEntryAdapter = new EntryViewAdapter(mSessionEntries, context, this);

        mEntriesContainer = (RecyclerView) v.findViewById(R.id.entries_holder);

        //region Add item decorations
        mEntriesContainer.addItemDecoration(new GroupDecoration(getContext(), R.dimen.entries_section_text_size, new GroupDecoration.Callback() {
            @Override
            public long getGroupId(int position) {
                if (position >= 0 && position < mSessionEntries.size()) {
                    return mSessionEntries.get(position).getStartingTimeDate();
                }
                else {
                    return -1;
                }
            }

            @Override
            @NonNull
            public String getGroupFirstLine(int position) {
                if (position >= 0 && position < mSessionEntries.size()) {
                    return mSessionEntries.get(position).getStartTimeAsString(mDateFormat);
                }
                else {
                    return "";
                }
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

        return v;
    }

    //endregion

    //region Methods responsible for manipulating entries in the fragment
    public void removeSessionEntryById(long databaseId) {
        int index = getSessionEntryIndex(databaseId);
        if (index != mSessionEntries.size()) {
            mSessionEntries.remove(index);
            mEntryAdapter.notifyItemRemoved(index);
        }
    }

    public void updateSessionEntryById(long databaseId, SessionEntry oldEntry, SessionEntry newEntry) {
        int index = getSessionEntryIndex(databaseId);
        if (index != mSessionEntries.size()) {
            if (oldEntry.isSameStartingTimeAs(newEntry)) {
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
        for(int index = 0; index < mSessionEntries.size(); index++){
            if(mSessionEntries.get(index).getDatabaseId() == entryId)
                return index;
        }

        return -1;
    }

    private int getPositionForEntry(SessionEntry newEntry) {
        int foundPosition = MyCollectionUtils.binarySearch(mSessionEntries, newEntry.getStartTime(), SessionEntry.ICompareStartTime);
        return foundPosition < 0 ?  (-foundPosition) - 1 : foundPosition;
    }
    //endregion

    @Override
    public void onEntryViewClick(long habitId, long entryId) {
        SessionEntry oldEntry = mHabitDatabase.getEntry(entryId);

        EditEntryForm dialog = EditEntryForm.newInstance(oldEntry);

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
    public void updateEntries(SessionEntriesSample dataSample) {
        if (mEntryAdapter != null) {
            this.mSessionEntries = dataSample.getSessionEntries();
            mEntryAdapter = new EntryViewAdapter(this.mSessionEntries, getContext(), this);
            mEntriesContainer.setAdapter(mEntryAdapter);
        }
    }
}
