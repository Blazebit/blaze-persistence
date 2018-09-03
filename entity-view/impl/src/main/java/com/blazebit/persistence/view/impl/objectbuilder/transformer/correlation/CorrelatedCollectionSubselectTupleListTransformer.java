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

import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Collection;
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
    private final boolean recording;

    public CorrelatedCollectionSubselectTupleListTransformer(ExpressionFactory ef, Correlator correlator, EntityViewManagerImpl evm, ManagedViewType<?> viewRootType, String viewRootAlias, ManagedViewType<?> embeddingViewType, String embeddingViewPath, String correlationResult, String correlationBasisExpression, String correlationKeyExpression,
                                                             CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration, CollectionInstantiator collectionInstantiator,
                                                             boolean filterNulls, boolean recording) {
        super(ef, correlator, evm, viewRootType, viewRootAlias, embeddingViewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, viewRootIndex, embeddingViewIndex, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.collectionInstantiator = collectionInstantiator;
        this.filterNulls = filterNulls;
        this.recording = recording;
    }

    @Override
    protected void populateResult(Map<Object, Map<Object, TuplePromise>> correlationValues, List<Object[]> list) {
        Map<Object, Map<Object, Collection<Object>>> collections;
        collections = new HashMap<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object[] element = list.get(i);
            Map<Object, Collection<Object>> viewRootResult = collections.get(element[VIEW_INDEX]);
            if (viewRootResult == null) {
                viewRootResult = new HashMap<>();
                collections.put(element[VIEW_INDEX], viewRootResult);
            }
            Collection<Object> result = viewRootResult.get(element[keyIndex]);
            if (result == null) {
                result = (Collection<Object>) createDefaultResult();
                viewRootResult.put(element[keyIndex], result);
            }

            if (element[VALUE_INDEX] != null) {
                add(result, element[VALUE_INDEX]);
            }
        }

        for (Map.Entry<Object, Map<Object, Collection<Object>>> entry : collections.entrySet()) {
            Map<Object, TuplePromise> tuplePromiseMap = correlationValues.get(entry.getKey());
            for (Map.Entry<Object, Collection<Object>> correlationEntry : entry.getValue().entrySet()) {
                tuplePromiseMap.get(correlationEntry.getKey()).onResult(correlationEntry.getValue(), this);
            }
        }
    }

    private void add(Collection<Object> result, Object o) {
        if (recording) {
            ((RecordingCollection<?, Object>) result).getDelegate().add(o);
        } else {
            result.add(o);
        }
    }

    @Override
    public Object copy(Object o) {
        return createCollection((Collection<? extends Object>) o);
    }

    @Override
    protected Object createDefaultResult() {
        if (recording) {
            return collectionInstantiator.createRecordingCollection(0);
        } else {
            return collectionInstantiator.createCollection(0);
        }
    }

    protected Collection<Object> createCollection(Collection<? extends Object> list) {
        Collection<Object> result;
        Collection<Object> collection;
        if (recording) {
            RecordingCollection<?, ?> recordingCollection = collectionInstantiator.createRecordingCollection(list.size());
            collection = (Collection<Object>) recordingCollection.getDelegate();
            result = (Collection<Object>) recordingCollection;
        } else {
            result = collection = (Collection<Object>) collectionInstantiator.createCollection(list.size());
        }
        if (filterNulls) {
            for (Object o : list) {
                if (o != null) {
                    collection.add(o);
                }
            }
        } else {
            collection.addAll(list);
        }
        return result;
    }

}
