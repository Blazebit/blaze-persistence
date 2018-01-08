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

import java.util.ListIterator;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingListIterator<E> implements ListIterator<E> {

    private final RecordingList<E> recordingList;
    private final ListIterator<E> iterator;

    public RecordingListIterator(RecordingList<E> recordingList, int index) {
        this.recordingList = recordingList;
        this.iterator = recordingList.delegate.listIterator(index);
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public E next() {
        return iterator.next();
    }

    public void remove() {
        int idx = iterator.previousIndex();
        recordingList.addRemoveAction(idx);
        iterator.remove();
    }

    @Override
    public void set(E e) {
        int idx = iterator.previousIndex();
        recordingList.addSetAction(idx, e);
        iterator.set(e);
    }

    @Override
    public void add(E e) {
        int idx = iterator.nextIndex();
        recordingList.addAddAction(idx, e);
        iterator.add(e);
    }

    /**************
     * Read-only
     *************/
    
    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    @Override
    public E previous() {
        return iterator.previous();
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

}
