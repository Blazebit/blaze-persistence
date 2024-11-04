/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import org.hibernate.ScrollableResults;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Iterator over {@code ScrollableResults}.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
public class ScrollableResultsIterator implements Iterator<Object>, Closeable {

    private final ScrollableResults scrollableResults;
    private Object[] next;

    public ScrollableResultsIterator(ScrollableResults scrollableResults) {
        this.scrollableResults = scrollableResults;
    }

    @Override
    public boolean hasNext() {
        return next != null || (scrollableResults.next() && (next = scrollableResults.get()) != null);
    }

    @Override
    public Object next() {
        Object[] next = this.next;
        this.next = null;
        if (next.length == 1) {
            return next[0];
        } else {
            return next;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public void close() {
        scrollableResults.close();
    }

}
