package com.example.brandon.habitlogger.common;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;

import com.example.brandon.habitlogger.R;

/**
 * Created by Brandon on 3/13/2017.
 */

public class AskForConfirmationDialog {

    Context context;
    String title = "Confirm action?", message = " ";
    boolean doShowIcon = false;

    AlertDialog dialog = null;
    DialogInterface.OnClickListener onYesListener = null;
    DialogInterface.OnClickListener onNoListener = null;
    DialogInterface.OnCancelListener onCancelListener = null;
    DialogInterface.OnDismissListener onDismissListener = null;

    public AskForConfirmationDialog(Context context) {
        this.context = context;
    }

    public AskForConfirmationDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public AskForConfirmationDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public AskForConfirmationDialog setDoShowIcon(boolean state) {
        this.doShowIcon = state;
        return this;
    }

    public AskForConfirmationDialog setOnYesClickListener(DialogInterface.OnClickListener listener) {
        this.onYesListener = listener;
        return this;
    }

    public AskForConfirmationDialog setOnNoClickListener(DialogInterface.OnClickListener listener) {
        this.onNoListener = listener;
        return this;
    }

    public AskForConfirmationDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        this.onCancelListener = listener;
        return this;
    }

    public AskForConfirmationDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.onDismissListener = listener;
        return this;
    }

    @DrawableRes
    private int getIconRes() {
        boolean nightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        return nightMode ? R.drawable.ic_warning_white_24px :
                R.drawable.ic_warning_black_24dp;
    }

    public AskForConfirmationDialog show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", this.onYesListener)
                .setNegativeButton("No", this.onNoListener)
                .setOnCancelListener(this.onCancelListener)
                .setOnDismissListener(this.onDismissListener);

        if(doShowIcon)
            builder.setIcon(getIconRes());

        dialog = builder.show();

        return this;
    }
}
