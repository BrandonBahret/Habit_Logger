package com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog;

import android.content.Context;
import android.os.Bundle;

import com.example.brandon.habitlogger.data.DataModels.SessionEntry;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NewEntryForm extends EntryFormDialogBase {

    //region Code responsible for providing an interface
    protected OnFinishedListener mOnFinishedListener;

    public interface OnFinishedListener {
        void onNewEntryCreated(SessionEntry newEntry);
    }
    //endregion

    //region Methods responsible for getting instances of the dialog
    public static NewEntryForm newInstance() {
        return new NewEntryForm();
    }

    public static NewEntryForm newInstance(int accentColor) {
        NewEntryForm form = new NewEntryForm();

        Bundle args = new Bundle();
        args.putInt(EntryFormDialogBase.KEY_COLOR, accentColor);
        form.setArguments(args);

        return form;
    }
    //endregion -- end --

    //region Methods responsible for handling the dialog lifecycle

    //region Methods to help construct the dialog
    @Override
    String getTitle() {
        return "New Entry";
    }

    @Override
    String getPositiveButtonText() {
        return "Add Entry";
    }

    @Override
    String getNegativeButtonText() {
        return null;
    }
    //endregion -- end --

    //region (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFinishedListener) {
            mOnFinishedListener = (OnFinishedListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFinishedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnFinishedListener = null;
    }
    //endregion -- end --

    //endregion -- end --

    @Override
    public void onNegativeClicked(SessionEntry removeEntry) {}

    @Override
    public void onPositiveClicked(SessionEntry newEntry) {
        if (mOnFinishedListener != null)
            mOnFinishedListener.onNewEntryCreated(newEntry);
    }

}
