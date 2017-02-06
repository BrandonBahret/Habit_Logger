package com.example.brandon.habitlogger;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

public class ComplexDecoration extends RecyclerView.ItemDecoration {

    private final Callback callback;
    private final TextPaint textPaint;
    private final Paint paint;
    private final int topGap;
    private final int bottomGap;
    private Paint.FontMetrics fontMetrics;

    public ComplexDecoration(Context context, Callback callback) {
        super();
        final Resources res = context.getResources();
        textPaint = new TextPaint();
        paint = new Paint();
        this.callback = callback;

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(46);
        textPaint.setColor(res.getColor(R.color.headerTextColor));
        textPaint.getFontMetrics(fontMetrics);
        textPaint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(res.getColor(R.color.colorAccent));
        fontMetrics = new Paint.FontMetrics();

        topGap = res.getDimensionPixelSize(R.dimen.indent_pad_top);
        bottomGap = res.getDimensionPixelSize(R.dimen.indent_pad_bottom);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Inset first item of a group on the top.
     * Inset all items left & right side.
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int pos = parent.getChildAdapterPosition(view);
        long groupId = callback.getGroupId(pos);
        if (groupId < 0) return;

        if (isLastInGroup(pos)) {
            outRect.bottom = bottomGap;
        }

        if (pos == 0 || isFirstInGroup(pos)) {
            outRect.top = topGap;
        } else {
            outRect.top = 0;
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        final int totalItemCount = state.getItemCount();
        final int childCount = parent.getChildCount();
        final float left = parent.getPaddingLeft();
        final float lineHeight = textPaint.getTextSize() + fontMetrics.descent;

        long prevGroupId, groupId = -1;
        for (int i = 0; i < childCount; i++) {
            final View view = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(view);

            // Ignore items without a group and items that are in same group as previous item
            prevGroupId = groupId;
            groupId = callback.getGroupId(position);
            if (groupId < 0 || groupId == prevGroupId) continue;

            // Ignore empty text line
            final String textLine = callback.getGroupFirstLine(position).toUpperCase();
            if (TextUtils.isEmpty(textLine)) continue;

            // Find appropriate y position for text; on screen unless pushed off by bottom of group
            final int viewBottom = view.getBottom() + view.getPaddingBottom();
            float textY = view.getTop() + view.getPaddingTop() - 15;
            if (position + 1 < totalItemCount) {
                long nextGroupId = callback.getGroupId(position + 1);
                if (nextGroupId != groupId && viewBottom < textY + lineHeight) {
                    // Next item is different group, align Y with bottom of current group
                    textY = viewBottom - lineHeight;
                }
            }

            // draw text
            c.drawText(textLine, left, textY, textPaint);
        }
    }

    private boolean isFirstInGroup(int position) {
        if (position == 0) {
            return true;
        } else {
            long prevGroupId = callback.getGroupId(position - 1);
            long groupId = callback.getGroupId(position);
            return prevGroupId != groupId;
        }
    }

    private boolean isLastInGroup(int position){
        long nextGroupId = callback.getGroupId(position + 1);
        long groupId = callback.getGroupId(position);
        return nextGroupId != groupId;
    }

    public interface Callback {
        long getGroupId(int position);

        @NonNull
        String getGroupFirstLine(int position);
    }
}
