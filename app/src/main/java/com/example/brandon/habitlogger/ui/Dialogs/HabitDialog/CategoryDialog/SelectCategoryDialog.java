package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
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
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.databinding.DialogCategorySelectorBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 5/5/2017.
 * Dialog class used to select categories
 */

public class SelectCategoryDialog extends DialogFragment {

    //region (Member attributes)
    Context mContext;
    DialogCategorySelectorBinding ui;
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

    //region Methods responsible for creating new instances of the dialog
    public static SelectCategoryDialog newInstance(List<HabitCategory> categories) {
        SelectCategoryDialog dialog = new SelectCategoryDialog();

        Bundle args = new Bundle();
        args.putParcelableArrayList("categories", (ArrayList<? extends Parcelable>) categories);
        dialog.setArguments(args);

        return dialog;
    }
    //endregion -- end --

    //region Methods responsible for handling the dialog lifecycle

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<HabitCategory> categories = getArguments().getParcelableArrayList("categories");
        mAdapter = new CategorySpinnerAdapter(getContext(), categories);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        final DialogCategorySelectorBinding ui = DataBindingUtil.inflate(
                layoutInflater, R.layout.dialog_category_selector,
                null, false
        );

        ui.categoryList.setAdapter(mAdapter);
        ui.categoryList.setOnItemClickListener(OnCategoryListItemClick);
        ui.newCategoryButton.setOnClickListener(OnNewCategoryButtonClicked);

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.spinner_prompt_categories))
                .setCancelable(true)
                .setView(ui.getRoot())
                .create();

//        if (mAccentColor != 0) {
//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    int color = mAccentColor;
//                    if (MyColorUtils.getLightness(color) > 0.5) {
//                        color = MyColorUtils.darkenColorBy(mAccentColor, 0.2f);
//                    }
//                    ui.newCategoryButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);
//                }
//            });
//        }

        return dialog;
    }
    //endregion -- end --

    //region Methods responsible for handling events
    private AdapterView.OnItemClickListener OnCategoryListItemClick =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mCallbackInterface != null)
                        mCallbackInterface.onCategoryListItemClick(position);
                    dismiss();
                }
            };

    private View.OnClickListener OnNewCategoryButtonClicked =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallbackInterface != null) mCallbackInterface.onNewCategoryButtonClick();
                }
            };
    //endregion -- end --

}
