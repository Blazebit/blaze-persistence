/*
 * Copyright 2014 - 2022 Blazebit.
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
