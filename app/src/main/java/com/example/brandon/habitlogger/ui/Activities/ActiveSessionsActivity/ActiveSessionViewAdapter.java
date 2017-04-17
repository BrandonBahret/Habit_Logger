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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SessionEntry item = mSessionEntries.get(position);
        long habitId = item.getHabit().getDatabaseId();

        if (mSessionManager.getIsSessionActive(habitId)) {
            holder.habitId = habitId;
            holder.name.setText(item.getHabit().getName());
            holder.time.setText(item.stringifyDuration());
            holder.timeStarted.setText(item.stringifyStartingTime("h:mm a"));

            int color = mHabitDatabase.getHabitColor(habitId);
            holder.accent.setColorFilter(color);

            boolean isPaused = mSessionManager.getIsPaused(habitId);
            int res = HabitViewHolder.getResourceIdForPauseButton(isPaused);
            holder.pauseButton.setImageResource(res);

            float alpha = isPaused ? 0.50f : 1.0f;
            holder.accent.setAlpha(alpha);
            holder.name.setAlpha(alpha);
            holder.time.setAlpha(alpha);
            holder.timeStarted.setAlpha(alpha);

            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSessionViewClick(holder.habitId);
                }
            });

            holder.pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSessionPauseButtonClick(holder, holder.habitId);
                }
            });
        }
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
