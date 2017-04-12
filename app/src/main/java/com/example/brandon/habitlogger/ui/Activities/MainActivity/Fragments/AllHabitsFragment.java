package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.example.brandon.habitlogger.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllHabitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllHabitsFragment extends MyFragmentBase {

    public AllHabitsFragment() {
        // Required empty public constructor
    }

    public static AllHabitsFragment newInstance(){
        return new AllHabitsFragment();
    }

    @Override
    @StringRes
    public int getFragmentTitle() {
        return R.string.home_nav_string;
    }

}
