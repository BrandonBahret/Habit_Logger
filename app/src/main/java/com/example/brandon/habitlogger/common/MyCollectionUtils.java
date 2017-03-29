package com.example.brandon.habitlogger.common;

import com.android.internal.util.Predicate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Brandon on 3/20/2017.
 * Helper class to work with collections
 */

public class MyCollectionUtils {

    public interface IGetKey<In, Out> {
        Out get(In object);
    }

    public interface KeyComparator {
        int compare(Object element, Object key);
    }

    public static <SetType, ListType> Set<SetType> listToSet(List<ListType> list, IGetKey<ListType, SetType> collectObj) {
        Set<SetType> set = new HashSet<>();
        for (ListType element : list)
            set.add(collectObj.get(element));

        return set;
    }

    public static <T> void filter(List<T> list, Predicate<? super T> shouldRemove) {
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (shouldRemove.apply(iterator.next()))
                iterator.remove();
        }
    }

    /**
     * @return The non-negative index of the element, or a negative index which is the -index - 1 where the element would be inserted
     */
    public static int binarySearch(List<?> list, final Object key, final KeyComparator comparator) {
        return Collections.binarySearch(list, null, new Comparator<Object>() {
            @Override
            public int compare(Object obj, Object nullObj) {
                return comparator.compare(obj, key);
            }
        });
    }

    public static <In> double sum(In[] objects, IGetKey<In, ? extends Number> keyGetter) {
        double sum = 0.0;
        for (In object : objects)
            sum += keyGetter.get(object).doubleValue();

        return sum;
    }

    public static <In> double sum(Iterable<In> objects, IGetKey<In, ? extends Number> keyGetter) {
        double sum = 0.0;
        for (In object : objects)
            sum += keyGetter.get(object).doubleValue();

        return sum;
    }

}
