/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.List;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DelegatingObjectBuilder<T> implements ObjectBuilder<T> {

    protected final ObjectBuilder<T> delegate;

    public DelegatingObjectBuilder(ObjectBuilder<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        delegate.applySelects(queryBuilder);
    }

    @Override
    public T build(Object[] tuple) {
        return delegate.build(tuple);
    }

    @Override
    public List<T> buildList(List<T> list) {
        return delegate.buildList(list);
    }
}
