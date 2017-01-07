package com.example.brandon.habitlogger.RecyclerVIewAdapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by Brandon on 12/26/2016.
 */

public class ActiveSessionViewAdapterWithSections extends RecyclerView.Adapter<ActiveSessionViewAdapterWithSections.ViewHolder> {

    List<SessionEntry> sessionEntries;
    HabitDatabase habitDatabase;
    SessionManager sessionManager;

    Context context;

    OnClickListeners listener;
    public interface OnClickListeners {
        void onRootClick(long habitId);

        void onPauseClick(ViewHolder holder, long habitId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, time;
        public ImageView accent;
        public ImageButton pauseButton;
        public CardView rootView;

        public long habitId;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.active_session_habit_name);
            this.time = (TextView) view.findViewById(R.id.active_habit_time);
            this.pauseButton = (ImageButton) view.findViewById(R.id.session_pause_play);
            this.accent = (ImageView) view.findViewById(R.id.card_accent);
            this.rootView = (CardView)view.getRootView();
        }
    }

    public ActiveSessionViewAdapterWithSections(List<SessionEntry> sessionEntries, Context context, OnClickListeners listener){
        this.sessionEntries = sessionEntries;
        this.habitDatabase  = new HabitDatabase(context, null, false);
        this.sessionManager = new SessionManager(context);
        this.context = context;
        this.listener = listener;
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SessionEntry item = sessionEntries.get(position);
        long habitId = item.getHabitId();

        if (sessionManager.isSessionActive(habitId)) {
            holder.habitId = habitId;

            holder.name.setText(item.getName());

            SessionManager.TimeDisplay time = new SessionManager.TimeDisplay(item.getDuration());
            String timeDisplay = String.format(Locale.US, "%02d:%02d:%02d", time.hours, time.minutes, time.seconds);
            holder.time.setText(timeDisplay);

            if (sessionManager.getIsPaused(habitId)) {
                holder.pauseButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
            } else {
                holder.pauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }

            holder.accent.setColorFilter(habitDatabase.getHabitColor(habitId));

            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onRootClick(holder.habitId);
                }
            });

            holder.pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onPauseClick(holder, holder.habitId);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sessionEntries.size();
    }
}
