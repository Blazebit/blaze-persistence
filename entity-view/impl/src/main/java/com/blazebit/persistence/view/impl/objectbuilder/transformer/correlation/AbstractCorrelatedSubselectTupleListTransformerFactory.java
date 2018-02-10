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
public abstract class AbstractCorrelatedSubselectTupleListTransformerFactory implements TupleListTransformerFactory {

    protected final Correlator correlator;
    protected final ManagedViewType<?> viewRootType;
    protected final String viewRootAlias;
    protected final String correlationResult;
    protected final String correlationKeyExpression;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final String attributePath;
    protected final String[] fetches;
    protected final int tupleIndex;
    protected final Class<?> correlationBasisType;
    protected final Class<?> correlationBasisEntity;

    public AbstractCorrelatedSubselectTupleListTransformerFactory(Correlator correlator, ManagedViewType<?> viewRootType, String viewRootAlias, String correlationResult, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                                  int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity) {
        this.correlator = correlator;
        this.viewRootType = viewRootType;
        this.viewRootAlias = viewRootAlias;
        this.correlationResult = correlationResult;
        this.correlationKeyExpression = correlationKeyExpression;
        this.correlationProviderFactory = correlationProviderFactory;
        this.attributePath = attributePath;
        this.fetches = fetches;
        this.tupleIndex = tupleIndex;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
    }

}
