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
 * @since 1.5.0
 */
public class PreProcessingObjectBuilder<T> implements ObjectBuilder<T> {

    private final ObjectBuilder<Object[]> preProcessor;
    private final ObjectBuilder<T> objectBuilder;

    public PreProcessingObjectBuilder(ObjectBuilder<Object[]> preProcessor, ObjectBuilder<T> objectBuilder) {
        this.preProcessor = preProcessor;
        this.objectBuilder = objectBuilder;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X selectBuilder) {
        objectBuilder.applySelects(selectBuilder);
    }

    @Override
    public T build(Object[] tuple) {
        return objectBuilder.build(preProcessor.build(tuple));
    }

    @Override
    public List<T> buildList(List<T> list) {
        return objectBuilder.buildList(list);
    }
}
