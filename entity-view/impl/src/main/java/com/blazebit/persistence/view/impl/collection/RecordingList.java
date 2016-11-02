/*
 * Copyright 2014 - 2016 Blazebit.
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
import java.util.List;
import java.util.ListIterator;

public class RecordingList<E> extends RecordingCollection<List<E>, E> implements List<E> {

    public RecordingList(List<E> delegate) {
        super(delegate);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        actions.add(new ListAddAllAction<List<E>, E>(index, c));
        return delegate.addAll(index, c);
    }

    @Override
    public E set(int index, E element) {
        actions.add(new ListSetAction<List<E>, E>(index, element));
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        actions.add(new ListAddAction<List<E>, E>(index, element));
        delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        actions.add(new ListRemoveAction<List<E>, E>(index));
        return delegate.remove(index);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new RecordingListIterator<E>(delegate.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new RecordingListIterator<E>(delegate.listIterator(index));
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // TODO: implement
        throw new UnsupportedOperationException("Sublists for entity view collections are not yet supported!");
    }

    
    /**************
     * Read-only
     *************/

    @Override
    public E get(int index) {
        return delegate.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

}
