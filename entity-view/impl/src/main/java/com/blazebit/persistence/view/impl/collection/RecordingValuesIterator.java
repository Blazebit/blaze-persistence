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
public class RecordingValuesIterator<E> implements Iterator<E> {

    private final RecordingMap<Map<?, E>, ?, E> recordingMap;
    private final Iterator<Map.Entry<?, E>> iterator;
    private Object current;

    @SuppressWarnings("unchecked")
    public RecordingValuesIterator(RecordingMap<? extends Map<?, E>, ?, E> recordingMap) {
        this.recordingMap = (RecordingMap<Map<?, E>, ?, E>) recordingMap;
        this.iterator = (Iterator<Map.Entry<?, E>>) (Iterator<?>) recordingMap.delegate.entrySet().iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public E next() {
        Map.Entry<?, E> entry = iterator.next();
        current = entry.getKey();
        return entry.getValue();
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
