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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedCollectionSubselectTupleListTransformer extends AbstractCorrelatedSubselectTupleListTransformer {

    private final CollectionInstantiator collectionInstantiator;
    private final boolean filterNulls;

    public CorrelatedCollectionSubselectTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewType<?> viewRootType, String viewRootAlias, String correlationResult, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int tupleIndex, Class<?> correlationBasisType,
                                                             Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration, CollectionInstantiator collectionInstantiator, boolean filterNulls) {
        super(ef, correlator, viewRootType, viewRootAlias, correlationResult, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.collectionInstantiator = collectionInstantiator;
        this.filterNulls = filterNulls;
    }

    @Override
    protected void populateResult(boolean usesViewRoot, Map<Object, Map<Object, TuplePromise>> correlationValues, List<Object[]> list) {
        Map<Object, Map<Object, Collection<Object>>> collections;
        if (usesViewRoot) {
            collections = new HashMap<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                Object[] element = list.get(i);
                Map<Object, Collection<Object>> viewRootResult = collections.get(element[0]);
                if (viewRootResult == null) {
                    viewRootResult = new HashMap<>();
                    collections.put(element[0], viewRootResult);
                }
                Collection<Object> result = viewRootResult.get(element[1]);
                if (result == null) {
                    result = (Collection<Object>) collectionInstantiator.createCollection(0);
                    viewRootResult.put(element[1], result);
                }

                if (element[2] != null) {
                    result.add(element[2]);
                }
            }
        } else {
            Map<Object, Collection<Object>> viewRootResult = new HashMap<>(list.size());
            collections = Collections.singletonMap(null, viewRootResult);
            for (int i = 0; i < list.size(); i++) {
                Object[] element = (Object[]) list.get(i);
                Collection<Object> result = viewRootResult.get(element[0]);
                if (result == null) {
                    result = (Collection<Object>) collectionInstantiator.createCollection(0);
                    viewRootResult.put(element[0], result);
                }

                if (element[1] != null) {
                    result.add(element[1]);
                }
            }
        }

        for (Map.Entry<Object, Map<Object, Collection<Object>>> entry : collections.entrySet()) {
            Map<Object, TuplePromise> tuplePromiseMap = correlationValues.get(entry.getKey());
            for (Map.Entry<Object, Collection<Object>> correlationEntry : entry.getValue().entrySet()) {
                tuplePromiseMap.get(correlationEntry.getKey()).onResult(correlationEntry.getValue(), this);
            }
        }
    }

    @Override
    public Object copy(Object o) {
        return createCollection((Collection<? extends Object>) o);
    }

    @Override
    protected Object createDefaultResult() {
        return collectionInstantiator.createCollection(0);
    }

    protected Collection<Object> createCollection(Collection<? extends Object> list) {
        Collection<Object> result = (Collection<Object>) collectionInstantiator.createCollection(list.size());
        if (filterNulls) {
            for (Object o : list) {
                if (o != null) {
                    result.add(o);
                }
            }
        } else {
            result.addAll(list);
        }
        return result;
    }

}
