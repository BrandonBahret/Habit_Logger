package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyCollectionUtils;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.HabitDataActivity;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewAdapter;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewHolder;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.SessionActivity;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.HabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.GroupDecoration;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllHabitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllHabitsFragment extends MyFragmentBase {

    //region (Member attributes)
    public class DialogState {
        public long habitId = -1;

        public void saveState(Bundle outState) {
            outState.putLong("habitId", habitId);
        }

        public void restoreState(Bundle savedInstanceState) {
            habitId = savedInstanceState.getLong("habitId");
        }
    }

    private DialogState mDialogState = new DialogState();

    private final String KEY_DATA = "KEY_DATA";
    private boolean mCreatedUsingSavedInstance = false;
    List<Habit> mData;

    private HabitViewAdapter mHabitAdapter;
    private SpaceOffsetDecoration mSpaceDecoration;
    private GroupDecoration mGroupDecoration;

    private SessionManager mSessionManager;
    private LocalDataExportManager mLocalExportManager;
    private PreferenceChecker mPreferenceChecker;
    //endregion

    public AllHabitsFragment() {
        // Required empty public constructor
    }

    public static AllHabitsFragment newInstance() {
        return new AllHabitsFragment();
    }

    //region Methods responsible for handling the fragments lifecycle

    @Override
    protected int getNoDataLayoutId() {
        return R.id.no_habits_available_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mDialogState.restoreState(savedInstanceState);
            resetDialogListeners();
        }

        mSessionManager = new SessionManager(getContext());
        mLocalExportManager = new LocalDataExportManager(getContext());
        mPreferenceChecker = new PreferenceChecker(getContext());

        mCreatedUsingSavedInstance = savedInstanceState != null;
        if (mCreatedUsingSavedInstance) {
            mData = savedInstanceState.getParcelableArrayList(KEY_DATA);
        }
    }

    @Override
    void onSetUpView(RecyclerView recyclerView) {
        if (!mCreatedUsingSavedInstance) {
            mData = mHabitDatabase.getHabits();
            MyCollectionUtils.filter(mData, Habit.ICheckIfIsArchived);
        }
        else {
            updateNoResultsLayout(mData == null || mData.isEmpty());
        }

        mHabitAdapter = new HabitViewAdapter(getContext(), mData, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
        recyclerView.setAdapter(mHabitAdapter);
        applyGroupDecoration();
        applySpaceDecoration();
    }

    @Override
    public void onStart() {
        super.onStart();
        mCallbackInterface.showFab(true);
    }

    @Override
    protected void checkIfHabitsAreAvailable() {
        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
        mRecyclerView.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
        mNoDataLayout.setVisibility(habitsAvailable ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDialogState.saveState(outState);
        outState.putParcelableArrayList(KEY_DATA, (ArrayList<? extends Parcelable>) mData);
    }

    //endregion

    //region Methods responsible for updating the Ui
    @Override
    protected void updateHabitCards(List<SessionEntry> entries) {
        if (mHabitAdapter != null)
            mHabitAdapter.updateHabitViews(entries);
    }

    @Override
    public void notifySessionEnded(long habitId) {
        mHabitAdapter.notifySessionEnded(habitId);
    }

    @Override
    public void restartFragment() {
        mData = mHabitDatabase.getHabits();
        MyCollectionUtils.filter(mData, Habit.ICheckIfIsArchived);
        mHabitAdapter = new HabitViewAdapter(getContext(), mData, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
        mRecyclerView.setAdapter(mHabitAdapter);
        mCallbackInterface.showCurrentSessionsCard(false);
        mCallbackInterface.showFab(true);
        mRecyclerView.removeItemDecoration(mSpaceDecoration);
        mRecyclerView.removeItemDecoration(mGroupDecoration);
        applyGroupDecoration();
        applySpaceDecoration();
        checkIfHabitsAreAvailable();
    }

    @Override
    public void addHabitToLayout(Habit habit) {
        super.addHabitToLayout(habit);
        mData.add(habit);
        Collections.sort(mData, Habit.ICompareHabitName);
        Collections.sort(mData, Habit.ICompareCategoryName);
        mHabitAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeHabitFromLayout(int position) {
        super.removeHabitFromLayout(position);
        mData.remove(position);
        mHabitAdapter.notifyItemRemoved(position);
        applySpaceDecoration();
        mRecyclerView.invalidateItemDecorations();
    }

    @Override
    public void reapplySpaceDecoration() {
        mRecyclerView.removeItemDecoration(mSpaceDecoration);
        applySpaceDecoration();
    }

    @Override
    public void callNotifyDataSetChanged() {
        mHabitAdapter.notifyDataSetChanged();
    }

    private void applySpaceDecoration() {
        int sessionCount = (int) mSessionManager.getNumberOfActiveSessions();
        boolean useLargeTopOffset = mPreferenceChecker.shouldShowCurrentSessions(sessionCount);
        int topOffset = useLargeTopOffset ? (int) getResources().getDimension(R.dimen.large_top_offset_dp) : (int) getResources().getDimension(R.dimen.top_offset_dp);

        if (mPreferenceChecker.howToDisplayCategories() == PreferenceChecker.AS_SECTIONS)
            topOffset += (int) getResources().getDimension(R.dimen.sections_top_offset_dp);

        int bottomOffset = (int) getResources().getDimension(R.dimen.bottom_offset_dp);

        if (mSpaceDecoration != null)
            mRecyclerView.removeItemDecoration(mSpaceDecoration);

        mSpaceDecoration = new SpaceOffsetDecoration(bottomOffset, topOffset);
        mRecyclerView.addItemDecoration(mSpaceDecoration);
    }

    private void applyGroupDecoration() {
        switch (mPreferenceChecker.howToDisplayCategories()) {
            case PreferenceChecker.AS_SECTIONS:
                mGroupDecoration = getGroupDecoration();
                mRecyclerView.addItemDecoration(mGroupDecoration);
                break;

            case PreferenceChecker.WITHOUT_CATEGORIES:
                mRecyclerView.removeItemDecoration(mGroupDecoration);
                break;
        }
    }

    private GroupDecoration getGroupDecoration() {
        boolean shouldUseStickyHeaders = mPreferenceChecker.makeCategoryHeadersSticky();

        return new GroupDecoration(getContext(), R.dimen.category_section_text_size, shouldUseStickyHeaders,
                new GroupDecoration.Callback() {
                    @Override
                    public long getGroupId(int position) {
                        if (position >= 0 && position < mData.size())
                            return mData.get(position).getCategory().getDatabaseId();
                        else return -1;
                    }

                    @Override
                    public String getGroupFirstLine(int position) {
                        if (position >= 0 && position < mData.size())
                            return mData.get(position).getCategory().getName();
                        else return null;
                    }
                }
        );
    }
    //endregion

    //region Methods responsible for handling events
    @Override
    public boolean handleOnQuery(String query) {
        if (mHabitAdapter != null && query.length() != 0) {
            final Set<Long> databaseIds = mHabitDatabase.searchHabitDatabase(query);
            List<Habit> allHabits = mHabitDatabase.getHabits();
            MyCollectionUtils.filter(allHabits, new Predicate<Habit>() {
                @Override
                public boolean apply(Habit habit) {
                    return !databaseIds.contains(habit.getDatabaseId()) || habit.getIsArchived();
                }
            });

            mData.clear();
            mData.addAll(allHabits);
            mHabitAdapter.notifyDataSetChanged();

            updateNoResultsLayout(mData.isEmpty());
        }
        else if (mHabitAdapter != null && mData != null) {
            List<Habit> allHabits = mHabitDatabase.getHabits();
            MyCollectionUtils.filter(allHabits, new Predicate<Habit>() {
                @Override
                public boolean apply(Habit habit) {
                    return habit.getIsArchived();
                }
            });

            mData.clear();
            mData.addAll(allHabits);
            mHabitAdapter.notifyDataSetChanged();
            updateNoResultsLayout(false);
        }

        return true;
    }

    public void updateNoResultsLayout(boolean isEmpty) {
        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
        int visibility = isEmpty && habitsAvailable ? View.VISIBLE : View.GONE;
        View emptyResultsLayout = getActivity().findViewById(R.id.no_results_layout);

        if (emptyResultsLayout.getVisibility() != visibility) {
            mRecyclerView.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
            emptyResultsLayout.setVisibility(visibility);
        }
    }

    @Override
    public void onUpdateHabit(Habit oldHabit, Habit newHabit) {
        mHabitDatabase.updateHabit(oldHabit.getDatabaseId(), newHabit);

        int oldPos = mData.indexOf(oldHabit);
        mData.set(oldPos, newHabit);

        Collections.sort(mData, Habit.ICompareHabitName);
        Collections.sort(mData, Habit.ICompareCategoryName);

        mHabitAdapter.notifyDataSetChanged();
    }

    public int updateHabit(Habit oldHabit, Habit newHabit) {
        int pos = mData.indexOf(oldHabit);
        mData.remove(pos);
        mHabitAdapter.notifyItemRemoved(pos);

        int insertPos = MyCollectionUtils.binarySearchForInsertPosition
                (mData, newHabit.getCategory().getName(), Habit.IKeyCompareCategoryName);
        mData.add(insertPos, newHabit);
        mHabitAdapter.notifyItemInserted(insertPos);

        return insertPos;
    }

    private void resetDialogListeners() {
        DialogFragment dialog;
        FragmentManager fragmentManager = getFragmentManager();

        if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-habit-archive")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-habit-reset")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if ((dialog = (DialogFragment) fragmentManager.findFragmentByTag("confirm-habit-delete")) != null)
            setDialogListener((ConfirmationDialog) dialog);

        else if((dialog = (DialogFragment) fragmentManager.findFragmentByTag("edit-habit")) != null)
            ((HabitDialog)dialog).setPositiveButton(null, onYesUpdateHabitClicked);
    }

    private void setDialogListener(ConfirmationDialog dialog) {
        switch (dialog.getTag()) {
            case "confirm-habit-archive":
                dialog.setOnYesClickListener(onYesArchiveHabitClicked);
                break;
            case "confirm-habit-reset":
                dialog.setOnYesClickListener(onYesResetHabitClicked);
                break;
            case "confirm-habit-delete":
                dialog.setOnYesClickListener(onYesDeleteHabitClicked);
                break;
        }
    }

    //region Habit view menu option listeners
    DialogInterface.OnClickListener onYesResetHabitClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mHabitDatabase.deleteEntriesForHabit(mDialogState.habitId);
            Toast.makeText(getActivity(), R.string.entries_deleted_message, Toast.LENGTH_SHORT).show();
        }
    };

    DialogInterface.OnClickListener onYesDeleteHabitClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mSessionManager.getIsSessionActive(mDialogState.habitId)) {
                mSessionManager.cancelSession(mDialogState.habitId);
            }

            mHabitDatabase.deleteHabit(mDialogState.habitId);

            int position = mHabitAdapter.getAdapterItemPosition(mDialogState.habitId);
            removeHabitFromLayout(position);
        }
    };

    DialogInterface.OnClickListener onYesArchiveHabitClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mHabitDatabase.updateHabitIsArchived(mDialogState.habitId, true);

            int position = mHabitAdapter.getAdapterItemPosition(mDialogState.habitId);
            removeHabitFromLayout(position);
        }
    };
    //endregion -- end --

    HabitDialog.DialogResult onYesUpdateHabitClicked = new HabitDialog.DialogResult() {
        @Override
        public void onResult(Habit initHabit, Habit habit) {
            mHabitDatabase.updateHabit(initHabit.getDatabaseId(), habit);

            int oldPos = mData.indexOf(initHabit);
            mData.set(oldPos, habit);

            Collections.sort(mData, Habit.ICompareHabitName);
            Collections.sort(mData, Habit.ICompareCategoryName);

            mHabitAdapter.notifyDataSetChanged();
        }
    };

    private HabitViewAdapter.MenuItemClickListener getHabitMenuItemClickListener() {
        return new HabitViewAdapter.MenuItemClickListener() {
            @Override
            public void onHabitEditClick(long habitId, HabitViewHolder habitViewHolder) {
                Habit habit = mHabitDatabase.getHabit(habitId);

                HabitDialog dialog = new HabitDialog()
                        .setTitle("Edit Habit")
                        .setInitHabit(habit)
                        .setPositiveButton("Update", onYesUpdateHabitClicked)
                        .setNegativeButton("Cancel", null);

                dialog.show(getFragmentManager(), "edit-habit");
            }

            @Override
            public void onHabitResetClick(final long habitId, HabitViewHolder habitViewHolder) {
                String habitName = mHabitDatabase.getHabitName(habitId);
                String messageFormat = getString(R.string.confirm_habit_data_reset_message_format);
                String message = String.format(Locale.getDefault(), messageFormat, habitName);
                mDialogState.habitId = habitId;

                new ConfirmationDialog()
                        .setIcon(R.drawable.ic_delete_sweep_24dp)
                        .setTitle(getString(R.string.confirm_habit_data_reset_title))
                        .setMessage(message)
                        .setOnYesClickListener(onYesResetHabitClicked)
                        .show(getFragmentManager(), "confirm-habit-reset");
            }

            @Override
            public void onHabitDeleteClick(final long habitId, HabitViewHolder habitViewHolder) {
                String habitName = mHabitDatabase.getHabitName(habitId);
                mDialogState.habitId = habitId;

                new ConfirmationDialog()
                        .setIcon(R.drawable.ic_delete_forever_24dp)
                        .setTitle("Confirm Delete")
                        .setMessage("Do you really want to delete '" + habitName + "'?")
                        .setOnYesClickListener(onYesDeleteHabitClicked)
                        .show(getFragmentManager(), "confirm-habit-delete");
            }

            @Override
            public void onHabitExportClick(long habitId, HabitViewHolder habitViewHolder) {
                Habit habit = mHabitDatabase.getHabit(habitId);
                mLocalExportManager.shareExportHabit(habit);
            }

            @Override
            public void onHabitArchiveClick(final long habitId, HabitViewHolder habitViewHolder) {
                String habitName = mHabitDatabase.getHabitName(habitId);
                mDialogState.habitId = habitId;

                new ConfirmationDialog()
                        .setIcon(R.drawable.ic_archive_2_24dp)
                        .setTitle("Confirm Archive")
                        .setMessage("Do you really want to archive '" + habitName + "'? ")
                        .setOnYesClickListener(onYesArchiveHabitClicked)
                        .show(getFragmentManager(), "confirm-habit-archive");
            }

            @Override
            public void onHabitStartSession(long habitId, HabitViewHolder habitViewHolder) {
                SessionActivity.startActivity(getActivity(), mHabitDatabase.getHabit(habitId));
            }
        };
    }

    private HabitViewAdapter.ButtonClickCallback getHabitButtonClickCallback() {
        return new HabitViewAdapter.ButtonClickCallback() {

            @Override
            public View.OnClickListener getPlayButtonClickedListener(final long habitId, HabitViewHolder habitViewHolder) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mSessionManager.getIsSessionActive(habitId))
                            SessionActivity.startActivity(getActivity(), mHabitDatabase.getHabit(habitId));
                        else {
                            boolean isPaused = mSessionManager.getIsPaused(habitId);
                            mSessionManager.setPauseState(habitId, !isPaused);
                            mUpdateCards.run();
                        }
                    }
                };
            }

            @Override
            public View.OnLongClickListener getPlayButtonLongClickedListener(final long habitId, HabitViewHolder habitViewHolder) {
                return new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        SessionActivity.startActivity(getActivity(), mHabitDatabase.getHabit(habitId));
                        return true;
                    }
                };
            }

            @Override
            public View.OnClickListener getHabitViewClickedListener(final long habitId, HabitViewHolder habitViewHolder) {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HabitDataActivity.startActivity(getActivity(), habitId);
                    }
                };
            }

        };
    }
    //endregion -- end --

    @Override
    @StringRes
    public int getFragmentTitle() {
        return R.string.all_habits_fragment_title;
    }

}
