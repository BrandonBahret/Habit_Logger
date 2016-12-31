package com.example.brandon.habitlogger;

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
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null){
            toolbar.setDisplayHomeAsUpEnabled(true);
        }

        String version = getVersionNumber();
        TextView versionText = (TextView) findViewById(R.id.versionNumber);
        versionText.setText(version);
    }

    private String getVersionNumber() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return "v" + pInfo.versionName;
        } catch (Exception e){
            e.printStackTrace();
            return "Version Number Not Found";
        }
    }

    public void linkButtonClicked(View view) {
        String tag = (String)view.getTag();

        switch (tag){
            case "Github":{
                openLink("https://github.com/BrandonBahret");
            }break;

            case "LinkedIn":{
                openLink("https://www.linkedin.com/in/brandon-bahret-436012125");
            }break;

            case "Freelancer":{
                openLink("https://www.freelancer.com/u/brandonbahretfre.html");
            }break;
        }
    }

    private void openLink(String url) {
//        Toast.makeText(this, "Open " + url, Toast.LENGTH_SHORT).show();

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void share() {
        Toast.makeText(this, "Share the app", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case(R.id.share):{
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
            }break;

            case(android.R.id.home):{
                finish();
            }break;
        }

        return super.onOptionsItemSelected(item);
    }
}
