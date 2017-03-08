package com.example.brandon.habitlogger.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Brandon on 3/7/2017.
 */

public class LayoutCheckableTextView extends android.support.v7.widget.AppCompatTextView {

    public LayoutCheckableTextView(Context context) {
        super(context);
    }

    public LayoutCheckableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutCheckableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface OnLayoutListener {
        void onLayoutCreated(TextView view);
    }

    private OnLayoutListener mOnLayoutListener;

    public void setOnLayoutListener(OnLayoutListener listener) {
        mOnLayoutListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mOnLayoutListener != null) {
            mOnLayoutListener.onLayoutCreated(this);
        }
    }

}
