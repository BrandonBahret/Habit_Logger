package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.databinding.DialogNewCategoryBinding;

/**
 * Created by Brandon on 2/26/2017.
 *
 */

public class NewCategoryDialog {

    //region (Member attributes)
    private Context mContext;
    private DialogNewCategoryBinding ui;
    //endregion

    //region Code responsible for providing an interface
    public interface OnFinishedListener {
        void onFinishedWithResult(HabitCategory category);
    }

    private OnFinishedListener mOnFinishedListener;
    //endregion

    public NewCategoryDialog(Context context, OnFinishedListener listener) {
        this.mContext = context;
        this.mOnFinishedListener = listener;
    }

    //region Methods responsible for constructing the dialog builder
    public AlertDialog.Builder createBuilder() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ui = DataBindingUtil.inflate(inflater, R.layout.dialog_new_category, null, false);

        ui.categoryColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                setColorPreview(s.toString());
            }
        });

        return new AlertDialog.Builder(mContext)
                .setCancelable(true)
                .setView(ui.getRoot())
                .setTitle("New Category")
                .setPositiveButton("Create", OnPositiveButtonClicked);
    }

    private void setColorPreview(String color) {
        try {
            int convertColor = Color.parseColor(color);
            ui.colorPreview.setColorFilter(convertColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DialogInterface.OnClickListener OnPositiveButtonClicked =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOnFinishedListener.onFinishedWithResult(getCategory());
                }
            };
    //endregion

    public HabitCategory getCategory() {
        String color = ui.categoryColor.getText().toString();
        String name = ui.categoryName.getText().toString();
        return new HabitCategory(color, name);
    }

}
