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

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingSortedSet<C extends SortedSet<E>, E> extends RecordingSet<C, E> implements SortedSet<E> {

    protected RecordingSortedSet(C delegate, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        super(delegate, allowedSubtypes, updatable, optimize, false, false);
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
