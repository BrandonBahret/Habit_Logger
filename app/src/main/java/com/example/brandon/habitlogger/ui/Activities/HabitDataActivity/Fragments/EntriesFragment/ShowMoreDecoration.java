package com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.Fragments.EntriesFragment;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.View;
import android.widget.ImageButton;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.ui.Widgets.LayoutCheckableTextView;

/**
 * Created by Brandon on 5/7/2017.
 */

public class ShowMoreDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final int maxLines = parent.getContext().getResources().getInteger(R.integer.entry_card_note_max_lines);

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            LayoutCheckableTextView note = (LayoutCheckableTextView) view.findViewById(R.id.entry_note);
            ImageButton expandNote = (ImageButton) view.findViewById(R.id.expand_note);

            Layout l = note.getLayout();
            if (l != null) {
                int lines = l.getLineCount();
                if (lines > 0) {
                    if (l.getEllipsisCount(lines - 1) > 0)
                        expandNote.setVisibility(View.VISIBLE);
                    else if(lines <= maxLines) expandNote.setVisibility(View.INVISIBLE);
                }
            }

        }
    }
}
