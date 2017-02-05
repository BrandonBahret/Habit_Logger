package com.example.brandon.habitlogger.HabitActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.EntryViewAdapter;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EntriesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EntriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EntriesFragment extends Fragment {

    private static final String HABIT_ID = "HABIT_ID";
    private long habitId;

    private OnFragmentInteractionListener mListener;

    HabitDatabase habitDatabase;
    RecyclerView entriesContainer;
    FloatingActionMenu fab;
    List<SessionEntry> sessionEntries;
    EntryViewAdapter entryAdapter;

    public EntriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EntriesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EntriesFragment newInstance(long habitId) {
        EntriesFragment fragment = new EntriesFragment();
        Bundle args = new Bundle();
        args.putLong(HABIT_ID, habitId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.habitId = getArguments().getLong(HABIT_ID);
        }

        fab = (FloatingActionMenu)getActivity().findViewById(R.id.menu_fab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_entries, container, false);

        habitDatabase = new HabitDatabase(getContext(), null, false);
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


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        entriesContainer.setLayoutManager(layoutManager);
        entriesContainer.setItemAnimator(new DefaultItemAnimator());
        entriesContainer.setAdapter(entryAdapter);

        return v;
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

    public void updateEntries(List<SessionEntry> sessionEntries) {
        if(entryAdapter != null) {
            this.sessionEntries = sessionEntries;
            entryAdapter.notifyDataSetChanged();
        }
    }

    public void updateEntriesContainer(Set<Long> ids){

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
