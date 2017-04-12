package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.example.brandon.habitlogger.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArchivedHabitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArchivedHabitsFragment extends MyFragmentBase {

    public ArchivedHabitsFragment() {
        // Required empty public constructor
    }

    public static ArchivedHabitsFragment newInstance() {
        return new ArchivedHabitsFragment();
    }

    @Override
    @StringRes
    public int getFragmentTitle() {
        return R.string.menu_archive;
    }

}
