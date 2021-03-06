package com.example.brandon.habitlogger.ui.Widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Brandon on 3/7/2017.
 * A simple extension of TextView that can notify when it's done it's layout work
 */

public class LayoutCheckableTextView extends android.support.v7.widget.AppCompatTextView {

    //region Code responsible for providing an interface
    private OnLayoutListener mOnLayoutListener;

    public interface OnLayoutListener {
        void onLayoutCreated(TextView view);
    }

    public void setOnLayoutListener(OnLayoutListener listener) {
        mOnLayoutListener = listener;
    }
    //endregion

    //region Constructors {}
    public LayoutCheckableTextView(Context context) {
        super(context);
    }

    public LayoutCheckableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutCheckableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    //endregion

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mOnLayoutListener != null)
            mOnLayoutListener.onLayoutCreated(this);
    }
}
