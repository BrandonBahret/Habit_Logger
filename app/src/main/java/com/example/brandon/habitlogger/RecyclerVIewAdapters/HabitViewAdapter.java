package com.example.brandon.habitlogger.RecyclerVIewAdapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.brandon.habitlogger.HabitDatabase.Habit;
import com.example.brandon.habitlogger.R;

import java.util.List;

/**
 * Created by Brandon on 12/26/2016.
 */

public class HabitViewAdapter extends RecyclerView.Adapter<HabitViewAdapter.ViewHolder> {

    List<Habit> habitsList;

    private MenuItemClickListener menuItemClickListener;
    private ButtonClickListener buttonClickListener;

    public interface MenuItemClickListener{
        void onEditClick(long habitId);
        void onDeleteClick(long habitId);
        void onExportClick(long habitId);
        void onArchiveClick(long habitId);
        void onStartSession(long habitId);
    }

    public interface ButtonClickListener{
        void onPlayButtonClicked(long habitId);
        void onPlayButtonLongClicked(long habitId);

        void onCardClicked(long habitId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView rootView;
        public ImageView categoryAccent;
        public Toolbar toolbar;
        public ImageButton playButton;

        public ViewHolder(View view) {
            super(view);
            this.categoryAccent = (ImageView) view.findViewById(R.id.category_accent);
            this.toolbar = (Toolbar) view.findViewById(R.id.card_toolbar);
            this.playButton = (ImageButton) view.findViewById(R.id.session_control_button);
            this.rootView = (CardView)view;
        }
    }

    public HabitViewAdapter(List<Habit> habitsList, MenuItemClickListener menuItemClickListener,
                            ButtonClickListener buttonClickListener){

        this.habitsList = habitsList;
        this.menuItemClickListener = menuItemClickListener;
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_card, parent, false);

        setMargins(itemView, 0, 20, 0, 20);

        return new ViewHolder(itemView);
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Habit item = habitsList.get(position);
        final long habitId = item.getDatabaseId();

        holder.rootView.setTag(String.valueOf(item.getDatabaseId()));

        // Make sure menu hasn't been inflated yet.
        int menuSize = holder.toolbar.getMenu().size();
        if(menuSize == 0) {
            if(item.getIsArchived()){
                holder.toolbar.inflateMenu(R.menu.menu_archived_habit_card);
                holder.categoryAccent.setBackgroundColor(0xFFCCCCCC);
            }
            else{
                holder.toolbar.inflateMenu(R.menu.menu_habit_card);
                holder.categoryAccent.setBackgroundColor(item.getCategory().getColorAsInt());
            }
            holder.toolbar.setTitle(item.getName());

            holder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();

                    switch (id) {
                        case (R.id.habit_menu_edit): {
                            menuItemClickListener.onEditClick(habitId);
                        }
                        break;

                        case(R.id.menu_enter_session):{
                            menuItemClickListener.onStartSession(habitId);
                        }break;

                        case (R.id.habit_menu_delete): {
                            menuItemClickListener.onDeleteClick(habitId);
                        }
                        break;

                        case (R.id.habit_menu_export): {
                            menuItemClickListener.onExportClick(habitId);
                        }
                        break;

                        case (R.id.habit_menu_archive): {
                            menuItemClickListener.onArchiveClick(habitId);
                        }
                        break;
                    }

                    return true;
                }
            });
        }

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickListener.onPlayButtonClicked(habitId);
            }
        });

        holder.playButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buttonClickListener.onPlayButtonLongClicked(habitId);
                return true;
            }
        });

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClickListener.onCardClicked(habitId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }

    public static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
        }
    }

    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

        private Drawable divider;

        /**
         * Default divider will be used
         */
        public DividerItemDecoration(Context context, String text) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);

            divider = styledAttributes.getDrawable(0);
            styledAttributes.recycle();
        }

        /**
         * Custom divider will be used
         */
        public DividerItemDecoration(Context context, int resId) {
            divider = ContextCompat.getDrawable(context, resId);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();

                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }
}

