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
import java.util.SortedMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingSortedMap<C extends SortedMap<K, V>, K, V> extends RecordingMap<C, K, V> implements SortedMap<K, V> {

    protected RecordingSortedMap(C delegate, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        super(delegate, allowedSubtypes, updatable, optimize, false, false);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    
    /**************
     * Read-only
     *************/

    @Override
    public Comparator<? super K> comparator() {
        return delegate.comparator();
    }

    @Override
    public K firstKey() {
        return delegate.firstKey();
    }

    @Override
    public K lastKey() {
        return delegate.lastKey();
    }

}
