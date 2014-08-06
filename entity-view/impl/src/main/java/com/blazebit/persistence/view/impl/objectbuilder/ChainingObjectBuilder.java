/*
 * Copyright 2014 Blazebit.
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
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian
 */
public class ChainingObjectBuilder<T> implements ObjectBuilder<T>{
    
    private final TupleTransformator transformator;
    private final ObjectBuilder<T> objectBuilder;

    public ChainingObjectBuilder(TupleTransformator transformator, ObjectBuilder<T> objectBuilder, QueryBuilder<?, ?> queryBuilder, int startIndex) {
        this.transformator = transformator;
        this.objectBuilder = objectBuilder;
        transformator.init(queryBuilder);
    }

    @Override
    public void applySelects(SelectBuilder<?, ?> queryBuilder) {
        objectBuilder.applySelects(queryBuilder);
    }

    @Override
    public T build(Object[] tuple) {
        return (T) tuple;
    }

    @Override
    public List<T> buildList(List<T> list) {
        List<Object[]> currentTuples = transformator.transformAll((List<Object[]>) list);
        List<T> resultList = new ArrayList<T>(currentTuples.size());
        for (Object[] tuple : currentTuples) {
            resultList.add(objectBuilder.build(tuple));
        }
        return objectBuilder.buildList(resultList);
    }
}
