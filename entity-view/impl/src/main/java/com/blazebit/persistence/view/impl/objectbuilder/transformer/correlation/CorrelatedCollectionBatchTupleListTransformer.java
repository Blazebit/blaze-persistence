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
public class CorrelatedCollectionBatchTupleListTransformer extends AbstractCorrelatedBatchTupleListTransformer {

    private final CollectionInstantiator collectionInstantiator;
    private final boolean filterNulls;
    private final boolean recording;

    public CorrelatedCollectionBatchTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewType<?> viewRootType, ManagedViewType<?> embeddingViewType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, boolean correlatesThis,
                                                         int viewRootIndex, int embeddingViewIndex, int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration, CollectionInstantiator collectionInstantiator, boolean filterNulls, boolean recording) {
        super(ef, correlator, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, correlatesThis, viewRootIndex, embeddingViewIndex, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
        this.collectionInstantiator = collectionInstantiator;
        this.filterNulls = filterNulls;
        this.recording = recording;
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
            Collection<Object> result = collections.get(element[KEY_INDEX]);
            if (result == null) {
                result = (Collection<Object>) createDefaultResult();
                collections.put(element[KEY_INDEX], result);
            }

            if (element[VALUE_INDEX] != null) {
                add(result, element[VALUE_INDEX]);
            }
        }

        for (Map.Entry<Object, Collection<Object>> entry : collections.entrySet()) {
            correlationValues.get(entry.getKey()).onResult(postConstruct(entry.getValue()), this);
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
    protected boolean isRecording() {
        return recording;
    }

    @Override
    protected boolean isFilterNulls() {
        return filterNulls;
    }

    @Override
    protected CollectionInstantiator getCollectionInstantiator() {
        return collectionInstantiator;
    }
}
