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

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformator;
import com.blazebit.persistence.view.impl.objectbuilder.transformator.TupleTransformatorFactory;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ChainingObjectBuilder<T> implements ObjectBuilder<T> {

    private final TupleTransformator transformator;
    private final ObjectBuilder<T> objectBuilder;

    public ChainingObjectBuilder(TupleTransformatorFactory transformatorFactory, ObjectBuilder<T> objectBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration, int startIndex) {
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
