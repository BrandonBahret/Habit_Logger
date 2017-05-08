package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.EntriesFragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.ThemeColorPalette;
import com.example.brandon.habitlogger.data.DataModels.DataCollections.SessionEntryCollection;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.ui.Widgets.LayoutCheckableTextView;

/**
 * Created by Brandon on 12/26/2016.
 * RecyclerView mAdapter for displaying entries.
 */

public class EntryViewAdapter extends RecyclerView.Adapter<EntryViewAdapter.ViewHolder> {

    //region (Member attributes)
    private SessionEntryCollection mSessionEntries;
    private HabitDatabase mHabitDatabase;

    private Context mContext;
    //endregion

    //region Code responsible for providing an interface
    OnClickListeners mListener;
    private ThemeColorPalette mColorPalette = null;

    public interface OnClickListeners {
        void onEntryViewClick(long habitId, long entryId);
    }

    public OnClickListeners getListener() {
        return mListener;
    }
    //endregion

    //region Constructors {}
    public EntryViewAdapter(SessionEntryCollection sessionEntries, Context context, OnClickListeners listener) {
        mContext = context;
        mSessionEntries = sessionEntries;
        mHabitDatabase = new HabitDatabase(context);
        mListener = listener;
    }

    public EntryViewAdapter(SessionEntryCollection sessionEntries, Context context, ThemeColorPalette colorPalette, OnClickListeners listener) {
        mContext = context;
        mSessionEntries = sessionEntries;
        mHabitDatabase = new HabitDatabase(context);
        mListener = listener;
        mColorPalette = colorPalette;
    }
    //endregion -- end --

    public static class ViewHolder extends RecyclerView.ViewHolder {
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

    //region Methods responsible for creating and binding rootView holders
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_card, parent, false);

        int dpSize = 4;
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        setMargins(itemView, 0, margin, 0, margin);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SessionEntry item = mSessionEntries.get(position);
        holder.entryId = item.getDatabaseId();
        holder.habitId = item.getHabitId();

        holder.startTimeText.setText(item.stringifyStartingTime("h:mm a"));
        holder.durationText.setText(item.stringifyDuration());

        if (mColorPalette == null)
            holder.accent.setBackgroundColor(mHabitDatabase.getHabitColor(item.getHabitId()));
        else
            holder.accent.setBackgroundColor(mColorPalette.getBaseColor());

        if (!item.getNote().equals("")) {
            holder.noteText.setText(item.getNote());
            holder.noteText.setTypeface(Typeface.DEFAULT);
        }
        else {
            holder.noteText.setText(mContext.getResources().getString(R.string.no_note_available_entry));
            holder.noteText.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        }

        final int maxLines = mContext.getResources().getInteger(R.integer.entry_card_note_max_lines);

        // This is replaced with ShowMoreDecoration
//        holder.noteText.setOnLayoutListener(new LayoutCheckableTextView.OnLayoutListener() {
//            @Override
//            public void onLayoutCreated(TextView view) {
//                if (view.getLineCount() >= maxLines) {
//                    holder.expandNote.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionEntry item = mSessionEntries.get(holder.getAdapterPosition());
                mListener.onEntryViewClick(item.getHabitId(), item.getDatabaseId());
            }
        });

        holder.expandNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int noteMaxLines = holder.noteText.getMaxLines();
                boolean expand = noteMaxLines == maxLines;

                // todo : replace '50' with code that determines what the max lines should be for the given text
                final int targetMaxLines = expand ? 50 : maxLines;
//                holder.noteText.setMaxLines(targetMaxLines);

                ObjectAnimator animation = ObjectAnimator.ofInt(
                        holder.noteText,
                        "maxLines",
                        targetMaxLines
                );

                // This is a hack to ensure the full note is always shown.
                // This assumes '50' lines will always go off screen
//                animation.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        if (targetMaxLines > maxLines)
//                            holder.noteText.setMaxLines(5000);
//                    }
//                });

                animation.setDuration(150);
                animation.start();

                float rotation = holder.expandNote.getRotation();
                ValueAnimator anim = ValueAnimator.ofFloat(rotation, rotation + 180);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float val = (Float) valueAnimator.getAnimatedValue();
                        holder.expandNote.setRotation(val);
                    }
                });
                anim.setDuration(150);
                anim.start();

//                holder.expandNote.setImageResource(expand ? R.drawable.ic_arrow_drop_up_24dp :
//                        R.drawable.ic_arrow_drop_down_24dp);
            }
        });
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
    //endregion -- end --

    @Override
    public int getItemCount() {
        if (mSessionEntries == null) return 0;
        return mSessionEntries.size();
    }

    //region Setters {}
    public void setColorPalette(ThemeColorPalette colorPalette) {
        mColorPalette = colorPalette;
    }
    //endregion

}
