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

import java.util.AbstractList;
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

    public RecordingList(List<E> delegate, boolean indexed, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        super(delegate, indexed, indexed, allowedSubtypes, updatable, optimize);
    }

    void addAddAllAction(int index, Collection<? extends E> c) {
        if (indexed) {
            addAction(new ListAddAllAction<List<E>, E>(index, c));
        } else {
            addAddAllAction(c);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkType(c, "Adding");
        addAddAllAction(index, c);
        return delegate.addAll(index, c);
    }

    void addSetAction(int index, E element) {
        if (indexed) {
            addAction(new ListSetAction<List<E>, E>(index, element, delegate));
        } else {
            addRemoveAction(index);
            addAddAction(element);
        }
    }

    @Override
    public E set(int index, E element) {
        checkType(element, "Setting");
        addSetAction(index, element);
        return delegate.set(index, element);
    }

    void addAddAction(int index, E element) {
        if (indexed) {
            addAction(new ListAddAction<List<E>, E>(index, element));
        } else {
            addAddAction(element);
        }
    }

    @Override
    public void add(int index, E element) {
        checkType(element, "Adding");
        addAddAction(index, element);
        delegate.add(index, element);
    }

    void addRemoveAction(int index) {
        if (indexed) {
            addAction(new ListRemoveAction<List<E>, E>(index, delegate));
        } else {
            addRemoveAction(delegate.get(index));
        }
    }

    @Override
    public E remove(int index) {
        addRemoveAction(index);
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


    /**************
     * Read-only
     *************/

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        final List<E> subDelegate = delegate.subList(fromIndex, toIndex);
        return new AbstractList<E>() {

            @Override
            public E get(int index) {
                return subDelegate.get(index);
            }

            @Override
            public boolean contains(Object o) {
                return subDelegate.contains(o);
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return subDelegate.containsAll(c);
            }

            @Override
            public int indexOf(Object o) {
                return subDelegate.indexOf(o);
            }

            @Override
            public int lastIndexOf(Object o) {
                return subDelegate.lastIndexOf(o);
            }

            @Override
            public int size() {
                return subDelegate.size();
            }
            @Override
            public Object[] toArray() {
                return subDelegate.toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return subDelegate.toArray(a);
            }
        };
    }

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
