package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;

/**
 * Created by Brandon on 4/12/2017.
 * Base class for main activity fragments.
 */

public abstract class MyFragmentBase extends Fragment {

    protected View mFragmentView;

    public MyFragmentBase() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.fragment_main_recycler_view, container, false);
        return mFragmentView;
    }

    abstract public @StringRes int getFragmentTitle();

}
