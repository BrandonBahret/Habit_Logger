package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.brandon.habitlogger.HabitDatabase.HabitCategory;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 2/12/2017.
 * Adapter class for category spinner.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class CategorySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private Context mContext;
    private List<HabitCategory> mCategories;

    public CategorySpinnerAdapter(Context context, List<HabitCategory> categories) {
        mCategories = categories;
        mContext = context;
    }

    public class ViewHolder{
        public TextView title;
        public ImageView color;

        public ViewHolder(View view){
            title = (TextView) view.findViewById(R.id.title);
            color = (ImageView) view.findViewById(R.id.color);
        }
    }

    public int getCount() {
        return mCategories.size();
    }

    public Object getItem(int i) {
        return mCategories.get(i);
    }

    public long getItemId(int i) {
        return mCategories.get(i).getDatabaseId();
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(mContext);
        View row = inflater.inflate(R.layout.spinner_category_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(row);

        HabitCategory category = mCategories.get(position);
        viewHolder.title.setText(category.getName());
        viewHolder.color.setColorFilter(category.getColorAsInt());

        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

}
