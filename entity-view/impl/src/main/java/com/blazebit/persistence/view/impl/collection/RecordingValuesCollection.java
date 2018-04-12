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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingValuesCollection<C extends Map<K, V>, K, V> implements Collection<V> {

    protected final Collection<V> delegate;
    protected final RecordingMap<C, K, V> recordingMap;

    public RecordingValuesCollection(Collection<V> delegate, RecordingMap<C, K, V> recordingMap) {
        this.delegate = delegate;
        this.recordingMap = recordingMap;
    }

    @Override
    public boolean add(V e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        recordingMap.addAction(new MapRemoveAllValuesAction<C, K, V>(o, recordingMap.delegate));
        return delegate.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        recordingMap.addAction(new MapRemoveAllValuesAction<C, K, V>(c, recordingMap.delegate));
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        recordingMap.addAction(new MapRetainAllValuesAction<C, K, V>(c, recordingMap.delegate));
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        recordingMap.addClearAction();
        delegate.clear();
    }

    @Override
    public Iterator<V> iterator() {
        return new RecordingValuesIterator<>(recordingMap);
    }
    
    /**************
     * Read-only
     *************/

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

}
