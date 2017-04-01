package com.example.brandon.habitlogger.ModifyHabitDialog;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.databinding.DialogCategorySelectorBinding;

/**
 * Created by Brandon on 2/25/2017.
 * Custom spinner used to display a custom dialog to pick and or create categories.
 */

public class CategorySpinner extends AppCompatSpinner {

    //region (Member attributes)
    private AlertDialog mDialog;
    Context mContext;
    //endregion

    public CategorySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        mContext = getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        DialogCategorySelectorBinding ui = DataBindingUtil.inflate(
                layoutInflater, R.layout.dialog_category_selector,
                null, false
        );

        ui.categoryList.setAdapter((ListAdapter) adapter);
        ui.categoryList.setOnItemClickListener(OnCategoryListItemClick);
        ui.newCategoryButton.setOnClickListener(OnNewCategoryButtonClicked);

        mDialog = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.spinner_prompt_categories))
                .setCancelable(true)
                .setView(ui.getRoot())
                .create();

        super.setAdapter(adapter);
    }

    //region Methods responsible for handling events
    @Override
    public boolean performClick() {
        mDialog.show();
        return false;
    }

    private AdapterView.OnItemClickListener OnCategoryListItemClick =
            new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setSelection(position);
                    mDialog.dismiss();
                }
            };

    private OnClickListener OnNewCategoryButtonClicked =
            new OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewCategoryDialog dialog = new NewCategoryDialog(mContext, OnCategoryDialogFinished);
                    dialog.createBuilder().show();
                }
            };

    private NewCategoryDialog.OnFinishedListener OnCategoryDialogFinished =
            new NewCategoryDialog.OnFinishedListener() {
                @Override
                public void onFinishedWithResult(HabitCategory category) {
                    ((CategorySpinnerAdapter) getAdapter()).addCategory(category);
                }
            };
    //endregion -- end --

}
