package com.example.brandon.habitlogger.ui.Activities.ActiveSessionsActivity;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewHolder;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 * RecyclerView adapter for active session activity.
 */

public class ActiveSessionViewAdapter extends RecyclerView.Adapter<ActiveSessionViewAdapter.ViewHolder> {

    //region (Member attributes)
    private List<SessionEntry> mSessionEntries;
    private HabitDatabase mHabitDatabase;
    private SessionManager mSessionManager;
    //endregion

    //region Code responsible for providing an interface
    OnClickListeners mListener;

    public interface OnClickListeners {
        void onSessionViewClick(long habitId);

        void onSessionPauseButtonClick(ViewHolder holder, long habitId);
    }
    //endregion

    public ActiveSessionViewAdapter(List<SessionEntry> sessionEntries, Context context, OnClickListeners listener) {
        mSessionEntries = sessionEntries;
        mHabitDatabase = new HabitDatabase(context);
        mSessionManager = new SessionManager(context);
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, time, timeStarted;
        public ImageView accent;
        public ImageButton pauseButton;
        public CardView rootView;

        public long habitId;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.active_session_habit_name);
            this.time = (TextView) view.findViewById(R.id.active_habit_time);
            this.timeStarted = (TextView) view.findViewById(R.id.time_started);
            this.pauseButton = (ImageButton) view.findViewById(R.id.session_pause_play);
            this.accent = (ImageView) view.findViewById(R.id.card_accent);
            this.rootView = (CardView) view.getRootView();
        }

        public void bindSessionEntry(SessionEntry entry){
            long habitId = entry.getHabit().getDatabaseId();

            if (mSessionManager.getIsSessionActive(habitId)) {
                this.habitId = habitId;
                this.name.setText(entry.getHabit().getName());
                this.time.setText(entry.stringifyDuration());
                this.timeStarted.setText(entry.stringifyStartingTime("h:mm a"));

                int color = mHabitDatabase.getHabitColor(habitId);
                this.accent.setColorFilter(color);

                boolean isPaused = mSessionManager.getIsPaused(habitId);
                int res = HabitViewHolder.getResourceIdForPauseButton(isPaused);
                this.pauseButton.setImageResource(res);

                float alpha = isPaused ? 0.50f : 1.0f;
                this.accent.setAlpha(alpha);
                this.name.setAlpha(alpha);
                this.time.setAlpha(alpha);
                this.timeStarted.setAlpha(alpha);

                this.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onSessionViewClick(ViewHolder.this.habitId);
                    }
                });

                this.pauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onSessionPauseButtonClick(ViewHolder.this, ViewHolder.this.habitId);
                    }
                });
            }
        }

    }

    //region Methods responsible for creating and binding rootView holders
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.active_session_card, parent, false);
        setMargins(itemView, 0, 20, 0, 20);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        if (!payloads.isEmpty()) {
            SessionEntry entry = (SessionEntry)payloads.get(0);
            holder.bindSessionEntry(entry);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SessionEntry entry = mSessionEntries.get(position);
        holder.bindSessionEntry(entry);
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
    //endregion

    //region Methods responsible for exposing the data set
    @Override
    public long getItemId(int position) {
        return mSessionEntries.get(position).getDatabaseId();
    }

    @Override
    public int getItemCount() {
        return mSessionEntries.size();
    }

    public List<SessionEntry> getSessionEntries() {
        return mSessionEntries;
    }
    //endregion -- end --

}
