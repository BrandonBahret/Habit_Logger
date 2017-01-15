package com.example.brandon.habitlogger.RecyclerVIewAdapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.HabitDatabase.SessionEntry;
import com.example.brandon.habitlogger.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.Preferences.PreferenceChecker;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 */

public class EntryViewAdapter extends RecyclerView.Adapter<EntryViewAdapter.ViewHolder> {

    List<SessionEntry> sessionEntries;
    HabitDatabase habitDatabase;
    SessionManager sessionManager;

    Context context;

    OnClickListeners listener;
    public interface OnClickListeners {
        void onRootClick(long habitId, long entryId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView startTimeText, durationText, noteText;
        public ImageButton expandNote;
        public ImageView accent;
        public CardView rootView;

        public long entryId;
        public Long habitId;

        public ViewHolder(View view) {
            super(view);
            this.startTimeText = (TextView) view.findViewById(R.id.entry_start_time);
            this.durationText  = (TextView) view.findViewById(R.id.entry_duration);
            this.noteText      = (TextView) view.findViewById(R.id.entry_note);
            this.expandNote    = (ImageButton) view.findViewById(R.id.expand_note);
            this.accent   = (ImageView) view.findViewById(R.id.card_accent);
            this.rootView = (CardView)view.getRootView();
        }
    }

    public EntryViewAdapter(List<SessionEntry> sessionEntries, Context context, OnClickListeners listener){
        this.sessionEntries = sessionEntries;
        this.habitDatabase  = new HabitDatabase(context, null, false);
        this.sessionManager = new SessionManager(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_card, parent, false);

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
        final SessionEntry item = sessionEntries.get(position);
        holder.entryId = item.getDatabaseId();
        holder.habitId = item.getHabitId();

        String dateFormat = new PreferenceChecker(context).stringGetDateFormat();

        holder.startTimeText.setText(item.getStartTimeAsString(dateFormat + " h:mm a"));
        holder.durationText.setText(item.getDurationAsString());
        holder.accent.setBackgroundColor(habitDatabase.getHabitColor(holder.habitId));

        String note = item.getNote();
        if (!note.equals("")) {
            holder.noteText.setText(item.getNote());
            holder.noteText.setTypeface(Typeface.DEFAULT);
        }

        // Thanks SO http://stackoverflow.com/questions/20069895/detect-if-textview-is-ellipsized-before-layout-is-shown
        ViewTreeObserver vto = holder.noteText.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if(!isTextViewEllipsized(holder.noteText)
                                && holder.noteText.getMaxLines() == context.getResources().getInteger(R.integer.entry_card_note_max_lines)){

                            holder.expandNote.setVisibility(View.GONE);
                        }
                    }
                });


        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRootClick(holder.habitId, item.getDatabaseId());
            }
        });

        holder.expandNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expand = holder.noteText.getMaxLines() == context.getResources().getInteger(R.integer.entry_card_note_max_lines);
                holder.noteText.setMaxLines(expand? 100 : context.getResources().getInteger(R.integer.entry_card_note_max_lines));

                holder.expandNote.setImageResource(expand? R.drawable.ic_arrow_drop_up_black_24dp :
                        R.drawable.ic_arrow_drop_down_black_24dp);
            }
        });
    }

    /**
     * Checks if the text of the supplied {@link TextView} has been ellipsized.
     *
     * Thanks SO http://stackoverflow.com/a/33499057/3589791
     *
     * @param textView
     *         The {@link TextView} to check its text.
     *
     * @return {@code True} if the text of the supplied {@code textView} has been ellipsized.
     */
    public static boolean isTextViewEllipsized(final TextView textView) {
        // Initialize the resulting variable
        boolean result = false;
        // Check if the supplied TextView is not null
        if (textView != null) {
            // Check if ellipsizing the text is enabled
            final TextUtils.TruncateAt truncateAt = textView.getEllipsize();
            if (truncateAt != null && !TextUtils.TruncateAt.MARQUEE.equals(truncateAt)) {
                // Retrieve the layout in which the text is rendered
                final Layout layout = textView.getLayout();
                if (layout != null) {
                    // Iterate all lines to search for ellipsized text
                    for (int index = 0; index < layout.getLineCount(); ++index) {
                        // Check if characters have been ellipsized away within this line of text
                        result = layout.getEllipsisCount(index) > 0;
                        // Stop looping if the ellipsis character has been found
                        if (result) {
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return sessionEntries.size();
    }
}
