package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.databinding.DialogNewCategoryBinding;

/**
 * Created by Brandon on 2/26/2017.
 */

public class NewCategoryDialogFactory implements DialogInterface.OnClickListener {

    private OnFinishedListener onFinishedListener;
    private Context context;
    private DialogNewCategoryBinding ui;

    public interface OnFinishedListener {
        void onFinishedWithResult(HabitCategory category);
    }

    public NewCategoryDialogFactory(Context context, OnFinishedListener listener) {
        this.context = context;
        this.onFinishedListener = listener;
    }

    public AlertDialog.Builder createBuilder() {
        LayoutInflater inflater = LayoutInflater.from(context);
        ui = DataBindingUtil.inflate(inflater, R.layout.dialog_new_category, null, false);

        ui.categoryColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setColorPreview(s.toString());
            }
        });

        return new AlertDialog.Builder(context)
                .setCancelable(true)
                .setView(ui.getRoot())
                .setTitle("New Category")
                .setPositiveButton("Create", this);
    }

    private void setColorPreview(String color) {
        try {
            int convertColor = Color.parseColor(color);
            ui.colorPreview.setColorFilter(convertColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        onFinishedListener.onFinishedWithResult(getCategory());
    }

    public HabitCategory getCategory() {
        String color = ui.categoryColor.getText().toString();
        String name = ui.categoryName.getText().toString();
        return new HabitCategory(color, name);
    }
}
