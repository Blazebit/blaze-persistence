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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MultiCollection<E> extends AbstractCollection<E> {

    private final Collection<E>[] collections;

    public MultiCollection(Collection<E>... collections) {
        this.collections = collections;
    }

    @Override
    public int size() {
        int size = 0;
        for (Collection<E> c : collections) {
            size += c.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (Collection<E> c : collections) {
            if (!c.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean contains(Object o) {
        for (Collection<E> c : collections) {
            if (c.contains(o)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int cursor = -1;
            Iterator<E> iter;

            @Override
            public boolean hasNext() {
                if (iter != null) {
                    if (iter.hasNext()) {
                        return true;
                    }
                }
                for (int i = cursor + 1; i < collections.length; i++) {
                    if (!collections[i].isEmpty()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public E next() {
                if (iter != null) {
                    if (iter.hasNext()) {
                        return iter.next();
                    }
                }

                for (int i = ++cursor; i < collections.length; i++) {
                    if (!collections[i].isEmpty()) {
                        iter = collections[i].iterator();
                        return iter.next();
                    }
                }

                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
