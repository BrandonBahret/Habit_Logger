package com.example.brandon.habitlogger.ui.Dialogs;

import android.content.Context;

import com.example.brandon.habitlogger.R;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * Created by Brandon on 4/4/2017.
 * Class for displaying the licenses and attributions dialog
 */

public class AttributionsDialog {

    public static void showLicensesDialog(Context context) {

        Notices notices = new Notices();

        notices.addNotice(getLicenseDialogNotice());
        notices.addNotice(getMPAndroidChartNotice());
        notices.addNotice(getGoogleIconsNotice());
        notices.addNotice(getGoogleDriveNotice());
        notices.addNotice(getOpenCSVNotice());
        notices.addNotice(getExpandableRecyclerViewNotice());

        new LicensesDialog.Builder(context)
                .setNotices(notices)
                .setNoticesCssStyle(R.string.notices_style)
                .build()
                .show();
    }

    private static Notice getLicenseDialogNotice() {
        final String name = "LicensesDialog";
        final String url = "http://psdev.de";
        final String copyright = "Copyright 2013 Philip Schiffer <admin@psdev.de>";
        final License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    private static Notice getMPAndroidChartNotice() {
        final String name = "MPAndroidChart";
        final String url = "https://github.com/PhilJay/MPAndroidChart";
        final String copyright = "Copyright 2016 Philipp Jahoda";
        final License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    private static Notice getExpandableRecyclerViewNotice() {
        final String name = "ExpandableRecyclerView";
        final String url = "https://github.com/thoughtbot/expandable-recycler-view";
        final String copyright = "Copyright (c) 2016 Amanda Hill and thoughtbot, inc.";
        final License license = new MITLicense();
        return new Notice(name, url, copyright, license);
    }

    private static Notice getOpenCSVNotice() {
        final String name = "opencsv";
        final String url = "http://opencsv.sourceforge.net";
        final License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, null, license);
    }

    private static Notice getGoogleIconsNotice() {
        final String name = "Google Material Icons";
        final String url = "https://material.io/icons/";
        final License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, null, license);
    }

    private static Notice getGoogleDriveNotice(){
        final String name = "Google Drive API";
        final String url = "https://developers.google.com/drive/android/";
        final License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, null, license);
    }

}
