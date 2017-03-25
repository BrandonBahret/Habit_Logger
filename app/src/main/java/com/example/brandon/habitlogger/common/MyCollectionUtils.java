package com.example.brandon.habitlogger.common;

import com.android.internal.util.Predicate;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Brandon on 3/20/2017.
 * Helper class to work with collections
 */

public class MyCollectionUtils {

    public interface IGetKey {
        Object get(Object object);
    }

    public static <T> void filter(List<T> list, Predicate<? super T> shouldRemove) {

        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (shouldRemove.apply(iterator.next()))
                iterator.remove();
        }

    }

    public static <In> Long sum(In[] objects, IGetKey keyGetter) {
        Long sum = 0L;

        for (In object : objects)
            sum += (Long)keyGetter.get(object);

        return sum;
    }

}
