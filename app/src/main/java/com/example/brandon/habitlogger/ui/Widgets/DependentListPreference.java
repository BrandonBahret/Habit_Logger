package com.example.brandon.habitlogger.ui.Widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.example.brandon.habitlogger.R;

/**
 * Created by Brandon on 4/8/2017.
 * Sub-class of ListPreference to make other preferences depend on a ListPreference selection
 * http://stackoverflow.com/questions/3969807/listpreference-dependency
 */

public class DependentListPreference extends ListPreference {

    private String dependentValue = "";

    public DependentListPreference(Context context) {
        this(context, null);
    }

    public DependentListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DependentListPreference);
            dependentValue = a.getString(R.styleable.DependentListPreference_dependentValue);
            a.recycle();
        }
    }

    @Override
    public void setValue(String value) {
        String oldValue = getValue();
        super.setValue(value);
        if (!value.equals(oldValue)) {
            notifyDependencyChange(shouldDisableDependents());
        }
    }

    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisableDependents = super.shouldDisableDependents();
        String value = getValue();
        return shouldDisableDependents || value == null || !value.equals(dependentValue);
    }

}