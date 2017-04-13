package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;

import java.util.List;

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
    protected int getNoDataLayoutId() {
        return R.id.no_habits_available_layout;
    }

    @Override
    void onSetUpView(RecyclerView recyclerView) {

    }

    //region Methods responsible for updating the Ui
    @Override
    protected void updateHabitCards(List<SessionEntry> entries) {

    }

    @Override
    public void addHabitToLayout(Habit habit) {

    }

    @Override
    public void notifySessionEnded(long habitId) {

    }

    @Override
    protected void checkIfHabitsAreAvailable() {
        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
        mRecyclerView.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
        mNoDataLayout.setVisibility(habitsAvailable ? View.GONE : View.VISIBLE);
    }

    @Override
    public void refreshLayout() {

    }
    //endregion -- end --

    //region Methods responsible for handling events
    @Override
    public boolean handleOnQuery(String query) {
        return true;
    }
    //endregion -- end --

    @Override
    @StringRes
    public int getFragmentTitle() {
        return R.string.home_nav_string;
    }

}
