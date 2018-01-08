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
    private Object current;
    private Map<K, V> replacedElements;

    @SuppressWarnings("unchecked")
    public RecordingEntrySetReplacingIterator(RecordingMap<? extends Map<K, V>, K, V> recordingMap) {
        this.recordingMap = (RecordingMap<Map<K, V>, K, V>) recordingMap;
        this.iterator = (Iterator<Map.Entry<K, V>>) (Iterator<?>) recordingMap.delegate.entrySet().iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Map.Entry<K, V> next() {
        // If replaced elements are set, we are in "replace mode" and remove all elements to retain the same order
        if (replacedElements != null) {
            Map.Entry<K, V> entry = iterator.next();
            current = entry.getKey();
            replacedElements.put(entry.getKey(), entry.getValue());
            iterator.remove();
            return entry;
        } else {
            Map.Entry<K, V> entry = iterator.next();
            current = entry.getKey();
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
            V old = recordingMap.get(current);
            iterator.remove();
            current = null;
            return old;
        } else {
            // Remove the last element that was added via next() as it will be replaced
            return replacedElements.remove(current);
        }
    }

    public void replaceValue(Set<Object> removedKeys) {
        if (replacedElements == null) {
            replacedElements = new LinkedHashMap<>();
            removedKeys.add(current);
            iterator.remove();
            current = null;
        } else {
            // Remove the last element that was added via next() as it will be replaced
            removedKeys.add(current);
            replacedElements.remove(current);
        }
    }

    public void reset() {
        // Re-add the elements in the appropriate order
        if (replacedElements != null) {
            recordingMap.getDelegate().putAll(replacedElements);
        }
    }

    public void add(K key, V value) {
        if (replacedElements != null) {
            replacedElements.put(key, value);
        }
    }
}
