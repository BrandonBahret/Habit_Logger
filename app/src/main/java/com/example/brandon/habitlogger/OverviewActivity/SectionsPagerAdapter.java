package com.example.brandon.habitlogger.OverviewActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Brandon on 3/2/2017.
 *
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public OverviewEntriesFragment entriesFragment = OverviewEntriesFragment.newInstance();
    public OverviewCalendarFragment calendarFragment = OverviewCalendarFragment.newInstance();
    public OverviewStatisticsFragment statisticsFragment = OverviewStatisticsFragment.newInstance();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //<editor-fold desc="// Create fragments">
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case (0):
                entriesFragment.setHasOptionsMenu(true);
                return entriesFragment;

            case (1):
                calendarFragment.setHasOptionsMenu(true);
                return calendarFragment;

            case (2):
                statisticsFragment.setHasOptionsMenu(true);
                return statisticsFragment;
        }

        return null;
    }
    //</editor-fold>

    //<editor-fold desc="// Get data for the tabs">
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Entries";
            case 1:
                return "Calendar";
            case 2:
                return "Statistics";
        }
        return null;
    }
    //</editor-fold>
}
