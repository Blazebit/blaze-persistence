/*
 * Copyright 2014 - 2024 Blazebit.
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
