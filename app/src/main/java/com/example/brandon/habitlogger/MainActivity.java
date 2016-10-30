package com.example.brandon.habitlogger;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;

import static com.example.brandon.habitlogger.R.menu.main;

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

        HabitCategory cat = new HabitCategory("color", "ab cd ef gh");
        habitDatabase.addCategory(cat);
        habitDatabase.addCategory(new HabitCategory("color", "gh cd ab ef"));
        habitDatabase.addCategory(new HabitCategory("color", "ef ef gh ab"));


        long habitId = habitDatabase.addHabit(new Habit("AB CD EF", null, cat, null, null));
        long habitId2 = habitDatabase.addHabit(new Habit("CD AB EF", null, cat, null, null));
        habitDatabase.addHabit(new Habit("EF AB CD", null, cat, null, null));

        SessionEntry entry1 = new SessionEntry(5000, 0, "This is a note");
        SessionEntry entry2 = new SessionEntry(6000, 0, "Note this is");
        SessionEntry entry3 = new SessionEntry(7000, 0, "a this is note");
        SessionEntry entry4 = new SessionEntry(8000, 0, "herro");

        habitDatabase.addEntry(habitId, entry1);
        habitDatabase.addEntry(habitId, entry2);
        habitDatabase.addEntry(habitId, entry3);
        habitDatabase.addEntry(habitId, entry4);

        habitDatabase.addEntry(habitId2, entry1);
        habitDatabase.addEntry(habitId2, entry2);
        habitDatabase.deleteHabit(habitId2);
        habitId2 = habitDatabase.addHabit(new Habit("CD AB EF", null, cat, null, null));
        habitDatabase.addEntry(habitId2, entry3);
        habitDatabase.addEntry(habitId2, entry4);

        showDatabase();
    }

    public void showDatabase(){
        StringBuilder databaseString = new StringBuilder();

        for(int categoryIndex = 0; categoryIndex < habitDatabase.getNumberOfCategories(); categoryIndex++){
            long categoryId = habitDatabase.getCategoryIdFromIndex(categoryIndex);

            Habit habits[] = habitDatabase.getHabits(categoryId);
            for(Habit eachHabit : habits){
                databaseString.append(eachHabit.toString());
            }
        }

        long categoryIds[] = habitDatabase.searchCategoryIdsByName("ini");
        Log.d("categories found", String.valueOf(categoryIds.length));

        databaseString.append(habitDatabase.getNumberOfCategories());
        TextView main = (TextView)findViewById(R.id.text);
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
        getMenuInflater().inflate(main, menu);
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
        else if(id == R.id.reset_database){
            habitDatabase.resetDatabase();
            showDatabase();
        }
        else if(id == R.id.save_to_sd){
            habitDatabase.copyDatabaseToPhoneStorage();
            Toast.makeText(this, "Saved database", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
