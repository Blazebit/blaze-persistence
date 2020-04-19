/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedCollectionBatchTupleListTransformerFactory extends AbstractCorrelatedBatchTupleListTransformerFactory {

    private final CollectionInstantiatorImplementor<?, ?> collectionInstantiator;
    private final boolean filterNulls;
    private final boolean recording;

    public CorrelatedCollectionBatchTupleListTransformerFactory(Correlator correlator, ManagedViewType<?> viewRoot, ManagedViewType<?> embeddingViewType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                                boolean correlatesThis, int viewRootIndex, int embeddingViewIndex, int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter, CollectionInstantiatorImplementor<?, ?> collectionInstantiator, boolean filterNulls, boolean recording) {
        super(correlator, viewRoot, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, correlatesThis, viewRootIndex, embeddingViewIndex, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, limiter);
        this.collectionInstantiator = collectionInstantiator;
        this.filterNulls = filterNulls;
        this.recording = recording;
    }

    @Override
    public TupleListTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration config) {
        return new CorrelatedCollectionBatchTupleListTransformer(config.getExpressionFactory(), correlator, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, correlatesThis, viewRootIndex, embeddingViewIndex, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, limiter, config, collectionInstantiator, filterNulls, recording);
    }

}
