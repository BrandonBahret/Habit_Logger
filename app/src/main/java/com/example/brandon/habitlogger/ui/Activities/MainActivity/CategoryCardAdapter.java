package com.example.brandon.habitlogger.ui.Activities.MainActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
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

    //region (Member attributes)
    private HabitViewAdapter.MenuItemClickListener mMenuItemClickListener;
    private HabitViewAdapter.ButtonClickCallback mButtonClickCallback;
    //endregion

    public CategoryCardAdapter(
            List<? extends ExpandableGroup> groups,
            HabitViewAdapter.MenuItemClickListener menuItemClickListener,
            HabitViewAdapter.ButtonClickCallback buttonClickCallback) {
        super(groups);
        mMenuItemClickListener = menuItemClickListener;
        mButtonClickCallback = buttonClickCallback;
    }

    //region Methods responsible for creating rootView holders
    @Override
    public CategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_card, parent, false);
        setMargins(view, 0, 20, 0, 26);

        return new CategoryViewHolder(view, new CategoryViewHolder.Callback() {
            @Override
            public void beforeExpand(String title) {
                for (ExpandableGroup group : getGroups()) {
                    if (isGroupExpanded(group) && !group.getTitle().equals(title))
                        toggleGroup(group);
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

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
    //endregion -- end --

    //region Methods responsible for binding objects
    @Override
    public void onBindGroupViewHolder(CategoryViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {

        CategoryDataSample container = (CategoryDataSample) group;

        holder.setTitle(group);
        holder.setColor(container.getCategory().getColorAsInt());
        holder.setNumberOfEntries(group.getItemCount());
    }

    @Override
    public void onBindChildViewHolder(HabitViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {
        Habit item = (Habit) group.getItems().get(childIndex);
        holder.bindItem(item, mMenuItemClickListener, mButtonClickCallback);
    }
    //endregion -- end --
}
