package com.example.brandon.habitlogger.ModifyHabitActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.brandon.habitlogger.R;

import java.util.ArrayList;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ChangeColorDialogFragment extends DialogFragment {
    private ArrayList<OnFinishedListener> onFinishedListeners = new ArrayList<>();

    public void setOnFinishedListener(OnFinishedListener listener){
        onFinishedListeners.add(listener);
    }

    public interface OnFinishedListener{
        void onFinishedWithResult(int color);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Select a color");

        builder.setCancelable(true);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        // Prepare grid view
        GridView gridView = new GridView(getContext());


        int intColors[] = getResources().getIntArray(R.array.colors);
        Integer colors[] = new Integer[intColors.length];
        for(int i = 0; i < intColors.length; ++i){
            colors[i] = intColors[i];
        }
        final ArrayAdapter<Integer> arrayAdapter = new ColorArrayAdapter(getContext(), colors);
        gridView.setAdapter(arrayAdapter);

        gridView.setGravity(Gravity.CENTER);
        gridView.setHorizontalSpacing(getDp(10));
        gridView.setVerticalSpacing(getDp(10));
        gridView.setNumColumns(4);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // do something here
                Integer color = arrayAdapter.getItem(position);
                if(color == null){
                    throw new Error("Failed to get color at line: 60 ChangeColorDialogFragment.java");
                }

                for(OnFinishedListener listener: onFinishedListeners){
                    listener.onFinishedWithResult(color);
                }
                dismiss();
            }
        });

        builder.setView(gridView);

        return builder.create();
    }

    public int getDp(int px){
        float logicalDensity = getResources().getDisplayMetrics().density;
        return (int)logicalDensity * px;
    }
}

class ColorArrayAdapter extends ArrayAdapter<Integer> {
    Context context;

    public ColorArrayAdapter(Context context, Integer[] items){
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Integer color = getItem(position);
        ImageView view = new ImageView(context);

        view.setImageResource(R.drawable.ic_simple_circle);

        if(color != null)
            view.setColorFilter(color);

        view.setScaleX(1.8f);
        view.setScaleY(1.8f);
        view.setPadding(20, 30, 20, 30);

        return view;
    }
}
