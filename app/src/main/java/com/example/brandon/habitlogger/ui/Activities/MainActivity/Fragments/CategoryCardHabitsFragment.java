package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.example.brandon.habitlogger.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryCardHabitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryCardHabitsFragment extends MyFragmentBase {

    public CategoryCardHabitsFragment() {
        // Required empty public constructor
    }

    public static CategoryCardHabitsFragment newInstance(){
        return new CategoryCardHabitsFragment();
    }

    @Override
    @StringRes
    public int getFragmentTitle() {
        return R.string.home_nav_string;
    }

}
