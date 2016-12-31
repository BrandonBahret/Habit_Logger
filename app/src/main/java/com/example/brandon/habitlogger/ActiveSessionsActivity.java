package com.example.brandon.habitlogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.ActiveSessionViewAdapter;
import com.example.brandon.habitlogger.RecyclerVIewAdapters.RecyclerTouchListener;
import com.example.brandon.habitlogger.SessionManager.SessionManager;

import java.util.List;

public class ActiveSessionsActivity extends AppCompatActivity {

    List<SessionEntry> sessionEntries;
    SessionManager sessionManager;

    ActiveSessionViewAdapter sessionViewAdapter;
    RecyclerView sessionViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_sessions);

        sessionManager = new SessionManager(this);
        sessionEntries = sessionManager.getActiveSessionList();

        sessionViewContainer = (RecyclerView) findViewById(R.id.session_view_container);
        sessionViewAdapter = new ActiveSessionViewAdapter(sessionEntries, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        sessionViewContainer.setLayoutManager(layoutManager);
        sessionViewContainer.setItemAnimator(new DefaultItemAnimator());
        sessionViewContainer.setAdapter(sessionViewAdapter);

        sessionViewContainer.addOnItemTouchListener(new RecyclerTouchListener(this, sessionViewContainer, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(ActiveSessionsActivity.this, "session clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }


}
