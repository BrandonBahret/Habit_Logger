package com.example.brandon.habitlogger.RecyclerViewAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.CategoryDataSample;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by Brandon on 2/6/2017.
 * RecyclerView adapter for category cards.
 */

public class CategoryCardAdapter extends ExpandableRecyclerViewAdapter<CategoryViewHolder, HabitViewHolder> {

    private HabitViewAdapter.MenuItemClickListener menuItemClickListener;
    private HabitViewAdapter.ButtonClickListener buttonClickListener;

    public CategoryCardAdapter(List<? extends ExpandableGroup> groups,
                               HabitViewAdapter.MenuItemClickListener menuItemClickListener,
                               HabitViewAdapter.ButtonClickListener buttonClickListener) {
        super(groups);

        this.menuItemClickListener = menuItemClickListener;
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    public CategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_card, parent, false);
        setMargins(view, 0, 20, 0, 26);

        return new CategoryViewHolder(view, new CategoryViewHolder.Callback() {
            @Override
            public void beforeExpand(String title) {
                for(ExpandableGroup group:getGroups()){
                    if(isGroupExpanded(group) && !group.getTitle().equals(title)){
                        toggleGroup(group);
                    }
                }
            }
        });
    }

    @Override
    public HabitViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_card, parent, false);
        setMargins(itemView, 26, 8, 26, 16);

        return new HabitViewHolder(itemView);
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public void onBindChildViewHolder(HabitViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {
        Habit item = (Habit)group.getItems().get(childIndex);
        holder.bindItem(item, menuItemClickListener, buttonClickListener);
    }

    @Override
    public void onBindGroupViewHolder(CategoryViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {

        CategoryDataSample container = (CategoryDataSample) group;

        holder.setTitle(group);
        holder.setColor(container.getCategory().getColorAsInt());
        holder.setNumberOfEntries(group.getItemCount());
    }
}
