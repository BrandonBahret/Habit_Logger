package com.example.brandon.habitlogger.ui.Activities.MainActivity;

import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by Brandon on 2/6/2017.
 * ViewHolder for HabitViewAdapter
 */

public class HabitViewHolder extends ChildViewHolder {

    //region (Member attributes)
    public CardView rootView;
    public ImageView categoryAccent;
    public Toolbar toolbar;
    public ImageButton playButton;
    public TextView time;
    public TextView cardTitle;
    //endregion

    public HabitViewHolder(View view) {
        super(view);
        this.categoryAccent = (ImageView) view.findViewById(R.id.category_accent);
        this.toolbar = (Toolbar) view.findViewById(R.id.card_toolbar);
        this.playButton = (ImageButton) view.findViewById(R.id.session_control_button);
        this.time = (TextView) view.findViewById(R.id.habit_card_time_display);
        this.rootView = (CardView) view;
        this.cardTitle = (TextView) view.findViewById(R.id.card_title);
    }

    //region Bind habit object
    public void bindItem(final Habit item,
                         final HabitViewAdapter.MenuItemClickListener menuItemClickListener,
                         final HabitViewAdapter.ButtonClickCallback buttonClickCallback) {

        // Make sure menu hasn't been inflated yet.
        int menuSize = toolbar.getMenu().size();
        if (menuSize == 0) {
            if (item.getIsArchived()) toolbar.inflateMenu(R.menu.menu_archived_habit_card);

            else toolbar.inflateMenu(R.menu.menu_habit_card);
        }

        categoryAccent.setBackgroundColor(item.getColor());
        cardTitle.setText(item.getName());

        setListeners(item, menuItemClickListener, buttonClickCallback);
    }

    private void setListeners(
            Habit item,
            HabitViewAdapter.MenuItemClickListener menuItemClickListener,
            HabitViewAdapter.ButtonClickCallback buttonClickCallback) {

        toolbar.setOnMenuItemClickListener(
                getMenuItemClickListener(item, menuItemClickListener)
        );

        long habitId = item.getDatabaseId();
        playButton.setOnClickListener(
                buttonClickCallback.getPlayButtonClickedListener(habitId, this)
        );

        playButton.setOnLongClickListener(
                buttonClickCallback.getPlayButtonLongClickedListener(habitId, this)
        );

        rootView.setOnClickListener(
                buttonClickCallback.getHabitViewClickedListener(habitId, this)
        );
    }

    private Toolbar.OnMenuItemClickListener getMenuItemClickListener
            (final Habit item, final HabitViewAdapter.MenuItemClickListener menuItemClickListener) {

        return new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                long habitId = item.getDatabaseId();

                switch (id) {
                    case R.id.habit_menu_edit:
                        menuItemClickListener.onHabitEditClick(habitId, HabitViewHolder.this);
                        break;

                    case R.id.menu_enter_session:
                        menuItemClickListener.onHabitStartSession(habitId, HabitViewHolder.this);
                        break;

                    case R.id.habit_menu_reset:
                        menuItemClickListener.onHabitResetClick(habitId, HabitViewHolder.this);
                        break;

                    case R.id.habit_menu_delete:
                        menuItemClickListener.onHabitDeleteClick(habitId, HabitViewHolder.this);
                        break;

                    case R.id.habit_menu_export:
                        menuItemClickListener.onHabitExportClick(habitId, HabitViewHolder.this);
                        break;

                    case R.id.habit_menu_archive:
                        menuItemClickListener.onHabitArchiveClick(habitId, HabitViewHolder.this);
                        break;
                }

                return true;
            }
        };
    }
    //endregion

    //region Bind session entry object
    public void bindSessionEntry(SessionEntry entry) {
        if (entry != null) {
            time.setText(entry.stringifyDuration());
            time.setAlpha(1f);
            playButton.setImageResource(
                    getResourceIdForPauseButton(entry.getIsPaused())
            );
        }
        else {
            time.setText("00:00:00");
            time.setAlpha(0.60f);
            playButton.setImageResource(
                    R.drawable.ic_add_timer_24dp
            );
        }
    }

    public static int getResourceIdForPauseButton(boolean isPaused) {
        return isPaused ? R.drawable.ic_play_24dp :
                R.drawable.ic_pause_24dp;
    }
    //endregion

}