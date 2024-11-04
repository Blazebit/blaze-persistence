/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.object;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CountExtractionObjectBuilder<T> implements ObjectBuilder<T> {

    private final ObjectBuilder<T> delegate;
    private long count = -1;

    public CountExtractionObjectBuilder(ObjectBuilder<T> delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T build(Object[] tuple) {
        count = (long) tuple[tuple.length - 1];

        Object[] newTuple = new Object[tuple.length - 1];
        System.arraycopy(tuple, 0, newTuple, 0, newTuple.length);
        return delegate.build(newTuple);
    }

    public long getCount() {
        return count;
    }

    @Override
    public List<T> buildList(List<T> list) {
        return delegate.buildList(list);
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        delegate.applySelects(queryBuilder);
    }

}
