package com.example.brandon.habitlogger.ui.Activities.MainActivity.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.data.DataModels.Habit;
import com.example.brandon.habitlogger.data.DataModels.HabitCategory;
import com.example.brandon.habitlogger.data.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ui.Events.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Events.ScrollObservers.RecyclerViewScrollObserver;

import java.util.List;

/**
 * Created by Brandon on 4/12/2017.
 * Base class for main activity fragments.
 */

public abstract class MyFragmentBase extends Fragment {

    //region (Member attributes)
    private static final String RECYCLER_STATE = "RECYCLER_STATE";

    protected RecyclerView mRecyclerView;
    protected View mNoDataLayout;

//    protected View mContentView;
    private SessionManager mSessionManager;
    HabitDatabase mHabitDatabase;

    private Handler mUpdateHandler = new Handler();
    //endregion

    //region Code responsible for opening communication between fragments and the main activity
    protected IMainActivity mCallbackInterface;

    public interface IMainActivity {
        IScrollEvents getScrollEventsListener();

        void hideFab(boolean animate);

        void hideCurrentSessionsCard(boolean animate);

        void showFab(boolean animate);

        void showCurrentSessionsCard(boolean animate);
    }
    //endregion

    public MyFragmentBase() {
        // Required empty public constructor
    }

    //region Methods to handle the fragments lifecycle
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCallbackInterface = (IMainActivity) context;
    }

    abstract void onSetUpView(RecyclerView recyclerView);

    //region Methods responsible for creating the fragment
    abstract protected int getNoDataLayoutId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionManager = new SessionManager(getContext());
        mHabitDatabase = new HabitDatabase(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().findViewById(R.id.no_habits_available_layout).setVisibility(View.GONE);
        getActivity().findViewById(R.id.no_archived_habits_available_layout).setVisibility(View.GONE);
        getActivity().findViewById(R.id.no_results_layout).setVisibility(View.GONE);

        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_main_recycler_view, container, false);
        mNoDataLayout = getActivity().findViewById(getNoDataLayoutId());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(getOnScrollListener());

        onSetUpView(mRecyclerView);
        checkIfHabitsAreAvailable();

        return mRecyclerView;
    }
    //endregion

    //region foreground lifetime (onResume - onPause)
    @Override
    public void onResume() {
        super.onResume();

        if (getActivity().getIntent().hasExtra(RECYCLER_STATE)) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(
                    getActivity().getIntent().getExtras().getParcelable(RECYCLER_STATE)
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().getIntent().putExtra(
                RECYCLER_STATE, mRecyclerView.getLayoutManager().onSaveInstanceState()
        );
    }
    //endregion -- end --

    //region visible lifetime (onStart - onStop)
    @Override
    public void onStart() {
        super.onStart();
        startRepeatingTask();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRepeatingTask();
    }
    //endregion -- end --

    //endregion

    //region Methods responsible for maintaining state changes
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(RECYCLER_STATE, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }
    //endregion -- end --

    //region Code responsible to update habit card views
    protected abstract void updateHabitCards(List<SessionEntry> entries);

    public abstract void notifySessionEnded(long habitId);

    protected Runnable mUpdateCards = new Runnable() {
        @Override
        public void run() {
            List<SessionEntry> entries = mSessionManager.getActiveSessionList();
            updateHabitCards(entries);

            mUpdateHandler.postDelayed(mUpdateCards, 500);
        }
    };

    void startRepeatingTask() {
        mUpdateCards.run();
    }

    void stopRepeatingTask() {
        mUpdateHandler.removeCallbacks(mUpdateCards);
    }
    //endregion -- end --

    abstract protected void checkIfHabitsAreAvailable();

    public abstract void onCategoryRemoved(HabitCategory categoryRemoved);

    public abstract void onUpdateCategory(HabitCategory oldCategory, HabitCategory newCategory);

    public abstract void onUpdateHabit(Habit oldHabit, Habit newHabit);

    public void addHabitToLayout(Habit habit) {
        checkIfHabitsAreAvailable();
    }

    public void removeHabitFromLayout(int position) {
        checkIfHabitsAreAvailable();
    }

    abstract public void reapplySpaceDecoration();

    abstract public void restartFragment();

    public abstract void callNotifyDataSetChanged();

    //region Methods to handle events
    abstract public boolean handleOnQuery(String query);

    private RecyclerViewScrollObserver getOnScrollListener() {
        return new RecyclerViewScrollObserver() {
            @Override
            public void onScrollUp() {
                mCallbackInterface.getScrollEventsListener().onScrollUp();
            }

            @Override
            public void onScrollDown() {
                mCallbackInterface.getScrollEventsListener().onScrollDown();
            }
        };
    }
    //endregion -- end --

    @StringRes
    abstract public int getFragmentTitle();

}
