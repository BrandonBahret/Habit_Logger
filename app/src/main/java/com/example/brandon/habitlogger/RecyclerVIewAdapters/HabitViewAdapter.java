package com.example.brandon.habitlogger.RecyclerVIewAdapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 */

public class HabitViewAdapter extends RecyclerView.Adapter<HabitViewAdapter.ViewHolder> {

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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView rootView;
        public ImageView categoryAccent;
        public Toolbar toolbar;
        public ImageButton playButton;

        public ViewHolder(View view) {
            super(view);
            this.categoryAccent = (ImageView) view.findViewById(R.id.category_accent);
            this.toolbar = (Toolbar) view.findViewById(R.id.card_toolbar);
            this.playButton = (ImageButton) view.findViewById(R.id.session_control_button);
            this.rootView = (CardView)view;
        }
    }

    public HabitViewAdapter(List<Habit> habitsList, MenuItemClickListener menuItemClickListener,
                            ButtonClickListener buttonClickListener){

        this.habitsList = habitsList;
        this.menuItemClickListener = menuItemClickListener;
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_card, parent, false);

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
        Habit item = habitsList.get(position);

        // Make sure menu hasn't been inflated yet.
        int menuSize = holder.toolbar.getMenu().size();
        if(menuSize == 0) {
            if (item.getIsArchived()) {
                holder.toolbar.inflateMenu(R.menu.menu_archived_habit_card);
            } else {
                holder.toolbar.inflateMenu(R.menu.menu_habit_card);
            }
        }

        holder.categoryAccent.setBackgroundColor(item.getColor());
        holder.toolbar.setTitle(item.getName());

        holder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();

                int position = holder.getAdapterPosition();
                Habit item = habitsList.get(position);
                long habitId = item.getDatabaseId();

                switch (id) {
                    case (R.id.habit_menu_edit): {
                        menuItemClickListener.onEditClick(habitId);
                    }
                    break;

                    case(R.id.menu_enter_session):{
                        menuItemClickListener.onStartSession(habitId);
                    }break;

                    case (R.id.habit_menu_delete): {
                        menuItemClickListener.onDeleteClick(habitId);
                    }
                    break;

                    case (R.id.habit_menu_export): {
                        menuItemClickListener.onExportClick(habitId);
                    }
                    break;

                    case (R.id.habit_menu_archive): {
                        menuItemClickListener.onArchiveClick(habitId);
                    }
                    break;
                }

                return true;
            }
        });

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int position = holder.getAdapterPosition();
                Habit item = habitsList.get(position);
                long habitId = item.getDatabaseId();


                buttonClickListener.onPlayButtonClicked(habitId);
            }
        });

        holder.playButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                Habit item = habitsList.get(position);
                long habitId = item.getDatabaseId();

                buttonClickListener.onPlayButtonLongClicked(habitId);
                return true;
            }
        });

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Habit item = habitsList.get(position);
                long habitId = item.getDatabaseId();

                buttonClickListener.onCardClicked(habitId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }
}

