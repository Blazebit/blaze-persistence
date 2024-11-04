/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
