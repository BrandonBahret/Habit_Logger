package com.example.brandon.habitlogger.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 3/20/2017.
 *
 */

public class MyCollectionUtils {

    public interface Filter<T> {
        boolean retain(T item);
    }

    public static <T> void filter(List<T> list, Filter<? super T> filter) {
        List<T> itemsToRetain = new ArrayList<>(list.size());

        for(T item : list){
            if(filter.retain(item))
                itemsToRetain.add(item);
        }

        list.clear();
        list.addAll(itemsToRetain);
    }

}
