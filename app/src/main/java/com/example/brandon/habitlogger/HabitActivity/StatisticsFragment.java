package com.example.brandon.habitlogger.HabitActivity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment{

    private static View view;
    int menuRes = R.menu.menu_habit;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EntriesFragment.
     */
    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_statistics, container, false);
        } catch (InflateException e) {
            // empty stub
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(this.menuRes, menu);
        menu.findItem(R.id.search).setVisible(false);
    }

    public void setMenuRes(int res) {
        this.menuRes = res;
    }
}
