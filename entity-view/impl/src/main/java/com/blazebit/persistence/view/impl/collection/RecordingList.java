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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingList<E> extends RecordingCollection<List<E>, E> implements List<E> {

    public RecordingList(List<E> delegate, Set<Class<?>> allowedSubtypes, boolean updatable) {
        super(delegate, allowedSubtypes, updatable);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkType(c, "Adding");
        addAction(new ListAddAllAction<List<E>, E>(index, c));
        return delegate.addAll(index, c);
    }

    void addSetAction(int index, E element) {
        addAction(new ListSetAction<List<E>, E>(index, element));
    }

    @Override
    public E set(int index, E element) {
        checkType(element, "Setting");
        addSetAction(index, element);
        return delegate.set(index, element);
    }

    void addAddAction(int index, E element) {
        addAction(new ListAddAction<List<E>, E>(index, element));
    }

    @Override
    public void add(int index, E element) {
        checkType(element, "Adding");
        addAddAction(index, element);
        delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        addAction(new ListRemoveAction<List<E>, E>(index));
        return delegate.remove(index);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new RecordingListIterator<E>(this, 0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new RecordingListIterator<E>(this, index);
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
