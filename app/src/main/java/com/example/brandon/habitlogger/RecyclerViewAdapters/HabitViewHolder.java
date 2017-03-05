package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.TimeDisplay;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by Brandon on 2/6/2017.
 * ViewHolder for HabitViewAdapter
 */

public class HabitViewHolder extends ChildViewHolder {
    public CardView rootView;
    public ImageView categoryAccent;
    public Toolbar toolbar;
    public ImageButton playButton;
    public TextView time;

    public HabitViewHolder(View view) {
        super(view);
        this.categoryAccent = (ImageView) view.findViewById(R.id.category_accent);
        this.toolbar = (Toolbar) view.findViewById(R.id.card_toolbar);
        this.playButton = (ImageButton) view.findViewById(R.id.session_control_button);
        this.time = (TextView) view.findViewById(R.id.habit_card_time_display);
        this.rootView = (CardView) view;
    }

    public void bindItem(final Habit item,
                         final HabitViewAdapter.MenuItemClickListener menuItemClickListener,
                         final HabitViewAdapter.ButtonClickListener buttonClickListener) {

        // Make sure menu hasn't been inflated yet.
        int menuSize = toolbar.getMenu().size();
        if (menuSize == 0) {
            if (item.getIsArchived()) {
                toolbar.inflateMenu(R.menu.menu_archived_habit_card);
            }
            else {
                toolbar.inflateMenu(R.menu.menu_habit_card);
            }
        }

        categoryAccent.setBackgroundColor(item.getColor());
        toolbar.setTitle(item.getName());

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();

                long habitId = item.getDatabaseId();

                switch (id) {
                    case (R.id.habit_menu_edit): {
                        menuItemClickListener.onEditClick(habitId);
                    }
                    break;

                    case (R.id.menu_enter_session): {
                        menuItemClickListener.onStartSession(habitId);
                    }
                    break;

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

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long habitId = item.getDatabaseId();
                buttonClickListener.onPlayButtonClicked(habitId);
            }
        });

        playButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                long habitId = item.getDatabaseId();

                buttonClickListener.onPlayButtonLongClicked(habitId);
                return true;
            }
        });

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long habitId = item.getDatabaseId();

                buttonClickListener.onCardClicked(habitId);
            }
        });
    }

    public static int getResourceIdForPauseButton(boolean isPaused) {
        return isPaused ? R.drawable.ic_play_black :
                R.drawable.ic_pause_black;
    }

    public void setEntry(SessionEntry entry) {
        time.setText(TimeDisplay.getDisplay(entry.getDuration()));
        playButton.setImageResource(
                getResourceIdForPauseButton(entry.getIsPaused())
        );
    }
}