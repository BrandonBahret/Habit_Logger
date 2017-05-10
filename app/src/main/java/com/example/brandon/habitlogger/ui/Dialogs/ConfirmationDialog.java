package com.example.brandon.habitlogger.ui.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.io.Serializable;

/**
 * Created by Brandon on 5/9/2017.
 * Used for getting user confirmation with AlertDialog.
 */

public class ConfirmationDialog extends DialogFragment {

    //region (Member attributes)

    private class DialogState implements Serializable {
        public String title, message;
        public Integer accentColor = 0;
        public int iconRes = -1;
        public @StringRes Integer titleId = null;
    }

    DialogInterface.OnClickListener onYesListener;
    DialogInterface.OnClickListener onNoListener;
    DialogInterface.OnCancelListener onCancelListener;
    DialogInterface.OnDismissListener onDismissListener;

    DialogState mDialogState = new DialogState();

    //endregion -- end --

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("DIALOG_STATE", mDialogState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mDialogState = (DialogState) savedInstanceState.getSerializable("DIALOG_STATE");

        if(mDialogState != null && mDialogState.title == null && mDialogState.titleId != null)
            mDialogState.title = getContext().getString(mDialogState.titleId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(mDialogState.title)
                .setMessage(mDialogState.message)
                .setPositiveButton("Yes", this.onYesListener)
                .setNegativeButton("No", this.onNoListener)
                .setOnCancelListener(this.onCancelListener)
                .setOnDismissListener(this.onDismissListener);

        if (mDialogState.iconRes != -1)
            builder.setIcon(mDialogState.iconRes);

        final AlertDialog confirmDialog = builder.create();

        if (mDialogState.accentColor != 0) {
            confirmDialog.setOnShowListener(
                    new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(mDialogState.accentColor);

                            confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(mDialogState.accentColor);
                        }
                    }
            );
        }

        return confirmDialog;
    }

    //region Setters {}
    public ConfirmationDialog setTitle(String title) {
        mDialogState.title = title;
        return this;
    }

    public ConfirmationDialog setTitle(@StringRes int textId) {
        mDialogState.titleId = textId;
        return this;
    }

    public ConfirmationDialog setMessage(String message) {
        mDialogState.message = message;
        return this;
    }

    public ConfirmationDialog setIcon(@DrawableRes int iconRes) {
        mDialogState.iconRes = iconRes;
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
        mDialogState.accentColor = color;
        return this;
    }
    //endregion

}
