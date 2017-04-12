package com.example.brandon.habitlogger.ui.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;

/**
 * Created by Brandon on 3/13/2017.
 * Used for getting user confirmation with AlertDialog.
 */

public class ConfirmationDialog {

    private AlertDialog.Builder mDialogBuilder;
    private Context mContext;
    private String mTitle, mMessage;
    private int mIconRes = -1;
    private Integer mAccentColor = 0;

    DialogInterface.OnClickListener onYesListener;
    DialogInterface.OnClickListener onNoListener;
    DialogInterface.OnCancelListener onCancelListener;
    DialogInterface.OnDismissListener onDismissListener;

    public ConfirmationDialog(Context context) {
        mDialogBuilder = new AlertDialog.Builder(context);
        mContext = context;
    }

    public ConfirmationDialog show() {
        mDialogBuilder
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton("Yes", this.onYesListener)
                .setNegativeButton("No", this.onNoListener)
                .setOnCancelListener(this.onCancelListener)
                .setOnDismissListener(this.onDismissListener);

        if (mIconRes != -1)
            mDialogBuilder.setIcon(mIconRes);

        final AlertDialog confirmDialog = mDialogBuilder.create();

        if (mAccentColor != 0) {
            confirmDialog.setOnShowListener(
                    new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
//                            int color = mAccentColor == null ?
//                                    ContextCompat.getColor(mContext, R.color.textColorContrastBackground) :
//                                    mAccentColor;

                            confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mAccentColor);
                            confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mAccentColor);
                        }
                    }
            );
        }

        confirmDialog.show();

        return this;
    }

    public AlertDialog create() {
        return mDialogBuilder.create();
    }

    //region Setters {}
    public ConfirmationDialog setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public ConfirmationDialog setMessage(String message) {
        this.mMessage = message;
        return this;
    }

    public ConfirmationDialog setIcon(@DrawableRes int iconRes) {
        this.mIconRes = iconRes;
        return this;
    }
    //endregion

    //region Set listeners
    public ConfirmationDialog setOnYesClickListener(DialogInterface.OnClickListener listener) {
        this.onYesListener = listener;
        return this;
    }

    public ConfirmationDialog setOnNoClickListener(DialogInterface.OnClickListener listener) {
        this.onNoListener = listener;
        return this;
    }

    public ConfirmationDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        this.onCancelListener = listener;
        return this;
    }

    public ConfirmationDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.onDismissListener = listener;
        return this;
    }

    public ConfirmationDialog setAccentColor(int color) {
        mAccentColor = color;
        return this;
    }
    //endregion
}