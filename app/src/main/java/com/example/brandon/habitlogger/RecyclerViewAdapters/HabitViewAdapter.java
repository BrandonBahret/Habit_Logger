package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 * RecyclerView mAdapter for displaying habit cards.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class HabitViewAdapter extends RecyclerView.Adapter<HabitViewHolder> {

    //region (Memberr attributes)
    private List<Habit> mHabitsList;
    private static List<SessionEntry> mCurrentEntries;
    //endregion

    //region Code responsible for providing communication
    private MenuItemClickListener mMenuItemClickListener;
    private ButtonClickCallback mButtonClickCallback;

    public interface MenuItemClickListener {
        void onEditClick(long habitId);

        void onResetClick(long habitId);

        void onDeleteClick(long habitId);

        void onExportClick(long habitId);

        void onArchiveClick(long habitId);

        void onStartSession(long habitId);
    }

    public interface ButtonClickCallback {
        View.OnClickListener getPlayButtonClickedListener(final long habitId);

        View.OnLongClickListener getPlayButtonLongClickedListener(final long habitId);

        View.OnClickListener getHabitViewClickedListener(final long habitId);
    }
    //endregion -- end --

    public HabitViewAdapter(List<Habit> habitsList,
                            MenuItemClickListener menuItemClickListener,
                            ButtonClickCallback buttonClickCallback) {

        mHabitsList = habitsList;
        mMenuItemClickListener = menuItemClickListener;
        mButtonClickCallback = buttonClickCallback;
    }

    //region Methods responsible for creating and binding rootView holders
    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_card, parent, false);

        setMargins(itemView, 0, 20, 0, 20);

        return new HabitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final HabitViewHolder holder, int position) {
        Habit item = mHabitsList.get(position);
        holder.bindItem(item, mMenuItemClickListener, mButtonClickCallback);
        holder.bindSessionEntry(getSessionForHabitId(item.getDatabaseId()));
    }

    @Override
    public void onBindViewHolder(HabitViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads != null && !payloads.isEmpty()) {
            holder.bindSessionEntry((SessionEntry) payloads.get(0));
        }
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
    //endregion -- end --

    //region Methods responsible for exposing the data set
    @Override
    public int getItemCount() {
        return mHabitsList.size();
    }

    public void updateHabitViews(List<SessionEntry> entries) {
        mCurrentEntries = entries;

        for (SessionEntry entry : entries) {
            long habitId = entry.getHabitId();
            int adapterPosition = getAdapterItemPosition(habitId);
            notifyItemChanged(adapterPosition, entry);
        }
    }

    public void removeAt(int position) {
        mHabitsList.remove(position);
        notifyItemRemoved(position);
    }

    private SessionEntry getSessionForHabitId(long habitId) {
        if (mCurrentEntries != null) {
            for (SessionEntry entry : mCurrentEntries) {
                if (entry.getHabitId() == habitId)
                    return entry;
            }
        }

        return null;
    }

    private int getAdapterItemPosition(long habitId) {
        for (int position = 0; position < getItemCount(); position++) {
            if (mHabitsList.get(position).getDatabaseId() == habitId)
                return position;
        }

        return -1;
    }
    //endregion

}

