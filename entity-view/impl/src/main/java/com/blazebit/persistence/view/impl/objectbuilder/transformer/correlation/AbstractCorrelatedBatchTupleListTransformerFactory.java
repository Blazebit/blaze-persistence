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
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformerFactory;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedBatchTupleListTransformerFactory implements TupleListTransformerFactory {

    protected final Correlator correlator;
    protected final ManagedViewType<?> viewRootType;
    protected final ManagedViewType<?> embeddingViewType;
    protected final String correlationResult;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final String attributePath;
    protected final String[] fetches;
    protected final boolean correlatesThis;
    protected final int batchSize;
    protected final int viewRootIndex;
    protected final int embeddingViewIndex;
    protected final int tupleIndex;
    protected final Class<?> correlationBasisType;
    protected final Class<?> correlationBasisEntity;

    public AbstractCorrelatedBatchTupleListTransformerFactory(Correlator correlator, ManagedViewType<?> viewRootType, ManagedViewType<?> embeddingViewType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                              boolean correlatesThis, int viewRootIndex, int embeddingViewIndex, int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity) {
        this.correlator = correlator;
        this.viewRootType = viewRootType;
        this.embeddingViewType = embeddingViewType;
        this.correlationResult = correlationResult;
        this.correlationProviderFactory = correlationProviderFactory;
        this.correlatesThis = correlatesThis;
        this.viewRootIndex = viewRootIndex;
        this.embeddingViewIndex = embeddingViewIndex;
        this.tupleIndex = tupleIndex;
        this.batchSize = batchSize;
        this.attributePath = attributePath;
        this.fetches = fetches;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
    }

}
