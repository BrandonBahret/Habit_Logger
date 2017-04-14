package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.CalendarFragment;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.EntriesFragment;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.StatisticsFragment;

/**
 * Created by Brandon on 4/14/2017.
 * The view pager adapter for HabitDataActivity
 */

public class HabitDataActivityPagerAdapter  extends FragmentPagerAdapter {

    public final String[] titles;

    public HabitDataActivityPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        titles = context.getResources().getStringArray(R.array.tab_titles);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return EntriesFragment.newInstance();

            case 1:
                return CalendarFragment.newInstance();

            case 2:
                return StatisticsFragment.newInstance();
        }

        return null;
    }

    @Override
    public int getCount() {
        // Show 3 pages in total
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}