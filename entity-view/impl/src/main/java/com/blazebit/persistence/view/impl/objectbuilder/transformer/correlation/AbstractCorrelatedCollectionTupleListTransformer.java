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
package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;

import java.util.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedCollectionTupleListTransformer extends AbstractCorrelatedTupleListTransformer {

    public AbstractCorrelatedCollectionTupleListTransformer(CriteriaBuilder<?> criteriaBuilder, CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro, String correlationParamName, int tupleIndex, int batchSize, Class<?> correlationBasisEntity) {
        super(criteriaBuilder, viewRootJpqlMacro, correlationParamName, tupleIndex, batchSize, correlationBasisEntity);
    }

    @Override
    protected void populateResult(Map<Object, TuplePromise> correlationValues, Object defaultKey, List<Object[]> list) {
        if (batchSize == 1) {
            correlationValues.get(defaultKey).onResult(createCollection(list));
            return;
        }
        Map<Object, Collection<Object>> collections = new HashMap<Object, Collection<Object>>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object[] element = list.get(i);
            Collection<Object> result = collections.get(element[0]);
            if (result == null) {
                result = createCollection(0);
                collections.put(element[0], result);
            }

            result.add(element[1]);
        }

        for (Map.Entry<Object, Collection<Object>> entry : collections.entrySet()) {
            correlationValues.get(entry.getKey()).onResult(entry.getValue());
        }
    }

    protected Collection<Object> createCollection(List<? extends Object> list) {
        Collection<Object> result = createCollection(list.size());
        result.addAll(list);
        return result;
    }

    protected abstract Collection<Object> createCollection(int size);

}
