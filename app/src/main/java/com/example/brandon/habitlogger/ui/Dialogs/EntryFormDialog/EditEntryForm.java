package com.example.brandon.habitlogger.ui.Dialogs.EntryFormDialog;

import android.content.Context;
import android.os.Bundle;

import com.example.brandon.habitlogger.data.DataModels.SessionEntry;

/**
 * Created by Brandon on 3/29/2017.
 * Class for editing entries.
 */

public class EditEntryForm extends EntryFormDialogBase {

    //region Code responsible for providing an interface
    protected OnFinishedListener mOnFinishedListener;

    public interface OnFinishedListener {
        void onEditEntryUpdateEntry(SessionEntry newEntry);

        void onEditEntryDeleteEntry(SessionEntry removeEntry);
    }
    //endregion

    //region Methods responsible for getting instances of the dialog
    public static EditEntryForm newInstance(SessionEntry entry) {
        EditEntryForm form = new EditEntryForm();

        Bundle args = new Bundle();
        args.putSerializable(EntryFormDialogBase.KEY_ENTRY, entry);
        form.setArguments(args);

        return form;
    }

    public static EditEntryForm newInstance(SessionEntry entry, int accentColor) {
        EditEntryForm form = new EditEntryForm();

        Bundle args = new Bundle();
        args.putSerializable(EntryFormDialogBase.KEY_ENTRY, entry);
        args.putInt(EntryFormDialogBase.KEY_COLOR, accentColor);
        form.setArguments(args);

        return form;
    }
    //endregion -- end --

    //region Methods responsible for handling the dialog lifecycle

    //region Methods to help construct the dialog
    @Override
    String getTitle() {
        return "Edit Entry";
    }

    @Override
    String getPositiveButtonText() {
        return "Update Entry";
    }

    @Override
    String getNegativeButtonText() {
        return "Delete Entry";
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
    public void onNegativeClicked(SessionEntry removeEntry) {
        if (mOnFinishedListener != null)
            mOnFinishedListener.onEditEntryDeleteEntry(mEntry);
    }

    @Override
    public void onPositiveClicked(SessionEntry newEntry) {
        if (mOnFinishedListener != null)
            mOnFinishedListener.onEditEntryUpdateEntry(newEntry);
    }

}
