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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingReplacingIterator<E> implements Iterator<E> {

    private final RecordingCollection<?, E> recordingCollection;
    private final Iterator<E> iterator;
    private E current;
    private List<E> replacedElements;

    public RecordingReplacingIterator(RecordingCollection<?, E> recordingCollection) {
        this.recordingCollection = recordingCollection;
        this.iterator = recordingCollection.delegate.iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public E next() {
        // If replaced elements are set, we are in "replace mode" and remove all elements to retain the same order
        if (replacedElements != null) {
            current = iterator.next();
            replacedElements.add(current);
            iterator.remove();
            return current;
        } else {
            return current = iterator.next();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void replace() {
        // Starting the replace mode is only required once as next() will remove upcoming elements
        if (replacedElements == null) {
            replacedElements = new ArrayList<>();
            iterator.remove();
        } else {
            // Remove the last element that was added via next() as it will be replaced
            replacedElements.remove(replacedElements.size() - 1);
        }
    }

    public void reset() {
        // Re-add the elements in the appropriate order
        if (replacedElements != null) {
            Collection<E> delegate = recordingCollection.getDelegate();
            // We re-add the last element if it wasn't re-added properly
            // This can happen when an exception happens during flushing
            if (current != null && (replacedElements.isEmpty() || current != replacedElements.get(replacedElements.size() - 1))) {
                delegate.add(current);
            }
            delegate.addAll(replacedElements);
        }
    }

    public void add(E object) {
        if (replacedElements != null) {
            replacedElements.add(object);
            current = null;
        }
    }
}
