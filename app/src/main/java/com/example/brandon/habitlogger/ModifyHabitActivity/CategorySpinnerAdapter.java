package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 2/12/2017.
 * Adapter class for category spinner.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class CategorySpinnerAdapter extends BaseAdapter {

    private List<HabitCategory> mCategories;
    private LayoutInflater mInflater;

    public CategorySpinnerAdapter(Context context, List<HabitCategory> categories) {
        mCategories = categories;
        mInflater = LayoutInflater.from(context);
    }

    public static class ViewHolder{
        public TextView title;
        public ImageView color;
        public View itemView;

        public ViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.title);
            color = (ImageView) view.findViewById(R.id.color);
            itemView = view;
        }
    }

    //region // Methods responsible for exposing the data set.
    public HabitCategory getItem(int i) {
        return mCategories.get(i);
    }

    public long getItemId(int i) {
        return mCategories.get(i).getDatabaseId();
    }

    public int getItemPosition(HabitCategory category){
        return mCategories.indexOf(category);
    }

    public int getCount() {
        return mCategories.size();
    }

    public void addCategory(HabitCategory category){
        mCategories.add(category);
        notifyDataSetChanged();
    }
    //endregion

    //region // Methods responsible for binding categories to rows

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        HabitCategory category = getItem(position);

        if(row == null) {
            row = mInflater.inflate(R.layout.spinner_category_layout, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder(row);
        viewHolder.title.setText(category.getName());
        viewHolder.color.setColorFilter(category.getColorAsInt());

        return row;
    }

    //endregion

}
