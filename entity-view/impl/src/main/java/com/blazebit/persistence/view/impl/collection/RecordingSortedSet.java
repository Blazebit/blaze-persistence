package com.blazebit.persistence.view.impl.collection;

import java.util.Comparator;
import java.util.SortedSet;

public class RecordingSortedSet<C extends SortedSet<E>, E> extends RecordingSet<C, E> implements SortedSet<E> {

    public RecordingSortedSet(C delegate) {
        super(delegate);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        // TODO: implement
        throw new UnsupportedOperationException("Subsets for entity view collections are not yet supported!");
    }

    
    /**************
     * Read-only
     *************/

    @Override
    public Comparator<? super E> comparator() {
        return delegate.comparator();
    }

    @Override
    public E first() {
        return delegate.first();
    }

    @Override
    public E last() {
        return delegate.last();
    }

}
