package com.example.brandon.habitlogger.RecyclerVIewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 * RecyclerView adapter for displaying habit cards.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class HabitViewAdapter extends RecyclerView.Adapter<HabitViewHolder> {

    List<Habit> habitsList;

    private MenuItemClickListener menuItemClickListener;
    private ButtonClickListener buttonClickListener;

    public interface MenuItemClickListener{
        void onEditClick(long habitId);
        void onDeleteClick(long habitId);
        void onExportClick(long habitId);
        void onArchiveClick(long habitId);
        void onStartSession(long habitId);
    }

    public interface ButtonClickListener{
        void onPlayButtonClicked(long habitId);
        void onPlayButtonLongClicked(long habitId);

        void onCardClicked(long habitId);
    }

    public HabitViewAdapter(List<Habit> habitsList, MenuItemClickListener menuItemClickListener,
                            ButtonClickListener buttonClickListener){

        this.habitsList = habitsList;
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

    public static void setMargins (View v, int l, int t, int r, int b) {
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
    }

    public void updateHabitViews(List<SessionEntry> entries) {
        for(SessionEntry entry : entries){
            long habitId = entry.getHabitId();

            // Todo get viewholder from position and .setEntry(entry);
        }
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }
}

