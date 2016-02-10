package com.blazebit.persistence.view.impl.collection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class RecordingList<E> extends RecordingCollection<List<E>, E> implements List<E> {

    public RecordingList(List<E> delegate) {
        super(delegate);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        actions.add(new ListAddAllAction<List<E>, E>(index, c));
        return delegate.addAll(index, c);
    }

    @Override
    public E set(int index, E element) {
        actions.add(new ListSetAction<List<E>, E>(index, element));
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        actions.add(new ListAddAction<List<E>, E>(index, element));
        delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        actions.add(new ListRemoveAction<List<E>, E>(index));
        return delegate.remove(index);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new RecordingListIterator<E>(delegate.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new RecordingListIterator<E>(delegate.listIterator(index));
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // TODO: implement
        throw new UnsupportedOperationException("Sublists for entity view collections are not yet supported!");
    }

    
    /**************
     * Read-only
     *************/

    @Override
    public E get(int index) {
        return delegate.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

}
