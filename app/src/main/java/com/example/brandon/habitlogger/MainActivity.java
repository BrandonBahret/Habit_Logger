package com.example.brandon.habitlogger;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    
    HabitDatabase habitDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        habitDatabase = new HabitDatabase(MainActivity.this);

        habitDatabase.resetDatabase();

        HabitCategory work = new HabitCategory("#ffffff", "Work");
        HabitCategory hobbies = new HabitCategory("#ffffff", "hobbies");
        HabitCategory freelance = new HabitCategory("#ffffff", "freelance");

        habitDatabase.addCategory(work);
        habitDatabase.addCategory(hobbies);
        habitDatabase.addCategory(freelance);

        long programmingId = habitDatabase.addHabit(
                new Habit("programming", "The time spent programming other languages", hobbies, null, null)
        );

        habitDatabase.addHabit(new Habit("Freelance work", null, freelance, null, null));
        long freelanceId = habitDatabase.addHabit(new Habit("Freelance Search", null, freelance, null, null));
        habitDatabase.addHabit(new Habit("clean kitchen", null, work, null, null));
        habitDatabase.addHabit(new Habit("clean bathroom", null, work, null, null));

        long entryId = habitDatabase.addEntry(programmingId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(freelanceId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(programmingId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(freelanceId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(freelanceId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(freelanceId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(freelanceId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(programmingId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(programmingId, new SessionEntry(0,0,"Hello"));
        habitDatabase.addEntry(freelanceId, new SessionEntry(0,0,"Hello"));


        habitDatabase.deleteEntry(habitDatabase.getEntryIdFromIndex(programmingId, 0));
        habitDatabase.deleteEntry(habitDatabase.getEntryIdFromIndex(programmingId, 0));

        habitDatabase.deleteHabit(programmingId);

        if(entryId == -1){
            Toast.makeText(this, "error add entry", Toast.LENGTH_SHORT).show();
        }else{
            SessionEntry entry = habitDatabase.getEntry(entryId);
            if(entry != null){
                Toast.makeText(this, entry.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        TextView main = (TextView)findViewById(R.id.text);
        StringBuilder databaseString = new StringBuilder();

        for(int i = 0; i<habitDatabase.getNumberOfCategories(); i++){
            long categoryId = habitDatabase.getCategoryIdFromIndex(i);
            ArrayList<Habit> habits = habitDatabase.getHabits(categoryId);

            for(Habit habit : habits){
                databaseString.append(habit.toString());
            }
        }

        main.setText(databaseString.toString());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
