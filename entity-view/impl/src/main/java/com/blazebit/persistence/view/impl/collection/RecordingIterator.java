package com.blazebit.persistence.view.impl.collection;

import java.util.Iterator;

public class RecordingIterator<I extends Iterator<E>, E> implements Iterator<E> {

    protected final I delegate;
//    protected final List<IteratorAction<I>> actions;
    
    public RecordingIterator(I delegate) {
        this.delegate = delegate;
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public E next() {
        return delegate.next();
    }

    public void remove() {
        // TODO: implement
        throw new UnsupportedOperationException("Updating updatable entity view collections is not yet supported!");
    }
}
