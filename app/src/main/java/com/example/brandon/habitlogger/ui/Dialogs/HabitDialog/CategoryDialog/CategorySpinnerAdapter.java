package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.ui.Widgets.MySpinner;

import java.util.List;

/**
 * Created by Brandon on 2/12/2017.
 * Adapter class for category spinner.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class CategorySpinnerAdapter extends BaseAdapter {

    public interface CategoryManipulationCallback {
        void removeCategory(final HabitCategory category);

        void editCategory(final HabitCategory category);
    }

    //region (Member attributes)
    private List<HabitCategory> mCategories;
    private LayoutInflater mInflater;

    CategoryManipulationCallback mCallback;

    public void setCategoryManipulationCallback(CategoryManipulationCallback callback) {
        this.mCallback = callback;
    }
    //endregion

    public class ViewHolder {
        public TextView title;
        public ImageView color;
        public ImageButton delete, edit;
        public View itemView;

        public ViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.title);
            color = (ImageView) view.findViewById(R.id.color);
            delete = (ImageButton) view.findViewById(R.id.delete_category);
            edit = (ImageButton) view.findViewById(R.id.edit_category);
            itemView = view;
        }

        public void bindObject(final HabitCategory category) {
            title.setText(category.getName());
            color.setColorFilter(category.getColorAsInt());

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.editCategory(category);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.removeCategory(category);
                }
            });
        }

        public void hideEditButtons() {
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        }

        public void showEditButtons() {
            delete.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
        }
    }


    public CategorySpinnerAdapter(Context context, List<HabitCategory> categories) {
        mCategories = categories;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View row, ViewGroup parent) {
        if (row == null)
            row = mInflater.inflate(R.layout.spinner_category_edit_layout, parent, false);


        ViewHolder viewHolder = new ViewHolder(row);

        if(parent instanceof MySpinner || position == 0){
            viewHolder.hideEditButtons();
        }
        else {
            viewHolder.showEditButtons();
        }

        HabitCategory category = getItem(position);
        viewHolder.bindObject(category);

        return row;
    }

    //region Methods responsible for exposing the data set
    public int getItemPosition(HabitCategory category) {
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

    public void addCategory(HabitCategory category) {
        mCategories.add(category);
        notifyDataSetChanged();
    }

    public void removeCategory(HabitCategory category) {
        mCategories.remove(category);
        notifyDataSetChanged();
    }

    public void updateCategory(HabitCategory initCategory, HabitCategory category) {
        int prevIndex = mCategories.indexOf(initCategory);
        mCategories.set(prevIndex, category);
        notifyDataSetChanged();
    }
    //endregion -- end --

}
