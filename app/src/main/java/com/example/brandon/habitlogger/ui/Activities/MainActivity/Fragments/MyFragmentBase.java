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
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.data.HabitDatabase.DataModels.SessionEntry;
import com.example.brandon.habitlogger.data.HabitDatabase.HabitDatabase;
import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.IScrollEvents;
import com.example.brandon.habitlogger.ui.Activities.ScrollObservers.RecyclerViewScrollObserver;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionManager = new SessionManager(getContext());
        mHabitDatabase = new HabitDatabase(getContext());
    }

    abstract void onSetUpView(RecyclerView recyclerView);

    abstract protected int getNoDataLayoutId();

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


    @Override
    public void onResume() {
        super.onResume();
        startRepeatingTask();

        if (getActivity().getIntent().hasExtra(RECYCLER_STATE)) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(
                    getActivity().getIntent().getExtras().getParcelable(RECYCLER_STATE)
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRepeatingTask();
        getActivity().getIntent().putExtra(
                RECYCLER_STATE, mRecyclerView.getLayoutManager().onSaveInstanceState()
        );
    }

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

            mUpdateHandler.postDelayed(mUpdateCards, 1000);
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

    public void addHabitToLayout(Habit habit) {
        checkIfHabitsAreAvailable();
    }

    public void removeHabitFromLayout(int position) {
        checkIfHabitsAreAvailable();
    }

    abstract public void refreshLayout();

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
