package com.example.brandon.habitlogger.ui.Widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Brandon on 2/25/2017.
 * Custom spinner used to display a custom dialog to pick and or create categories.
 */

public class MySpinner extends AppCompatSpinner {

    View.OnClickListener mClickListener;

    public MySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setClickListener(View.OnClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public boolean performClick() {
        if (mClickListener != null)
            mClickListener.onClick(this);
        return false;
    }

}
