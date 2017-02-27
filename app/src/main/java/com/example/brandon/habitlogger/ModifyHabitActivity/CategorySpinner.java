package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.databinding.DialogCategorySelectorBinding;

/**
 * Created by Brandon on 2/25/2017.
 */

public class CategorySpinner extends Spinner implements AdapterView.OnItemClickListener, View.OnClickListener, NewCategoryDialogFactory.OnFinishedListener {
    private AlertDialog dialog;
    private DialogCategorySelectorBinding ui;

    public CategorySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        Context context = getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        ui = DataBindingUtil.inflate
                (layoutInflater, R.layout.dialog_category_selector, null, false);

        ui.categoryList.setAdapter((ListAdapter) getAdapter());
        ui.categoryList.setOnItemClickListener(this);
        ui.newCategoryButton.setOnClickListener(this);

        dialog = new AlertDialog.Builder(getContext())
                .setTitle(context.getString(R.string.spinner_prompt_categories))
                .setCancelable(true)
                .setView(ui.getRoot())
                .create();
        dialog.show();

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setSelection(position);
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == ui.newCategoryButton.getId()){
            NewCategoryDialogFactory dialog = new NewCategoryDialogFactory(getContext(), this);
            dialog.createBuilder().show();
        }
    }

    @Override
    public void onFinishedWithResult(HabitCategory category) {
        ((CategorySpinnerAdapter)getAdapter()).addCategory(category);
    }
}
