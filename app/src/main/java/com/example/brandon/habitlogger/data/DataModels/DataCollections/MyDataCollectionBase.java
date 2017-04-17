package com.example.brandon.habitlogger.data.DataModels.DataCollections;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Brandon on 4/17/2017.
 * A simple base class for creating collections of data models.
 */

public abstract class MyDataCollectionBase<ListType> extends ArrayList<ListType> {

    public MyDataCollectionBase(int initialCapacity) {
        super(initialCapacity);
    }

    public MyDataCollectionBase(@NonNull Collection<? extends ListType> c) {
        super(c);
    }

    /**
     * Called whenever a super method that modifies data is called.
     */
    abstract void invalidate();

    //region invalidate on data change

    @Override
    public ListType set(int index, ListType element) {
        invalidate();
        return super.set(index, element);
    }

    @Override
    public boolean add(ListType listType) {
        invalidate();
        return super.add(listType);
    }

    @Override
    public void add(int index, ListType element) {
        invalidate();
        super.add(index, element);
    }

    @Override
    public ListType remove(int index) {
        invalidate();
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        invalidate();
        return super.remove(o);
    }

    @Override
    public void clear() {
        invalidate();
        super.clear();
    }

    @Override
    public boolean addAll(Collection<? extends ListType> c) {
        invalidate();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends ListType> c) {
        invalidate();
        return super.addAll(index, c);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        invalidate();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        invalidate();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        invalidate();
        return super.retainAll(c);
    }

    //endregion -- end --

}
