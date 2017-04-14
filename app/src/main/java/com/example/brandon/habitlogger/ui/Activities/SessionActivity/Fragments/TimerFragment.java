package com.example.brandon.habitlogger.ui.Activities.SessionActivity.Fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.brandon.habitlogger.R;
import com.example.brandon.habitlogger.common.MyTimeUtils;
import com.example.brandon.habitlogger.databinding.FragmentTimerBinding;
import com.example.brandon.habitlogger.ui.Activities.MainActivity.HabitViewHolder;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ITimerFragment} interface
 * to handle interaction events.
 */
public class TimerFragment extends Fragment {

    //region (Member attributes)
    private FragmentTimerBinding ui;
    private Handler mUpdateHandler = new Handler();
    //endregion

    //region Code responsible for providing communication between the activity and the fragment
    private ITimerFragment mCallbackInterface;

    public interface ITimerFragment {
        void onSessionToggleClick();

        Long getSessionDuration(boolean required);

        boolean getSessionState();
    }
    //endregion -- end --

    public TimerFragment() {
        // Required empty public constructor
    }

    //region [ ---- Methods responsible for handling the fragment lifecycle ---- ]

    //region (onAttach - onDetach)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ITimerFragment)
            mCallbackInterface = (ITimerFragment) context;
        else {
            throw new RuntimeException(context.toString()
                    + " must implement ITimerFragment");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackInterface = null;
    }
    //endregion -- end --

    //region (onCreateView)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ui = DataBindingUtil.inflate(getLayoutInflater(savedInstanceState), R.layout.fragment_timer, container, false);

        ui.sessionPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbackInterface.onSessionToggleClick();
            }
        });

        return ui.getRoot();
    }
    //endregion -- end --

    //region (onResume - onPause)
    @Override
    public void onResume() {
        super.onResume();
        updateTimeDisplay(mCallbackInterface.getSessionDuration(true));
        updateSessionPlayButton(mCallbackInterface.getSessionState());
        startRepeatingTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRepeatingTask();
    }
    //endregion -- end --

    //endregion [ ---- end ---- ]

    //region Methods responsible for updating the ui
    private Runnable updateTimeDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeDisplay(mCallbackInterface.getSessionDuration(false));
            mUpdateHandler.postDelayed(updateTimeDisplayRunnable, 500);
        }
    };

    void startRepeatingTask() {
        updateTimeDisplayRunnable.run();
    }

    void stopRepeatingTask() {
        mUpdateHandler.removeCallbacks(updateTimeDisplayRunnable);
    }

    public void updateSessionPlayButton(boolean isPaused) {
        ui.sessionPausePlay.setImageResource(
                HabitViewHolder.getResourceIdForPauseButton(isPaused)
        );

        float alphaValue = isPaused ? 0.5f : 1.0f;

        ui.sessionTimeDisplayLayout.setAlpha(alphaValue);
    }

    public void updateTimeDisplay(Long duration) {
        if (duration != null) {
            int time[] = MyTimeUtils.getTimePortion(duration);

            ui.sessionHoursView.setText(String.format(Locale.US, "%02d", time[0]));
            ui.sessionMinutesView.setText(String.format(Locale.US, "%02d", time[1]));
            ui.sessionSecondsView.setText(String.format(Locale.US, "%02d", time[2]));
        }
    }
    //endregion -- end --

}
