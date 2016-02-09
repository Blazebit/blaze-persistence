package com.blazebit.persistence.view.impl.collection;

import java.util.ListIterator;

public class RecordingListIterator<E> extends RecordingIterator<ListIterator<E>, E> implements ListIterator<E> {

    public RecordingListIterator(ListIterator<E> delegate) {
        super(delegate);
    }

    @Override
    public void set(E e) {
        // TODO: implement
        throw new UnsupportedOperationException("Updating updatable entity view collections is not yet supported!");
    }

    @Override
    public void add(E e) {
        // TODO: implement
        throw new UnsupportedOperationException("Updating updatable entity view collections is not yet supported!");
    }

    /**************
     * Read-only
     *************/
    
    @Override
    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    @Override
    public E previous() {
        return delegate.previous();
    }

    @Override
    public int nextIndex() {
        return delegate.nextIndex();
    }

    @Override
    public int previousIndex() {
        return delegate.previousIndex();
    }

}
