/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingNavigableSet<E> extends RecordingSortedSet<NavigableSet<E>, E> implements NavigableSet<E> {

    public RecordingNavigableSet(NavigableSet<E> delegate, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        super(delegate, allowedSubtypes, updatable, optimize);
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
