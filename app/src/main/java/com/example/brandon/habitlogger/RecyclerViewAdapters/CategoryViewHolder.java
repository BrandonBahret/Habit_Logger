package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

/**
 * Created by Brandon on 2/6/2017.
 * Category card view holder for CategoryCardAdapter adapter
 */

public class CategoryViewHolder extends GroupViewHolder {
    public CardView view;
    public TextView title, numberOfHabits;

    Callback callback;

    public interface Callback{
        void beforeExpand(String title);
    }

    public CategoryViewHolder(View view, Callback beforeExpand) {
        super(view);
        this.view = (CardView)view;
        this.title = (TextView) view.findViewById(R.id.category_title);
        this.numberOfHabits = (TextView) view.findViewById(R.id.number_of_habits);
        this.callback = beforeExpand;
    }

    @Override
    public void onClick(View v) {
        callback.beforeExpand(title.getText().toString());
        super.onClick(v);
    }

    public void setTitle(ExpandableGroup group) {
        title.setText(group.getTitle());
    }

    public void setColor(int color) {
        view.setCardBackgroundColor(color);
    }

    public void setNumberOfEntries(int itemCount) {
        numberOfHabits.setText(String.valueOf(itemCount));
    }
}