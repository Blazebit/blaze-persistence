/*
 * Copyright 2014 - 2017 Blazebit.
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
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingKeySetIterator<E> implements Iterator<E> {

    private final RecordingMap<Map<E, ?>, E, ?> recordingMap;
    private final Iterator<Map.Entry<E, ?>> iterator;
    private E current;

    @SuppressWarnings("unchecked")
    public RecordingKeySetIterator(RecordingMap<? extends Map<E, ?>, E, ?> recordingMap) {
        this.recordingMap = (RecordingMap<Map<E, ?>, E, ?>) recordingMap;
        this.iterator = (Iterator<Map.Entry<E, ?>>) (Iterator<?>) recordingMap.delegate.entrySet().iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public E next() {
        return current = iterator.next().getKey();
    }

    public void remove() {
        if (current == null) {
            throw new IllegalStateException();
        }

        iterator.remove();
        recordingMap.addRemoveAction(current);
        current = null;
    }
}
