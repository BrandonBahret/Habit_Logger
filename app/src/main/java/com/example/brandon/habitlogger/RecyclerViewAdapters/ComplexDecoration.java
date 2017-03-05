package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

import com.example.brandon.habitlogger.R;

import static android.support.v4.content.ContextCompat.getColor;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ComplexDecoration extends RecyclerView.ItemDecoration {

    private final Callback callback;
    private final TextPaint textPaint;
    private final Paint linePaint;
    private final int topGap;
    private final int bottomGap;
    private final int lineInset;
    private final float lineWidth;
    private final int textVerticalOffset;
    private Paint.FontMetrics fontMetrics;

    public ComplexDecoration(Context context, @DimenRes int textSizeRes, Callback callback) {
        super();
        this.callback = callback;
        final Resources res = context.getResources();

        int textSize = res.getDimensionPixelSize(textSizeRes);
        lineWidth = res.getDimensionPixelSize(R.dimen.line_width);
        topGap = res.getDimensionPixelSize(R.dimen.indent_pad_top);
        bottomGap = res.getDimensionPixelSize(R.dimen.indent_pad_bottom_of_group);
        lineInset = res.getDimensionPixelSize(R.dimen.line_inset);
        textVerticalOffset = 20;
        int color = getColor(context, R.color.headerTextColor);
        int shadowColor = getColor(context, R.color.shadow_black);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setColor(color);

        textPaint = new TextPaint();
        fontMetrics = new Paint.FontMetrics();

        textPaint.setTextSize(textSize);
        textPaint.setColor(color);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);
        textPaint.getFontMetrics(fontMetrics);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(8f, 0f, 0f, shadowColor);
    }

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
        }
        else {
            outRect.top = 0;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final int totalItemCount = state.getItemCount();
        final int childCount = parent.getChildCount();
        final float width = parent.getWidth();
        final float left = width / 2;

        final float lineHeight = textPaint.getTextSize() + fontMetrics.descent;

        long prevGroupId, groupId = -1;
        for (int i = 0; i < childCount; i++) {
            final View view = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(view);

            prevGroupId = groupId;
            groupId = callback.getGroupId(position);
            if (groupId < 0 || groupId == prevGroupId) continue;

            final String textLine = callback.getGroupFirstLine(position);
            if (textLine.isEmpty()) continue;

            final int viewBottom = view.getBottom() + view.getPaddingBottom();
            float textY = view.getTop() + view.getPaddingTop() - textVerticalOffset;
            if (position + 1 < totalItemCount) {
                long nextGroupId = callback.getGroupId(position + 1);
                if (nextGroupId != groupId && viewBottom < textY + lineHeight) {
                    textY = viewBottom - lineHeight;
                }
            }

            Rect textRect = new Rect();
            textPaint.getTextBounds(textLine, 0, textLine.length() - 1, textRect);

            final float lineY = view.getTop() + view.getPaddingTop() - textVerticalOffset - textRect.height()/2.0f + lineWidth / 4.0f;

            float lineX1 = width / 2 - textRect.width()/2.0f - lineInset;// width - lineInset;
            float lineX2 = width / 2 + textRect.width()/2.0f + lineInset;// width - lineInset;

            c.drawLine(lineInset, lineY, lineX1, lineY, linePaint);
            c.drawLine(lineX2, lineY, width - lineInset, lineY, linePaint);

            c.drawText(textLine, left, textY, textPaint);
        }

    }

    private boolean isFirstInGroup(int position) {
        if (position == 0) {
            return true;
        }
        else {
            long prevGroupId = callback.getGroupId(position - 1);
            long groupId = callback.getGroupId(position);
            return prevGroupId != groupId;
        }
    }

    private boolean isLastInGroup(int position) {
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
