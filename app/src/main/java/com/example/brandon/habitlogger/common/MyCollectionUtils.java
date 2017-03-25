package com.example.brandon.habitlogger.common;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 3/20/2017.
 * Helper class to work with collections
 */

public class MyCollectionUtils {

    public static <T> void filter(List<T> list, Predicate<? super T> filter) {
        List<T> itemsToRetain = new ArrayList<>(list.size());

        for (T item : list) {
            if (filter.apply(item))
                itemsToRetain.add(item);
        }

        list.clear();
        list.addAll(itemsToRetain);
    }

}
