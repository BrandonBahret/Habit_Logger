package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.databinding.DialogCategorySelectorBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 5/5/2017.
 * Dialog class used to select categories
 */

public class SelectCategoryDialog extends DialogFragment {

    public class DialogState {
        List<HabitCategory> categories;
        int accentColor = 0;

        public void saveState(Bundle outState){
            outState.putParcelableArrayList("categories", (ArrayList<? extends Parcelable>) categories);
            outState.putInt("accentColor", accentColor);
        }

        public void restoreState(Bundle savedInstanceState){
            this.categories = savedInstanceState.getParcelableArrayList("categories");
            this.accentColor = savedInstanceState.getInt("accentColor");
        }
    }

    //region (Member attributes)
    DialogState mDialogState = new DialogState();
    private CategorySpinnerAdapter mAdapter;
    //endregion -- end --

    //region Code responsible for providing a callback to user.
    DialogCallback mCallbackInterface;

    public interface DialogCallback {
        void onCategoryListItemClick(int adapterPosition);

        void onNewCategoryButtonClick();
    }

    public void setCallbackInterface(DialogCallback callbackInterface) {
        mCallbackInterface = callbackInterface;
    }
    //endregion -- end --

    //region Methods responsible for handling the dialog lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            mDialogState.restoreState(savedInstanceState);

        mAdapter = new CategorySpinnerAdapter(getContext(), mDialogState.categories);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final DialogCategorySelectorBinding ui = DataBindingUtil.inflate(
                layoutInflater, R.layout.dialog_category_selector,
                null, false
        );

        ui.categoryList.setAdapter(mAdapter);
        ui.categoryList.setOnItemClickListener(onCategoryListItemClick);
        ui.newCategoryButton.setOnClickListener(onNewCategoryButtonClicked);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.spinner_prompt_categories))
                .setCancelable(true)
                .setView(ui.getRoot())
                .create();

        if (mDialogState.accentColor != 0) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    int color = mDialogState.accentColor;
                    if (MyColorUtils.getLightness(color) > 0.5) {
                        color = MyColorUtils.darkenColorBy(mDialogState.accentColor, 0.2f);
                    }
                    ui.newCategoryButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);
                }
            });
        }

        return dialog;
    }
    //endregion -- end --

    //region Methods responsible for handling events
    private AdapterView.OnItemClickListener onCategoryListItemClick =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mCallbackInterface != null)
                        mCallbackInterface.onCategoryListItemClick(position);
                    dismiss();
                }
            };

    private View.OnClickListener onNewCategoryButtonClicked =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallbackInterface != null)
                        mCallbackInterface.onNewCategoryButtonClick();
                }
            };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDialogState.saveState(outState);
    }
    //endregion -- end --

    //region Setters{}
    public SelectCategoryDialog setAccentColor(int color){
        mDialogState.accentColor = color;
        return this;
    }

    public SelectCategoryDialog setCategories(List<HabitCategory> categories){
        mDialogState.categories = categories;
        return this;
    }

    public void addCategory(HabitCategory category) {
        mAdapter.addCategory(category);
    }
    //endregion -- end --

}
