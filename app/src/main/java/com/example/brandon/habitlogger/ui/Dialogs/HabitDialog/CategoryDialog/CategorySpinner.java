package com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.CategoryDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyColorUtils;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.HabitCategory;
import com.example.brandon.habitlogger.databinding.DialogCategorySelectorBinding;

/**
 * Created by Brandon on 2/25/2017.
 * Custom spinner used to display a custom dialog to pick and or create categories.
 */

public class CategorySpinner extends AppCompatSpinner {

    //region (Member attributes)
    private AlertDialog mDialog;
    Context mContext;

    private Integer mAccentColor = 0;
    //endregion

    public CategorySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        mContext = getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        final DialogCategorySelectorBinding ui = DataBindingUtil.inflate(
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

        if(mAccentColor != 0) {
            mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    int color = mAccentColor;
                    if(MyColorUtils.getLightness(color) > 0.5){
                        color = MyColorUtils.darkenColorBy(mAccentColor, 0.2f);
                    }
                    ui.newCategoryButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);
                }
            });
        }

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
                    final AlertDialog categoryDialog = dialog.createBuilder().create();
                    categoryDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            int color = mAccentColor == 0 ? ContextCompat.getColor(getContext(), R.color.colorAccent) : mAccentColor;
                            categoryDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                        }
                    });
                    categoryDialog.show();
                }
            };

    private NewCategoryDialog.OnFinishedListener OnCategoryDialogFinished =
            new NewCategoryDialog.OnFinishedListener() {
                @Override
                public void onFinishedWithResult(HabitCategory category) {
                    ((CategorySpinnerAdapter) getAdapter()).addCategory(category);
                }
            };

    public void setAccentColor(Integer accentColor) {
        mAccentColor = accentColor;
    }
    //endregion -- end --

}
