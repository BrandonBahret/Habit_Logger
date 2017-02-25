package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 2/24/2017.
 */

public class CategorySelectorDialog extends DialogFragment {

    public static CategorySelectorDialog getInstance(){
        return new CategorySelectorDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getString(R.string.spinner_prompt_categories));
        builder.setCancelable(true);
        builder.setNegativeButton("CANCEL", null);

        List<HabitCategory> categories = new HabitDatabase(getContext()).getCategories();

        ListView categoryList = new ListView(getContext());
        CategorySpinnerAdapter arrayAdapter = new CategorySpinnerAdapter(getContext(), categories);
        categoryList.setAdapter(arrayAdapter);
        builder.setView(categoryList);

        return builder.create();
    }
}
