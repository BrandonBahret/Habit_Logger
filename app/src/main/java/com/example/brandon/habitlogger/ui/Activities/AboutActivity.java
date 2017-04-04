package com.example.brandon.habitlogger.ui.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.Dialogs.AttributionsDialog;

public class AboutActivity extends AppCompatActivity {

    //region Methods to handle the activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null)
            toolbar.setDisplayHomeAsUpEnabled(true);

        String version = getVersionNumber();
        TextView versionText = (TextView) findViewById(R.id.versionNumber);
        versionText.setText(version);
    }

    private String getVersionNumber() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return "v" + pInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Version Number Not Found";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //endregion -- end --

    //region Methods responsible for handling events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.share:
                share();
                break;

            case R.id.attributions:
                AttributionsDialog.showLicensesDialog(AboutActivity.this);
                break;

            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void linkButtonClicked(View view) {
        String tag = (String) view.getTag();
        switch (tag) {
            case "Github":
                openLink("https://github.com/BrandonBahret");
                break;

            case "LinkedIn":
                openLink("https://www.linkedin.com/in/brandon-bahret-436012125");
                break;

            case "Freelancer":
                openLink("https://www.freelancer.com/u/brandonbahretfre.html");
                break;
        }
    }
    //endregion -- end --

    public static void startActivity(Context context) {
        Intent startAbout = new Intent(context, AboutActivity.class);
        context.startActivity(startAbout);
    }

    private void openLink(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Habit Logger");
            String message = "\nCheck out this app, it helps you monitor how you spend time!\n\n" +
                    "<A Link to the application on the play store> \n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(shareIntent, "Share with..."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
