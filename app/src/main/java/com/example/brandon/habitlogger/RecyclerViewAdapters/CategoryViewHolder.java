package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

/**
 * Created by Brandon on 2/6/2017.
 * Category card mRootView holder for CategoryCardAdapter mAdapter
 */

public class CategoryViewHolder extends GroupViewHolder {

    //region (Member attributes)
    private CardView mRootView;
    private TextView mTitle, mNumberOfHabits;
    //endregion

    //region Code responsible for providing an interface
    private Callback mOnBeforeGroupExpands;

    public interface Callback{
        void beforeExpand(String title);
    }
    //endregion -- end --

    public CategoryViewHolder(View view, Callback onBeforeGroupExpands) {
        super(view);
        mRootView = (CardView)view;
        mTitle = (TextView) view.findViewById(R.id.category_title);
        mNumberOfHabits = (TextView) view.findViewById(R.id.number_of_habits);
        mOnBeforeGroupExpands = onBeforeGroupExpands;
    }

    @Override
    public void onClick(View v) {
        mOnBeforeGroupExpands.beforeExpand(mTitle.getText().toString());
        super.onClick(v);
    }

    //region Setters {}
    public void setTitle(ExpandableGroup group) {
        mTitle.setText(group.getTitle());
    }

    public void setColor(int color) {
        mRootView.setCardBackgroundColor(color);
    }

    public void setNumberOfEntries(int itemCount) {
        mNumberOfHabits.setText(String.valueOf(itemCount));
    }
    //endregion

}