package com.example.brandon.habitlogger.OverviewActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitActivity.GetScrollEventsFromFragmentsInterface;
import com.example.brandon.habitlogger.HabitActivity.NewEntryForm;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerViewAdapters.ComplexDecoration;
import com.example.brandon.habitlogger.RecyclerViewAdapters.EntryViewAdapter;
import com.example.brandon.habitlogger.RecyclerViewAdapters.SpaceOffsetDecoration;
import com.example.brandon.habitlogger.data.HabitDataSample;
import com.example.brandon.habitlogger.ui.RecyclerViewScrollObserver;

import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OverviewEntriesFragment extends Fragment implements UpdateHabitDataSampleInterface {
    HabitDatabase habitDatabase;
    RecyclerView entriesContainer;
    List<SessionEntry> sessionEntries;
    EntryViewAdapter entryAdapter;

    PreferenceChecker preferenceChecker;
    private CallbackInterface callbackInterface;
    private GetScrollEventsFromFragmentsInterface listener;

    public OverviewEntriesFragment() {
        // Required empty public constructor
    }

    public static OverviewEntriesFragment newInstance() {
        return new OverviewEntriesFragment();
    }

    //region // On create methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceChecker = new PreferenceChecker(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_entries, container, false);

        habitDatabase = new HabitDatabase(getContext());

        entriesContainer = (RecyclerView) v.findViewById(R.id.entries_holder);
        sessionEntries = habitDatabase.lookUpEntries(habitDatabase.searchAllEntriesWithTimeRange(0, Long.MAX_VALUE));

        entryAdapter = new EntryViewAdapter(sessionEntries, getContext(),
                new EntryViewAdapter.OnClickListeners() {
                    @Override
                    public void onRootClick(long habitId, long entryId) {
                        NewEntryForm dialog = NewEntryForm.newInstance(habitDatabase.getEntry(entryId));
                        dialog.setOnFinishedListener(new NewEntryForm.OnFinishedListener() {
                            @Override
                            public void onFinishedWithResult(SessionEntry entry) {
                                if(entry != null){
                                    habitDatabase.updateEntry(entry.getDatabaseId(), entry);
                                    updateSessionEntryById(entry.getDatabaseId(), entry);
                                }
                            }

                            @Override
                            public void onDeleteClicked(SessionEntry entry) {
                                habitDatabase.deleteEntry(entry.getDatabaseId());
                                removeSessionEntryById(entry.getDatabaseId());
                            }
                        });

                        dialog.show(getFragmentManager(), "edit-entry");
                    }
                });

        entriesContainer.addItemDecoration(new ComplexDecoration(getContext(), R.dimen.entries_section_text_size, new ComplexDecoration.Callback() {
            @Override
            public long getGroupId(int position) {
                if(position >= 0 && position < sessionEntries.size()) {
                    return sessionEntries.get(position).getStartingTimeDate();
                } else{
                    return -1;
                }
            }

            @Override @NonNull
            public String getGroupFirstLine(int position) {
                if(position >= 0 && position < sessionEntries.size()) {
                    String dateFormat = preferenceChecker.stringGetDateFormat();
                    return sessionEntries.get(position).getStartTimeAsString(dateFormat);
                } else{
                    return "";
                }
            }
        }));

        entriesContainer.addOnScrollListener(new RecyclerViewScrollObserver() {
            @Override
            public void onScrollUp() {
                if(OverviewEntriesFragment.this.listener != null)
                    OverviewEntriesFragment.this.listener.onScrollUp();
            }

            @Override
            public void onScrollDown() {
                if(OverviewEntriesFragment.this.listener != null)
                    OverviewEntriesFragment.this.listener.onScrollDown();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        entriesContainer.setLayoutManager(layoutManager);
        entriesContainer.setItemAnimator(new DefaultItemAnimator());
        entriesContainer.setAdapter(entryAdapter);

        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);
        int topOffset = (int) getResources().getDimension(R.dimen.top_offset_dp);
        SpaceOffsetDecoration bottomOffsetDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
        entriesContainer.addItemDecoration(bottomOffsetDecoration);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_overall_statistcs, menu);
    }

    //endregion

    //region // onAttach - onDetach
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbackInterface = (CallbackInterface)context;
        callbackInterface.addCallback(this);

        if(context instanceof GetScrollEventsFromFragmentsInterface){
            this.listener = (GetScrollEventsFromFragmentsInterface)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackInterface.removeCallback(this);
    }
    //endregion

    //region // Methods responsible for updating the ui
    public void removeSessionEntryById(long databaseId) {
        int index = getSessionEntryIndex(databaseId);
        sessionEntries.remove(index);
        entryAdapter.notifyItemRemoved(index);
    }

    public void updateSessionEntryById(long databaseId, SessionEntry entry){
        int index = getSessionEntryIndex(databaseId);
        sessionEntries.set(index, entry);
        entryAdapter.notifyItemChanged(index);
    }
    //endregion

    @Override
    public void updateDataSample(HabitDataSample data) {
        if(entryAdapter != null) {
            this.sessionEntries = data.getSessionEntriesSample().getSessionEntries();
            entryAdapter = new EntryViewAdapter(this.sessionEntries, getContext(), entryAdapter.getListener());
            entriesContainer.setAdapter(entryAdapter);
        }
    }

    private int getSessionEntryIndex(long entryId){
        int index = 0;
        for(SessionEntry entry : sessionEntries){
            if(entry.getDatabaseId() == entryId) break;
            index++;
        }

        return index;
    }
}