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

import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedCollectionBatchTupleListTransformerFactory extends AbstractCorrelatedBatchTupleListTransformerFactory {

    private final CollectionInstantiator collectionInstantiator;
    private final boolean dirtyTracking;

    public CorrelatedCollectionBatchTupleListTransformerFactory(Correlator correlator, ManagedViewType<?> viewRoot, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                                int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, CollectionInstantiator collectionInstantiator, boolean dirtyTracking) {
        super(correlator, viewRoot, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity);
        this.collectionInstantiator = collectionInstantiator;
        this.dirtyTracking = dirtyTracking;
    }

    @Override
    public TupleListTransformer create(Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return new CorrelatedCollectionBatchTupleListTransformer(entityViewConfiguration.getExpressionFactory(), correlator, viewRootType, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, entityViewConfiguration, collectionInstantiator, dirtyTracking);
    }

}
