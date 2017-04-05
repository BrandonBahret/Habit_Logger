package com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

import com.example.brandon.habitlogger.R;

import static android.support.v4.content.ContextCompat.getColor;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GroupDecoration extends RecyclerView.ItemDecoration {

    //region (Member attributes)
    private int mTopGap;
    private int mBottomGap;
    private int mTextVerticalOffset;
    private int mTextSize;

    private int mTextColor;
    private int mBackdropColor;

    private TextPaint mTextPaint;
    private Paint.FontMetrics mFontMetrics;
    //endregion

    //region Code responsible for providing an interface
    private final Callback mCallback;
    private Paint mBackdropPaint;
    private float mBackdropHeight;

    public interface Callback {
        long getGroupId(int position);

        String getGroupFirstLine(int position);
    }
    //endregion -- end --

    //region Constructor {}
    public GroupDecoration(Context context, @DimenRes int textSizeRes, Callback callback) {
        mCallback = callback;

        fetchDimensions(context, textSizeRes);

        fetchColors(context);

        createPaints();

        makeMeasurements();
    }

    private void fetchDimensions(Context context, @DimenRes int textSizeRes) {
        final Resources res = context.getResources();
        mTextSize = res.getDimensionPixelSize(textSizeRes);
        mTopGap = res.getDimensionPixelSize(R.dimen.indent_pad_top);
        mBottomGap = res.getDimensionPixelSize(R.dimen.indent_pad_bottom_of_group);
        mTextVerticalOffset = res.getDimensionPixelSize(R.dimen.line_vertical_offset);
    }

    private void fetchColors(Context context) {
        mBackdropColor = getColor(context, R.color.itemDecorationBackground);
        mTextColor = getColor(context, R.color.itemDecorationText);
    }

    private void createPaints() {
        mFontMetrics = new Paint.FontMetrics();
        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setAntiAlias(true);
        mTextPaint.getFontMetrics(mFontMetrics);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mBackdropPaint = new Paint();
        mBackdropPaint.setColor(mBackdropColor);
    }

    private void makeMeasurements() {
        mBackdropHeight = mTextPaint.getTextSize() + mFontMetrics.descent;
    }

    //endregion -- end --

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int childPosition = parent.getChildAdapterPosition(view);
        if (mCallback.getGroupId(childPosition) >= 0) {
            outRect.bottom = isLastInGroup(childPosition) ? mBottomGap : 0;
            outRect.top = isFirstInGroup(childPosition) ? mTopGap : 0;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final int finalIndex = state.getItemCount() - 1;
        final int childCount = parent.getChildCount();
        final float width = parent.getWidth();
        final float textX = width / 2;

        long prevGroupId, groupId = -1;
        for (int i = 0; i < childCount; i++) {
            final View view = parent.getChildAt(i);
            final int childPosition = parent.getChildAdapterPosition(view);

            // If this is the same group then skip
            prevGroupId = groupId;
            groupId = mCallback.getGroupId(childPosition);
            boolean isTheSameGroup = groupId == prevGroupId;
            if (groupId < 0 || isTheSameGroup) continue;

            // If there isn't any text to draw then skip
            final String textLine = mCallback.getGroupFirstLine(childPosition);
            if (textLine == null || textLine.isEmpty()) continue;

            float textY = view.getTop() + view.getPaddingTop() - mTextVerticalOffset;

            // Draw the backdrop
            c.drawRect(0, textY - mBackdropHeight, width, textY + (mBackdropHeight / 3), mBackdropPaint);

            // Draw the section text
            c.drawText(textLine, textX, textY, mTextPaint);
        }
    }

    private boolean isFirstInGroup(int position) {
        if (position == 0) return true;
        else {
            long prevGroupId = mCallback.getGroupId(position - 1);
            long groupId = mCallback.getGroupId(position);
            return prevGroupId != groupId;
        }
    }

    private boolean isLastInGroup(int position) {
        long nextGroupId = mCallback.getGroupId(position + 1);
        long groupId = mCallback.getGroupId(position);
        return nextGroupId != groupId;
    }
}
