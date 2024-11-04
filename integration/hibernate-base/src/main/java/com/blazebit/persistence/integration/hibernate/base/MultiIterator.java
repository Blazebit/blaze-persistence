/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MultiIterator<T> implements Iterator<T> {

    private final List<Iterator<T>> iterators;
    private Iterator<T> current;
    private int nextIndex;

    public MultiIterator(List<Iterator<T>> iterators) {
        this.iterators = iterators;
        this.current = iterators.get(0);
        this.nextIndex = 1;
    }

    @Override
    public boolean hasNext() {
        if (current != null && current.hasNext()) {
            return true;
        }
        if (nextIndex >= iterators.size()) {
            return false;
        }

        current = iterators.get(nextIndex);
        nextIndex++;
        return hasNext();
    }

    @Override
    public T next() {
        if (current != null && current.hasNext()) {
            return current.next();
        }
        if (nextIndex >= iterators.size()) {
            throw new NoSuchElementException();
        }

        current = iterators.get(nextIndex);
        nextIndex++;
        return next();
    }

    @Override
    public void remove() {
        if (current == null) {
            throw new IllegalStateException();
        }

        current.remove();
    }
}
