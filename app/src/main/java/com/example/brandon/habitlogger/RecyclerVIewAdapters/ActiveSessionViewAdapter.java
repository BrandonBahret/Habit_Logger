package com.example.brandon.habitlogger.RecyclerVIewAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.SessionManager.SessionManager;

import java.util.List;
import java.util.Locale;

/**
 * Created by Brandon on 12/26/2016.
 */

public class ActiveSessionViewAdapter extends RecyclerView.Adapter<ActiveSessionViewAdapter.ViewHolder> {

    List<SessionEntry> sessionEntries;
    HabitDatabase habitDatabase;
    SessionManager sessionManager;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, time;
        public ImageView accent;
        public ImageButton pauseButton;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.active_session_habit_name);
            this.time = (TextView) view.findViewById(R.id.active_habit_time);
            this.pauseButton = (ImageButton) view.findViewById(R.id.session_pause_play);
            this.accent = (ImageView) view.findViewById(R.id.card_accent);
        }
    }

    public ActiveSessionViewAdapter(List<SessionEntry> sessionEntries, Context context){
        this.sessionEntries = sessionEntries;
        this.habitDatabase  = new HabitDatabase(context, null, false);
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.active_session_card, parent, false);

        setMargins(itemView, 0, 20, 0, 20);

        return new ViewHolder(itemView);
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SessionEntry item = sessionEntries.get(position);

        holder.name.setText(item.getName());

        SessionManager.TimeDisplay time = new SessionManager.TimeDisplay(item.getDuration());
        String timeDisplay = String.format(Locale.US, "%02d:%02d:%02d", time.hours, time.minutes, time.seconds);
        holder.time.setText(timeDisplay);

        if (sessionManager.getIsPaused(item.getHabitId())) {
            holder.pauseButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        } else {
            holder.pauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        }

        holder.accent.setColorFilter(habitDatabase.getHabitColor(item.getHabitId()));
    }

    @Override
    public int getItemCount() {
        return sessionEntries.size();
    }
}
