/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingEntrySetIterator<K, V> implements Iterator<Map.Entry<K, V>> {

    private final RecordingMap<Map<K, V>, K, V> recordingMap;
    private final Iterator<Map.Entry<K, V>> iterator;
    private Object current;

    @SuppressWarnings("unchecked")
    public RecordingEntrySetIterator(RecordingMap<? extends Map<K, V>, K, V> recordingMap) {
        this.recordingMap = (RecordingMap<Map<K, V>, K, V>) recordingMap;
        this.iterator = (Iterator<Map.Entry<K, V>>) (Iterator<?>) recordingMap.delegate.entrySet().iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Map.Entry<K, V> next() {
        Map.Entry<K, V> entry = iterator.next();
        current = entry.getKey();
        return entry;
    }

    public void remove() {
        if (current == null) {
            throw new IllegalStateException();
        }

        recordingMap.addRemoveAction(current);
        iterator.remove();
        current = null;
    }
}
