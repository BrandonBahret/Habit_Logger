package com.example.brandon.habitlogger.common;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Brandon on 3/13/2017.
 * Used for getting user confirmation with AlertDialog.
 */

public class ConfirmationDialog {

    private AlertDialog.Builder mDialogBuilder;
    private String mTitle, mMessage;

    DialogInterface.OnClickListener onYesListener;
    DialogInterface.OnClickListener onNoListener;
    DialogInterface.OnCancelListener onCancelListener;
    DialogInterface.OnDismissListener onDismissListener;

    public ConfirmationDialog(Context context) {
        mDialogBuilder = new AlertDialog.Builder(context);
    }

    public ConfirmationDialog show() {
        mDialogBuilder
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton("Yes", this.onYesListener)
                .setNegativeButton("No", this.onNoListener)
                .setOnCancelListener(this.onCancelListener)
                .setOnDismissListener(this.onDismissListener)
                .show();

        return this;
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
    //endregion
    //endregion
}