package com.example.brandon.habitlogger.HabitActivity;

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

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.ComplexDecoration;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.EntryViewAdapter;
import com.example.brandon.habitlogger.data.SessionEntriesSample;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EntriesFragment extends Fragment implements UpdateEntriesInterface {

    private static final String HABIT_ID = "HABIT_ID";
    private long habitId;

    PreferenceChecker preferenceChecker;

    HabitDatabase habitDatabase;
    RecyclerView entriesContainer;
    FloatingActionMenu fab;
    List<SessionEntry> sessionEntries;
    EntryViewAdapter entryAdapter;

    public EntriesFragment() {
        // Required empty public constructor
    }

    public static EntriesFragment newInstance(long habitId) {
        EntriesFragment fragment = new EntriesFragment();
        Bundle args = new Bundle();
        args.putLong(HABIT_ID, habitId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_habit, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.habitId = getArguments().getLong(HABIT_ID);
        }

        preferenceChecker = new PreferenceChecker(getContext());
        fab = (FloatingActionMenu)getActivity().findViewById(R.id.menu_fab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_entries, container, false);

        habitDatabase = new HabitDatabase(getContext());
        sessionEntries = habitDatabase.getEntries(habitId);

        entriesContainer = (RecyclerView) v.findViewById(R.id.entries_holder);

        entryAdapter = new EntryViewAdapter(sessionEntries, getContext(),
                new EntryViewAdapter.OnClickListeners() {
                    @Override
                    public void onRootClick(long habitId, final long entryId) {
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

        entriesContainer.addItemDecoration(new ComplexDecoration(getContext(), new ComplexDecoration.Callback() {
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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        entriesContainer.setLayoutManager(layoutManager);
        entriesContainer.setItemAnimator(new DefaultItemAnimator());
        entriesContainer.setAdapter(entryAdapter);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        CallbackInterface callbackInterface = (CallbackInterface)context;
        callbackInterface.addCallback(this);
    }

    private int getSessionEntryIndex(long entryId){
        int index = 0;
        for(SessionEntry entry : sessionEntries){
            if(entry.getDatabaseId() == entryId){
                break;
            }
            index++;
        }

        return index;
    }

    public void addSessionEntry(SessionEntry entry) {
        sessionEntries.add(entry);
        entryAdapter.notifyItemInserted(sessionEntries.size() - 1);
    }

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

    @Override
    public void updateEntries(SessionEntriesSample dataSample) {
        if(entryAdapter != null) {
            this.sessionEntries = dataSample.getSessionEntries();

            entryAdapter = new EntryViewAdapter(this.sessionEntries, getContext(), entryAdapter.getListener());
            entriesContainer.setAdapter(entryAdapter);
        }
    }
}
