package com.example.brandon.habitlogger.ui.Widgets;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brandon.habitlogger.data.HabitSessions.SessionManager;
import com.example.brandon.habitlogger.ui.Activities.PreferencesActivity.PreferenceChecker;
import com.example.brandon.habitlogger.R;

/**
 * Created by Brandon on 4/2/2017.
 * Class to manage the current sessions card
 */

public class CurrentSessionCardManager {

    //region (Member attributes)
    private ViewHolder mViewHolder;

    private final int mBackgroundColor;
    private final int mAccentColor;

    private final int mDisabledBackgroundColor;
    private final int mDisabledAccentColor;
    //endregion

    public class ViewHolder {
        CardView itemView;
        ImageView accent;
        TextView title;
        TextView captionValue;
        TextView captionDescription;

        public ViewHolder(View v) {
            itemView = (CardView) v;
            accent = (ImageView) v.findViewById(R.id.card_accent);
            title = (TextView) v.findViewById(R.id.title);
            captionValue = (TextView) v.findViewById(R.id.active_session_value_text);
            captionDescription = (TextView) v.findViewById(R.id.active_session_description_text);
        }
    }

    public CurrentSessionCardManager(View view) {
        Context context = view.getContext();
        mViewHolder = new ViewHolder(view);

        mBackgroundColor = ContextCompat.getColor(context, R.color.currentSessionsBackground);
        mAccentColor = ContextCompat.getColor(context, R.color.currentSessionsAccent);

        mDisabledBackgroundColor = ContextCompat.getColor(context, R.color.currentSessionsBackgroundDisabled);
        mDisabledAccentColor = ContextCompat.getColor(context, R.color.currentSessionsAccentDisabled);
    }

    //region Methods responsible for hiding/showing the current sessions card
    public void showView(boolean animate) {
        if (animate)
            showView();
        else {
            mViewHolder.itemView.setAlpha(1);
            mViewHolder.itemView.setTranslationY(0);
        }
    }

    public void showView() {
        mViewHolder.itemView.animate()
                .setStartDelay(0)
                .setDuration(300)
                .alpha(1)
                .translationY(0);
    }

    public void hideView(boolean animate) {
        if (animate)
            hideView();
        else {
            mViewHolder.itemView.setAlpha(0);
            mViewHolder.itemView.setTranslationY(-mViewHolder.itemView.getHeight());
        }
    }

    public void hideView() {
        mViewHolder.itemView.animate()
                .setStartDelay(0)
                .setDuration(300)
                .alpha(0)
                .translationY(-mViewHolder.itemView.getHeight());
    }
    //endregion -- end --

    //region Methods responsible for updating the ui
    public void updateColor(int sessionCount, PreferenceChecker preferenceChecker) {
        if (!preferenceChecker.allowActiveSessionsActivity(sessionCount > 0)) {
            mViewHolder.itemView.setCardBackgroundColor(mDisabledBackgroundColor);
            mViewHolder.accent.setBackgroundColor(mDisabledAccentColor);
            mViewHolder.title.setAlpha(0.5f);
            mViewHolder.captionValue.setAlpha(0.5f);
            mViewHolder.captionDescription.setAlpha(0.5f);
        }
        else {
            mViewHolder.itemView.setCardBackgroundColor(mBackgroundColor);
            mViewHolder.accent.setBackgroundColor(mAccentColor);
            mViewHolder.title.setAlpha(1);
            mViewHolder.captionValue.setAlpha(1);
            mViewHolder.captionDescription.setAlpha(1);
        }
    }

    public void updateCard(SessionManager sessionManager, PreferenceChecker preferenceChecker) {
        int sessionCount = (int) sessionManager.getNumberOfActiveSessions();
        updateColor(sessionCount, preferenceChecker);

        if (preferenceChecker.shouldShowCurrentSessions(sessionCount))
            setVisibility(View.VISIBLE);
        else
            setVisibility(View.GONE);

        if (sessionCount == 0) {
            mViewHolder.captionValue.setText(R.string.no);
            mViewHolder.captionDescription.setText(R.string.active_sessions_lower);
        }
        else if (sessionCount == 1) {
            mViewHolder.captionValue.setText(R.string.one);
            mViewHolder.captionDescription.setText(R.string.active_session);
        }
        else {
            mViewHolder.captionValue.setText(String.valueOf(sessionCount));
            mViewHolder.captionDescription.setText(R.string.active_sessions_lower);
        }
    }
    //endregion

    //region Getters {}
    public ViewHolder getViewHolder() {
        return mViewHolder;
    }

    public int getVisibility() {
        return mViewHolder.itemView.getVisibility();
    }
    //endregion

    //region Setters {}
    public void setVisibility(int visible) {
        mViewHolder.itemView.setVisibility(visible);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mViewHolder.itemView.setOnClickListener(onClickListener);
    }
    //endregion

}
