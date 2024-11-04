/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import java.util.Iterator;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingIterator<E> implements Iterator<E> {

    private final RecordingCollection<?, E> recordingCollection;
    private final Iterator<E> iterator;
    private E current;
    
    public RecordingIterator(RecordingCollection<?, E> recordingCollection) {
        this.recordingCollection = recordingCollection;
        this.iterator = recordingCollection.delegate.iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public E next() {
        return current = iterator.next();
    }

    public void remove() {
        if (current == null) {
            throw new IllegalStateException();
        }

        recordingCollection.addRemoveAction(current);
        iterator.remove();
        current = null;
    }
}
