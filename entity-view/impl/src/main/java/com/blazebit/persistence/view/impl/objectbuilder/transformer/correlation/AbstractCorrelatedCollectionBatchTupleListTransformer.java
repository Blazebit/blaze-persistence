/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedCollectionBatchTupleListTransformer extends AbstractCorrelatedBatchTupleListTransformer {

    public AbstractCorrelatedCollectionBatchTupleListTransformer(Correlator correlator, Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                                 int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(correlator, criteriaBuilderRoot, viewRootType, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
    }

    @Override
    protected void populateResult(Map<Object, TuplePromise> correlationValues, Object defaultKey, List<Object> list) {
        if (batchSize == 1) {
            correlationValues.get(defaultKey).onResult(createCollection(list), this);
            return;
        }
        Map<Object, Collection<Object>> collections = new HashMap<Object, Collection<Object>>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object[] element = (Object[]) list.get(i);
            Collection<Object> result = collections.get(element[0]);
            if (result == null) {
                result = createCollection(0);
                collections.put(element[0], result);
            }

            if (element[1] != null) {
                result.add(element[1]);
            }
        }

        for (Map.Entry<Object, Collection<Object>> entry : collections.entrySet()) {
            correlationValues.get(entry.getKey()).onResult(entry.getValue(), this);
        }
    }

    @Override
    public Object copy(Object o) {
        return createCollection((Collection<? extends Object>) o);
    }

    @Override
    protected Object createDefaultResult() {
        return createCollection(0);
    }

    protected Collection<Object> createCollection(Collection<? extends Object> list) {
        Collection<Object> result = createCollection(list.size());
        result.addAll(list);
        return result;
    }

    protected abstract Collection<Object> createCollection(int size);

}
