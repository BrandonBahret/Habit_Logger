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

    public interface IGetKey <Out> {
        Out get(Object object);
    }

    public interface KeyComparator {
        int compare(Object element, Object key);
    }

    public static <SetType, ListType> Set<SetType> listToSet(List<ListType> list, IGetKey<SetType> collectObj){
        Set<SetType> set = new HashSet<>();
        for(ListType element : list)
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

    public static int binarySearch(List<?> list, final Object key, final KeyComparator comparator){
        return Collections.binarySearch(list, null, new Comparator<Object>() {
            @Override
            public int compare(Object obj, Object nullObj) {
                return comparator.compare(obj, key);
            }
        });
    }

    public static <In> Long sum(In[] objects, IGetKey keyGetter) {
        Long sum = 0L;

        for (In object : objects)
            sum += (Long)keyGetter.get(object);

        return sum;
    }

    public static <In> Long sum(Iterable<In> objects, IGetKey keyGetter) {
        Long sum = 0L;

        for (In object : objects)
            sum += (Long)keyGetter.get(object);

        return sum;
    }

}
