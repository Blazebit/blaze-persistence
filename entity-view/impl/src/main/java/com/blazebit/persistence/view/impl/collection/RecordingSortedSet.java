/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingSortedSet<C extends SortedSet<E>, E> extends RecordingSet<C, E> implements SortedSet<E> {

    protected RecordingSortedSet(C delegate, Set<Class<?>> allowedSubtypes, Set<Class<?>> parentRequiringSubtypes, Set<Class<?>> parentRequiringCreateSubtypes, boolean updatable, boolean optimize, boolean strictCascadingCheck) {
        super(delegate, allowedSubtypes, parentRequiringSubtypes, parentRequiringCreateSubtypes, updatable, optimize, false, false, strictCascadingCheck);
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
