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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingEntrySetReplacingIterator<K, V> implements Iterator<Map.Entry<K, V>> {

    private final RecordingMap<Map<K, V>, K, V> recordingMap;
    private final Iterator<Map.Entry<K, V>> iterator;
    private MutableEntry<K, V> current;
    private Map<K, V> replacedElements;

    @SuppressWarnings("unchecked")
    public RecordingEntrySetReplacingIterator(RecordingMap<? extends Map<K, V>, K, V> recordingMap) {
        this.recordingMap = (RecordingMap<Map<K, V>, K, V>) recordingMap;
        this.iterator = (Iterator<Map.Entry<K, V>>) (Iterator<?>) recordingMap.delegate.entrySet().iterator();
        this.current = new MutableEntry<>();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Map.Entry<K, V> next() {
        // If replaced elements are set, we are in "replace mode" and remove all elements to retain the same order
        if (replacedElements != null) {
            Map.Entry<K, V> entry = iterator.next();
            current.key = entry.getKey();
            current.value = entry.getValue();
            replacedElements.put(entry.getKey(), entry.getValue());
            iterator.remove();
            return entry;
        } else {
            Map.Entry<K, V> entry = iterator.next();
            current.key = entry.getKey();
            current.value = entry.getValue();
            return entry;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public V replace() {
        // Starting the replace mode is only required once as next() will remove upcoming elements
        if (replacedElements == null) {
            replacedElements = new LinkedHashMap<>();
            V old = recordingMap.get(current.key);
            iterator.remove();
            return old;
        } else {
            // Remove the last element that was added via next() as it will be replaced
            return replacedElements.remove(current.key);
        }
    }

    public void replaceValue(Set<Object> removedKeys) {
        if (replacedElements == null) {
            replacedElements = new LinkedHashMap<>();
            removedKeys.add(current.key);
            iterator.remove();
        } else {
            // Remove the last element that was added via next() as it will be replaced
            removedKeys.add(current.key);
            replacedElements.remove(current.key);
        }
    }

    public void reset() {
        // Re-add the elements in the appropriate order
        if (replacedElements != null) {
            Map<K, V> delegate = recordingMap.getDelegate();
            // We re-add the last element if it wasn't re-added properly
            // This can happen when an exception happens during flushing
            if (replacedElements.isEmpty() || current.value != replacedElements.get(current.key)) {
                delegate.put(current.key, current.value);
            }
            delegate.putAll(replacedElements);
        }
    }

    public void add(K key, V value) {
        if (replacedElements != null) {
            replacedElements.put(key, value);
            current.key = null;
            current.value = null;
        }
    }

    /**
     * Mutable entry.
     *
     * @param <K> Key type
     * @param <V> Value type
     * @since 1.3.0
     */
    private static final class MutableEntry<K, V> {
        K key;
        V value;
    }
}
