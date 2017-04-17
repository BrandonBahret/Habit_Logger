package com.example.brandon.habitlogger.common;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
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

    public interface IGetList<In, Out> {
        List<Out> getList(In object);
    }

    public interface ICompareKey {
        int compare(Object element, Object key);
    }

    //region Methods to collect from lists
    public static <ListType, Collect> List<Collect> collect
    (List<ListType> list, IGetKey<ListType, Collect> keyGetter) {

        List<Collect> collection = new ArrayList<>();
        for (ListType item : list)
            collection.add(keyGetter.get(item));

        return collection;
    }

    public static <SetType, ListType> Set<SetType> collectIntoSet
            (List<ListType> list, IGetKey<ListType, SetType> keyGetter) {

        Set<SetType> set = new HashSet<>();
        for (ListType element : list)
            set.add(keyGetter.get(element));

        return set;
    }

    public static <ListType, Collect> List<Collect> collectLists
            (List<ListType> list, IGetList<ListType, Collect> keyGetter) {

        List<Collect> collection = new ArrayList<>();
        for (ListType item : list)
            collection.addAll(keyGetter.getList(item));

        return collection;
    }
    //endregion -- end --

    //region Methods to search lists
    /**
     * @return The non-negative index of the element, or a negative index which is the -index - 1 where the element would be inserted
     */
    public static int binarySearch(List<?> list, final Object key, final ICompareKey comparator) {
        return Collections.binarySearch(list, null, new Comparator<Object>() {
            @Override
            public int compare(Object obj, Object nullObj) {
                return comparator.compare(obj, key);
            }
        });
    }

    public static int binarySearchForInsertPosition
            (List<?> list, final Object key, final ICompareKey comparator) {

        int pos = binarySearch(list, key, comparator);
        return -pos - 1;
    }
    //endregion -- end --

    public static <T> void filter(List<T> list, Predicate<? super T> removeIf) {
        if(list == null) return;

        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (removeIf.apply(iterator.next())) iterator.remove();
        }
    }

    public static <In> double sum(Iterable<In> objects, IGetKey<In, ? extends Number> keyGetter) {
        double sum = 0.0;
        for (In object : objects)
            sum += keyGetter.get(object).doubleValue();

        return sum;
    }

    public static <In> double sum(In[] objects, IGetKey<In, ? extends Number> keyGetter) {
        double sum = 0.0;
        for (In object : objects)
            sum += keyGetter.get(object).doubleValue();

        return sum;
    }

}
