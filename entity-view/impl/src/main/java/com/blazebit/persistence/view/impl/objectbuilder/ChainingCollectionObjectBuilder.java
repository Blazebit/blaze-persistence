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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.6.4
 */
public class ChainingCollectionObjectBuilder<T> implements ObjectBuilder<T> {

    private final TupleTransformator transformator;
    private final ObjectBuilder<T> objectBuilder;

    public ChainingCollectionObjectBuilder(TupleTransformatorFactory transformatorFactory, ObjectBuilder<T> objectBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        this.transformator = transformatorFactory.create(parameterHolder, optionalParameters, entityViewConfiguration);
        this.objectBuilder = objectBuilder;
    }

    @Override
    public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
        objectBuilder.applySelects(queryBuilder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T build(Object[] tuple) {
        return (T) tuple;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> buildList(List<T> list) {
        List<Object[]> currentTuples = transformator.transformAll((List<Object[]>) list);
        List<T> resultList = new ArrayList<T>(currentTuples.size());
        for (Object[] tuple : currentTuples) {
            resultList.add(objectBuilder.build(tuple));
        }
        return objectBuilder.buildList(resultList);
    }
}
