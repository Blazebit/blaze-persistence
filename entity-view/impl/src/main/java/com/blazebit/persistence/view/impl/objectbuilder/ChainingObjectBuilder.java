/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformator;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ChainingObjectBuilder<T> implements ObjectBuilder<T> {

    private final TupleTransformator transformator;
    private final ObjectBuilder<T> objectBuilder;

    public ChainingObjectBuilder(TupleTransformatorFactory transformatorFactory, ObjectBuilder<T> objectBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        this.transformator = transformatorFactory.create(parameterHolder, optionalParameters, entityViewConfiguration);
        this.objectBuilder = objectBuilder;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        objectBuilder.applySelects(queryBuilder);
    }

    @Override
    public T build(Object[] tuple) {
        return objectBuilder.build(transformator.transform(tuple));
    }

    @Override
    public List<T> buildList(List<T> list) {
        return objectBuilder.buildList(list);
    }
}
