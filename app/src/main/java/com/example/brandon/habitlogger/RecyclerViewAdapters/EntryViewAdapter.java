package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.LayoutCheckableTextView;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 * RecyclerView mAdapter for displaying entries.
 */

public class EntryViewAdapter extends RecyclerView.Adapter<EntryViewAdapter.ViewHolder> {

    List<SessionEntry> sessionEntries;
    HabitDatabase habitDatabase;
    SessionManager sessionManager;

    Context context;

    OnClickListeners listener;

    public interface OnClickListeners {
        void onEntryViewClick(long habitId, long entryId);
    }

    public OnClickListeners getListener() {
        return listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LayoutCheckableTextView noteText;
        public TextView startTimeText, durationText;
        public ImageButton expandNote;
        public ImageView accent;
        public CardView rootView;

        public long entryId;
        public long habitId;

        public ViewHolder(View view) {
            super(view);
            this.startTimeText = (TextView) view.findViewById(R.id.entry_start_time);
            this.durationText = (TextView) view.findViewById(R.id.entry_duration);
            this.noteText = (LayoutCheckableTextView) view.findViewById(R.id.entry_note);
            this.expandNote = (ImageButton) view.findViewById(R.id.expand_note);
            this.accent = (ImageView) view.findViewById(R.id.card_accent);
            this.rootView = (CardView) view.getRootView();
        }
    }

    public EntryViewAdapter(List<SessionEntry> sessionEntries, Context context, OnClickListeners listener) {
        this.sessionEntries = sessionEntries;
        this.habitDatabase = new HabitDatabase(context);
        this.sessionManager = new SessionManager(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_card, parent, false);

        setMargins(itemView, 0, 4, 0, 4);

        return new ViewHolder(itemView);
    }

    int getDP(int value) {
        Resources res = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                res.getDisplayMetrics()
        );
    }

    public void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            p.setMargins(getDP(l), getDP(t), getDP(r), getDP(b));
            v.requestLayout();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SessionEntry item = sessionEntries.get(position);
        holder.entryId = item.getDatabaseId();
        holder.habitId = item.getHabitId();

        holder.startTimeText.setText(item.stringifyStartingTime("h:mm a"));
        holder.durationText.setText(item.stringifyDuration());

        holder.accent.setBackgroundColor(habitDatabase.getHabitColor(holder.habitId));

        if (!item.getNote().equals("")) {
            holder.noteText.setText(item.getNote());
            holder.noteText.setTypeface(Typeface.DEFAULT);
        }else{
            holder.noteText.setText(context.getResources().getString(R.string.no_note_available_entry));
            holder.noteText.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        }

        final int maxLines = context.getResources().getInteger(R.integer.entry_card_note_max_lines);
        holder.noteText.setOnLayoutListener(new LayoutCheckableTextView.OnLayoutListener() {
            @Override
            public void onLayoutCreated(TextView view) {
                if (view.getLineCount() >= maxLines) {
                    holder.expandNote.setVisibility(View.VISIBLE);
                }
            }
        });


        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onEntryViewClick(holder.habitId, item.getDatabaseId());
            }
        });

        holder.expandNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expand = holder.noteText.getMaxLines() == maxLines;
                holder.noteText.setMaxLines(expand ? 100 : maxLines);

                holder.expandNote.setImageResource(expand ? R.drawable.ic_arrow_drop_up_black_24dp :
                        R.drawable.ic_arrow_drop_down_black_24dp);
            }
        });
    }

    /**
     * SO http://stackoverflow.com/a/15567519/3589791
     *
     * @return True when TextView has been condensed, otherwise False.
     */
    public boolean isTextViewEllipsized(TextView textview) {
        Layout layout = textview.getLayout();
        if (layout != null) {
            int lines = layout.getLineCount();
            if (lines > 0) {
                int ellipsisCount = layout.getEllipsisCount(lines - 1);
                if (ellipsisCount > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return sessionEntries.size();
    }
}
