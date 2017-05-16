package com.example.brandon.habitlogger.ui.Events;

import android.os.Bundle;

/**
 * Created by Brandon on 5/16/2017.
 * An interface to create state containers
 */

public interface IStateContainer {
    void saveState(Bundle outState);

    void restoreState(Bundle savedInstanceState);
}
