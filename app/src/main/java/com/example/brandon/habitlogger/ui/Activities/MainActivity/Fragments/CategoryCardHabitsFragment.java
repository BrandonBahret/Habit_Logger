package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataExportHelpers.LocalDataExportManager;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ui.Activities.HabitDataActivity.HabitDataActivity;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.CategoryCardAdapter;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewAdapter;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewHolder;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.ui.Activities.SessionActivity.SessionActivity;
import com.example.brandon.habitlogger.ui.Dialogs.ConfirmationDialog;
import com.example.brandon.habitlogger.ui.Dialogs.HabitDialog.EditHabitDialog;
import com.example.brandon.habitlogger.ui.Widgets.RecyclerViewDecorations.SpaceOffsetDecoration;

import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryCardHabitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryCardHabitsFragment extends MyFragmentBase {

    //region (Member attributes)
//    List<CategoryDataSample> mData;

    private CategoryCardAdapter mHabitAdapter;
    private SpaceOffsetDecoration mSpaceDecoration;

    private SessionManager mSessionManager;
    private LocalDataExportManager mLocalExportManager;
    private PreferenceChecker mPreferenceChecker;
    //endregion

    public CategoryCardHabitsFragment() {
        // Required empty public constructor
    }

    public static CategoryCardHabitsFragment newInstance(){
        return new CategoryCardHabitsFragment();
    }

    //region Methods responsible for handling the activity lifecycle
    @Override
    protected int getNoDataLayoutId() {
        return R.id.no_habits_available_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionManager = new SessionManager(getContext());
        mLocalExportManager = new LocalDataExportManager(getContext());
        mPreferenceChecker = new PreferenceChecker(getContext());
    }

    @Override
    void onSetUpView(RecyclerView recyclerView) {
//        mData = mHabitDatabase.getAllData();
//        MyCollectionUtils.filter(mData, CategoryDataSample.IFilterEmptySamples);
//        mHabitAdapter = new CategoryCardAdapter(mData, mSessionManager, getHabitMenuItemClickListener(), getHabitButtonClickCallback());
//        recyclerView.setAdapter(mHabitAdapter);
//        applySpaceDecoration();
    }

    @Override
    public void onStart() {
        super.onStart();
        mCallbackInterface.showFab(true);
    }
    //endregion -- end --

    //region Methods responsible for updating the Ui
    @Override
    protected void updateHabitCards(List<SessionEntry> entries) {

    }

    @Override
    public void addHabitToLayout(Habit habit) {

    }

    @Override
    public void notifySessionEnded(long habitId) {

    }

    @Override
    protected void checkIfHabitsAreAvailable() {
        boolean habitsAvailable = mHabitDatabase.getNumberOfHabits() != 0;
        mRecyclerView.setVisibility(habitsAvailable ? View.VISIBLE : View.GONE);
        mNoDataLayout.setVisibility(habitsAvailable ? View.GONE : View.VISIBLE);
    }

    @Override
    public void restartFragment() {

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
    //endregion -- end --

    //region Methods responsible for handling events
    @Override
    public boolean handleOnQuery(String query) {
        return true;
    }

    private HabitViewAdapter.MenuItemClickListener getHabitMenuItemClickListener() {
        return new HabitViewAdapter.MenuItemClickListener() {
            @Override
            public void onHabitEditClick(long habitId, final HabitViewHolder habitViewHolder) {
                Habit habit = mHabitDatabase.getHabit(habitId);
                EditHabitDialog dialog = EditHabitDialog.newInstance(habit, new EditHabitDialog.OnFinishedListener() {
                    @Override
                    public void onFinishedWithResult(Habit habit) {
                        mHabitDatabase.updateHabit(habit.getDatabaseId(), habit);

//                        int position = mHabitAdapter.getAdapterItemPosition(habit.getDatabaseId());
//                        int position = habitViewHolder.getAdapterPosition();
//                        mData.set(position, habit);
//                        mHabitAdapter.notifyItemChanged(position);
                    }
                });

                dialog.show(getActivity().getSupportFragmentManager(), "edit-habit");
            }

            @Override
            public void onHabitResetClick(final long habitId, HabitViewHolder habitViewHolder) {
                String habitName = mHabitDatabase.getHabitName(habitId);
                String messageFormat = getString(R.string.confirm_habit_data_reset_message_format);
                String message = String.format(Locale.getDefault(), messageFormat, habitName);
                new ConfirmationDialog(getActivity())
                        .setTitle(getString(R.string.confirm_habit_data_reset_title))
                        .setMessage(message)
                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mHabitDatabase.deleteEntriesForHabit(habitId);
                                Toast.makeText(getActivity(), R.string.entries_deleted_message, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }

            @Override
            public void onHabitDeleteClick(final long habitId, final HabitViewHolder habitViewHolder) {
                String habitName = mHabitDatabase.getHabitName(habitId);
                new ConfirmationDialog(getActivity())
                        .setTitle("Confirm Delete")
                        .setMessage("Do you really want to delete '" + habitName + "'?")
                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mSessionManager.getIsSessionActive(habitId)) {
                                    mSessionManager.cancelSession(habitId);
                                }

                                mHabitDatabase.deleteHabit(habitId);

                                // todo remove habit from layout
//                                int position = habitViewHolder.getAdapterPosition();
                            }
                        })
                        .show();
            }

            @Override
            public void onHabitExportClick(long habitId, HabitViewHolder habitViewHolder) {
                Habit habit = mHabitDatabase.getHabit(habitId);
                mLocalExportManager.shareExportHabit(habit);
            }

            @Override
            public void onHabitArchiveClick(final long habitId, HabitViewHolder habitViewHolder) {
                String habitName = mHabitDatabase.getHabitName(habitId);
                final boolean archivedState = mHabitDatabase.getIsHabitArchived(habitId);
                String actionName = archivedState ? "Unarchive" : "Archive";
                String actionNameLower = archivedState ? "unarchive" : "archive";

                new ConfirmationDialog(getContext())
                        .setTitle("Confirm " + actionName)
                        .setMessage("Do you really want to " + actionNameLower + " '" + habitName + "'? ")
                        .setOnYesClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mHabitDatabase.updateHabitIsArchived(habitId, !archivedState);

                                // todo remove from layout
                            }
                        })
                        .show();
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
        return R.string.category_card_fragment_title;
    }

}

