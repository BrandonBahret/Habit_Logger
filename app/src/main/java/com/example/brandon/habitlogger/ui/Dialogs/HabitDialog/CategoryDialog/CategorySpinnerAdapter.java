package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 2/12/2017.
 * Adapter class for category spinner.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class CategorySpinnerAdapter extends BaseAdapter {

    //region (Member attributes)
    private List<HabitCategory> mCategories;
    private LayoutInflater mInflater;
    //endregion

    public static class ViewHolder {
        public TextView title;
        public ImageView color;
        public View itemView;

        public ViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.title);
            color = (ImageView) view.findViewById(R.id.color);
            itemView = view;
        }

        public void bindObject(HabitCategory category){
            title.setText(category.getName());
            color.setColorFilter(category.getColorAsInt());
        }
    }

    public CategorySpinnerAdapter(Context context, List<HabitCategory> categories) {
        mCategories = categories;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        if(row == null)
            row = mInflater.inflate(R.layout.spinner_category_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(row);
        HabitCategory category = getItem(position);
        viewHolder.bindObject(category);

        return row;
    }

    //region Methods responsible for exposing the data set
    public int getItemPosition(HabitCategory category){
        return mCategories.indexOf(category);
    }

    public int getCount() {
        return mCategories.size();
    }

    public List<HabitCategory> getItems() {
        return mCategories;
    }

    public HabitCategory getItem(int position) {
        return mCategories.get(position);
    }

    public long getItemId(int position) {
        return getItem(position).getDatabaseId();
    }

    public void addCategory(HabitCategory category){
        mCategories.add(category);
        notifyDataSetChanged();
    }
    //endregion -- end --

}
