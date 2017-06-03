package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.databinding.DialogNewCategoryBinding;
import com.example.brandon.habitlogger.ui.Events.IStateContainer;
import com.example.brandon.habitlogger.ui.Widgets.TextWatcherAdapter;

import java.io.Serializable;

/**
 * Created by Brandon on 5/16/2017.
 * A class to modify categories in the database.
 */

public class CategoryDialog extends DialogFragment {

    private class DialogState implements IStateContainer {
        HabitCategory category;
        HabitCategory initCategory;
        int accentColor = 0;
        String title = "Category Dialog";
        String positiveText = "Confirm", negativeText = "Cancel";

        @Override
        public void saveState(@NonNull Bundle outState) {
            outState.putSerializable("category", category);
            outState.putSerializable("initCategory", initCategory);
            outState.putString("title", title);
            outState.putString("positiveText", positiveText);
            outState.putString("negativeText", negativeText);
            outState.putInt("accentColor", accentColor);
        }

        @Override
        public void restoreState(@NonNull Bundle savedInstanceState) {
            Serializable serializedCategory = savedInstanceState.getSerializable("category");
            if (serializedCategory != null) category = (HabitCategory) serializedCategory;

            serializedCategory = savedInstanceState.getSerializable("initCategory");
            if (serializedCategory != null) initCategory = (HabitCategory) serializedCategory;

            title = savedInstanceState.getString("title");
            positiveText = savedInstanceState.getString("positiveText");
            negativeText = savedInstanceState.getString("negativeText");
            accentColor = savedInstanceState.getInt("accentColor");
        }
    }

    public interface DialogResult {
        void onResult(HabitCategory initCategory, HabitCategory category);
    }

    //region (Member attributes)
    private DialogState mDialogState = new DialogState();
    private DialogNewCategoryBinding ui;

    DialogInterface.OnClickListener onPositiveClicked;
    DialogInterface.OnClickListener onNegativeClicked;
    //endregion -- end --

    //region Methods responsible for handling the dialog lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            mDialogState.restoreState(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        ui = DataBindingUtil.inflate(inflater, R.layout.dialog_new_category, null, false);

        ui.categoryColor.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable text) {
                setColorPreview(text.toString());
            }
        });

        if(mDialogState.initCategory != null){
            setColorPreview(mDialogState.initCategory.getColor());
            ui.categoryName.setText(mDialogState.initCategory.getName());
            ui.categoryColor.setText(mDialogState.initCategory.getColor());
        }

        final AlertDialog categoryDialog = new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setView(ui.getRoot())
                .setTitle(mDialogState.title)
                .setPositiveButton(mDialogState.positiveText, onPositiveClicked)
                .setNegativeButton(mDialogState.negativeText, onNegativeClicked)
                .create();

        if (mDialogState.accentColor != 0) {
            categoryDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    categoryDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                            .setTextColor(mDialogState.accentColor);

                    categoryDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .setTextColor(mDialogState.accentColor);
                }
            });
        }

        return categoryDialog;
    }
    //endregion -- end --

    //region Methods responsible for handling events
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDialogState.saveState(outState);
    }
    //endregion -- end --

    //region Setters {}
    public CategoryDialog setTitle(String title) {
        mDialogState.title = title;
        return this;
    }

    public CategoryDialog setCategory(HabitCategory category) {
        mDialogState.initCategory = category;
        mDialogState.category = category;
        return this;
    }

    public CategoryDialog setAccentColor(int color) {
        mDialogState.accentColor = color;
        return this;
    }

    public void setColorPreview(String color) {
        try {
            int parsedColor = MyColorUtils.parseColor(color);
            ui.colorPreview.setColorFilter(parsedColor);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public CategoryDialog setPositiveButton(String title, final DialogResult listener) {
        if (title != null)
            mDialogState.positiveText = title;

        onPositiveClicked = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null)
                    listener.onResult(mDialogState.initCategory, getCategory());
            }
        };
        return this;
    }

    public CategoryDialog setNegativeButton(String title, final DialogResult listener) {
        if (title != null)
            mDialogState.negativeText = title;

        onNegativeClicked = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null)
                    listener.onResult(mDialogState.initCategory, getCategory());
            }
        };
        return this;
    }
    //endregion -- end --

    //region Getters {}
    private HabitCategory getCategory() {
        HabitCategory categoryResult = new HabitCategory();

        String color = ui.categoryColor.getText().toString();
        String name = ui.categoryName.getText().toString();

        if (mDialogState.initCategory != null)
            HabitCategory.copy(categoryResult, mDialogState.category);

        categoryResult.setColor(color);
        categoryResult.setName(name);

        return categoryResult;
    }
    //endregion -- end --

}
