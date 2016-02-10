package com.blazebit.persistence.view.impl.collection;

import java.util.Iterator;
import java.util.NavigableSet;

public class RecordingNavigableSet<E> extends RecordingSortedSet<NavigableSet<E>, E> implements NavigableSet<E> {

    public RecordingNavigableSet(NavigableSet<E> delegate) {
        super(delegate);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }

    @Override
    public Iterator<E> descendingIterator() {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }
    
    /**************
     * Read-only
     *************/

    @Override
    public E lower(E e) {
        return delegate.lower(e);
    }

    @Override
    public E floor(E e) {
        return delegate.floor(e);
    }

    @Override
    public E ceiling(E e) {
        return delegate.ceiling(e);
    }

    @Override
    public E higher(E e) {
        return delegate.higher(e);
    }

    @Override
    public E pollFirst() {
        return delegate.pollFirst();
    }

    @Override
    public E pollLast() {
        return delegate.pollLast();
    }

}
