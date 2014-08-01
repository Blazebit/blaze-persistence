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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian
 */
public class ChainingObjectBuilder<T> implements ObjectBuilder<T>{
    
    private final TupleTransformer[] tupleTransformers;
    private final TupleListTransformer[] tupleListTransformers;
    private final ObjectBuilder<T> objectBuilder;

    public ChainingObjectBuilder(List<TupleTransformer> tupleTransformers, List<TupleListTransformer> tupleListTransformers, ObjectBuilder<T> objectBuilder, QueryBuilder<?, ?> queryBuilder, int startIndex) {
        this.tupleTransformers = new TupleTransformer[tupleTransformers.size()];
        this.tupleListTransformers = new TupleListTransformer[tupleListTransformers.size()];
        this.objectBuilder = objectBuilder;
        
        for (int i = 0; i < this.tupleTransformers.length; i++) {
            this.tupleTransformers[i] = tupleTransformers.get(i).init(queryBuilder);
        }
        
        for (int i = 0; i < this.tupleListTransformers.length; i++) {
            this.tupleListTransformers[i] = tupleListTransformers.get(i).init(queryBuilder);
        }
    }

    @Override
    public void applySelects(QueryBuilder<?, ?> queryBuilder) {
        objectBuilder.applySelects(queryBuilder);
    }

    @Override
    public T build(Object[] tuple, String[] aliases) {
        Object[] currentTuple = tuple;
        for (TupleTransformer t : tupleTransformers) {
            currentTuple = t.transform(currentTuple);
        }
        return (T) currentTuple;
    }

    @Override
    public List<T> buildList(List<T> list) {
        List<Object[]> currentTuples = (List<Object[]>) list;
        for (TupleListTransformer t : tupleListTransformers) {
            currentTuples = t.transform(currentTuples);
        }
        List<T> resultList = new ArrayList<T>(currentTuples.size());
        for (Object[] tuple : currentTuples) {
            resultList.add(objectBuilder.build(tuple, null));
        }
        return objectBuilder.buildList(resultList);
    }
}
