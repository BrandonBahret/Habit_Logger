package com.example.brandon.habitlogger.OverallStatistics;

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

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverallEntriesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverallEntriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverallEntriesFragment extends Fragment {

    private OverallEntriesFragment.OnFragmentInteractionListener listener;

    HabitDatabase habitDatabase;
    RecyclerView entriesContainer;
    List<SessionEntry> sessionEntries;
    EntryViewAdapter entryAdapter;

    public OverallEntriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EntriesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OverallEntriesFragment newInstance() {
        return new OverallEntriesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_entries, container, false);

        habitDatabase = new HabitDatabase(getContext(), null, false);

        entriesContainer = (RecyclerView) v.findViewById(R.id.entries_holder);
        sessionEntries = habitDatabase.lookUpEntries(habitDatabase.searchAllEntriesWithTimeRange(0, Long.MAX_VALUE));

        entryAdapter = new EntryViewAdapter(sessionEntries, getContext(),
                new EntryViewAdapter.OnClickListeners() {
                    @Override
                    public void onRootClick(long habitId, long entryId) {

                    }
                });


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        entriesContainer.setLayoutManager(layoutManager);
        entriesContainer.setItemAnimator(new DefaultItemAnimator());
        entriesContainer.setAdapter(entryAdapter);

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (listener != null) {
            listener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
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
