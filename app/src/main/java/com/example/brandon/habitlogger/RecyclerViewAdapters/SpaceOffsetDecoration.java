package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Brandon on 3/5/2017.
 * Item decorator to add spacers into RecyclerViews
 */

public class SpaceOffsetDecoration extends RecyclerView.ItemDecoration {
    private int mBottomOffset;
    private int mTopOffset;

    public SpaceOffsetDecoration(int bottomOffset, int topOffset) {
        mBottomOffset = bottomOffset;
        mTopOffset = topOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int dataSize = state.getItemCount();
        int position = parent.getChildAdapterPosition(view);

        if (dataSize > 0 && position != 0 && position == dataSize - 1)
            outRect.set(0, 0, 0, mBottomOffset);

        else if (dataSize > 0 && position == 0)
            outRect.set(0, mTopOffset, 0, 0);

        else
            outRect.set(0, 0, 0, 0);

    }
}