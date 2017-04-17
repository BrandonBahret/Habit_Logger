package com.example.brandon.habitlogger.ui.Activities.OverviewActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewCalendarFragment;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewEntriesFragment;
import com.example.brandon.habitlogger.ui.Activities.OverviewActivity.Fragments.OverviewStatisticsFragment;

/**
 * Created by Brandon on 4/17/2017.
 * The view pager adapter for DataOverviewActivity
 */

public class DataOverviewActivityPagerAdapter extends FragmentPagerAdapter {

    public final String[] titles;

    public DataOverviewActivityPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        titles = context.getResources().getStringArray(R.array.tab_titles);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return OverviewEntriesFragment.newInstance();

            case 1:
                return OverviewCalendarFragment.newInstance();

            case 2:
                return OverviewStatisticsFragment.newInstance();
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
