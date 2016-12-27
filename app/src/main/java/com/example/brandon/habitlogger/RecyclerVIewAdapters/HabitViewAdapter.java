package com.example.brandon.habitlogger.RecyclerVIewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 */

public class HabitViewAdapter extends RecyclerView.Adapter<HabitViewAdapter.ViewHolder> {

    List<Habit> habitsList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, streakCount;
        public ImageView categoryAccent;

        public ViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.habit_name_card_view);
            this.streakCount = (TextView) view.findViewById(R.id.streak_counter_card_view);
            this.categoryAccent = (ImageView) view.findViewById(R.id.category_accent);
        }
    }

    public HabitViewAdapter(List<Habit> habitsList){
        this.habitsList = habitsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_view_layout, parent, false);

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
        Habit item = habitsList.get(position);

        holder.name.setText(item.getName());
        holder.streakCount.setText(String.valueOf(item.calculateStreakCount()));
        holder.categoryAccent.setBackgroundColor(item.getCategory().getColorAsInt());
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }
}
