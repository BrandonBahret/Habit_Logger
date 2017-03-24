package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.MainActivity;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 * RecyclerView adapter for displaying habit cards.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class HabitViewAdapter extends RecyclerView.Adapter<HabitViewHolder> {

    //    private final SessionManager sessionManager;
    List<Habit> habitsList;

    private MenuItemClickListener menuItemClickListener;
    private ButtonClickListener buttonClickListener;
    private static List<SessionEntry> currentEntries;

    public interface MenuItemClickListener {
        void onEditClick(long habitId);

        void onResetClick(long habitId);

        void onDeleteClick(long habitId);

        void onExportClick(long habitId);

        void onArchiveClick(long habitId);

        void onStartSession(long habitId);
    }

    public interface ButtonClickListener {
        void onPlayButtonClicked(long habitId);

        void onPlayButtonLongClicked(long habitId);

        void onCardClicked(long habitId);
    }

    public HabitViewAdapter(List<Habit> habitsList, MainActivity context, MenuItemClickListener menuItemClickListener,
                            ButtonClickListener buttonClickListener) {

        this.habitsList = habitsList;
//        this.sessionManager = new SessionManager(context);
        this.menuItemClickListener = menuItemClickListener;
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_card, parent, false);

        setMargins(itemView, 0, 20, 0, 20);

        return new HabitViewHolder(itemView);
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public void onBindViewHolder(final HabitViewHolder holder, int position) {
        Habit item = habitsList.get(position);
        holder.bindItem(item, menuItemClickListener, buttonClickListener);
        holder.setEntry(getSessionForHabitId(item.getDatabaseId()));
    }

    @Override
    public void onBindViewHolder(HabitViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads != null && !payloads.isEmpty()) {
            holder.setEntry((SessionEntry) payloads.get(0));
        }
    }

    public void removeAt(int position) {
        habitsList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateHabitViews(List<SessionEntry> entries) {
        currentEntries = entries;

        for (SessionEntry entry : entries) {
            long habitId = entry.getHabitId();
            int adapterPosition = this.getAdapterItemPosition(habitId);
            notifyItemChanged(adapterPosition, entry);
        }
    }

    private SessionEntry getSessionForHabitId(long databaseId) {
        if (currentEntries != null) {
            for (SessionEntry entry : currentEntries) {
                if (entry.getHabitId() == databaseId) {
                    return entry;
                }
            }
        }

        return null;
    }

    private int getAdapterItemPosition(long habitId) {
        for (int position = 0; position < getItemCount(); position++) {
            if (this.habitsList.get(position).getDatabaseId() == habitId)
                return position;
        }

        return 0;
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }
}

