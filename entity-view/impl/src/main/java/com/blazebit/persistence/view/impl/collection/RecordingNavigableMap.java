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

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingNavigableMap<C extends NavigableMap<K, V>, K, V> extends RecordingSortedMap<C, K, V> implements NavigableMap<K, V> {

    public RecordingNavigableMap(C delegate, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        super(delegate, allowedSubtypes, updatable, optimize);
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        // TODO: implement
        throw new UnsupportedOperationException("Submaps for entity view collections are not yet supported!");
    }

    
    /**************
     * Read-only
     *************/

    @Override
    public java.util.Map.Entry<K, V> lowerEntry(K key) {
        return delegate.lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return delegate.lowerKey(key);
    }

    @Override
    public java.util.Map.Entry<K, V> floorEntry(K key) {
        return delegate.floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return delegate.floorKey(key);
    }

    @Override
    public java.util.Map.Entry<K, V> ceilingEntry(K key) {
        return delegate.ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return delegate.ceilingKey(key);
    }

    @Override
    public java.util.Map.Entry<K, V> higherEntry(K key) {
        return delegate.higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
        return delegate.higherKey(key);
    }

    @Override
    public java.util.Map.Entry<K, V> firstEntry() {
        return delegate.firstEntry();
    }

    @Override
    public java.util.Map.Entry<K, V> lastEntry() {
        return delegate.lastEntry();
    }

    @Override
    public java.util.Map.Entry<K, V> pollFirstEntry() {
        return delegate.pollFirstEntry();
    }

    @Override
    public java.util.Map.Entry<K, V> pollLastEntry() {
        return delegate.pollLastEntry();
    }

}
