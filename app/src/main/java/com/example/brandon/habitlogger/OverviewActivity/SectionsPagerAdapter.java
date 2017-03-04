package com.example.brandon.habitlogger.OverviewActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Brandon on 3/2/2017.
 *
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    final String[] TITLES = {"ENTRIES", "CALENDAR", "STATISTICS"};

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //region // Create fragments
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case (0):
                OverviewEntriesFragment entriesFragment = OverviewEntriesFragment.newInstance();
                entriesFragment.setHasOptionsMenu(true);
                return entriesFragment;

            case (1):
                OverviewCalendarFragment calendarFragment = OverviewCalendarFragment.newInstance();
                calendarFragment.setHasOptionsMenu(true);
                return calendarFragment;

            case (2):
                OverviewStatisticsFragment statisticsFragment = OverviewStatisticsFragment.newInstance();
                statisticsFragment.setHasOptionsMenu(true);
                return statisticsFragment;
        }

        return null;
    }
    //endregion

    //region // Get data for tabs
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
    //endregion

}
