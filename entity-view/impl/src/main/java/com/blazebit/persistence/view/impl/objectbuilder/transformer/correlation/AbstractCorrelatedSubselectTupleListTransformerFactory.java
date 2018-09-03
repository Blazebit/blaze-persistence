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
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformerFactory;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedSubselectTupleListTransformerFactory implements TupleListTransformerFactory {

    protected final Correlator correlator;
    protected final EntityViewManagerImpl evm;
    protected final ManagedViewType<?> viewRootType;
    protected final ManagedViewType<?> embeddingViewType;
    protected final String viewRootAlias;
    protected final String embeddingViewPath;
    protected final String correlationResult;
    protected final String correlationBasisExpression;
    protected final String correlationKeyExpression;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final String attributePath;
    protected final String[] fetches;
    protected final int viewRootIndex;
    protected final int embeddingViewIndex;
    protected final int correlationBasisIndex;
    protected final Class<?> correlationBasisType;
    protected final Class<?> correlationBasisEntity;

    public AbstractCorrelatedSubselectTupleListTransformerFactory(Correlator correlator, EntityViewManagerImpl evm, ManagedViewType<?> viewRootType, String viewRootAlias, ManagedViewType<?> embeddingViewType, String embeddingViewPath, String correlationResult, String correlationBasisExpression, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory,
                                                                  String attributePath, String[] fetches, int viewRootIndex, int embeddingViewIndex, int correlationBasisIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity) {
        this.correlator = correlator;
        this.evm = evm;
        this.viewRootType = viewRootType;
        this.embeddingViewType = embeddingViewType;
        this.viewRootAlias = viewRootAlias;
        this.embeddingViewPath = embeddingViewPath;
        this.correlationResult = correlationResult;
        this.correlationBasisExpression = correlationBasisExpression;
        this.correlationKeyExpression = correlationKeyExpression;
        this.correlationProviderFactory = correlationProviderFactory;
        this.attributePath = attributePath;
        this.fetches = fetches;
        this.viewRootIndex = viewRootIndex;
        this.embeddingViewIndex = embeddingViewIndex;
        this.correlationBasisIndex = correlationBasisIndex;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
    }

}
