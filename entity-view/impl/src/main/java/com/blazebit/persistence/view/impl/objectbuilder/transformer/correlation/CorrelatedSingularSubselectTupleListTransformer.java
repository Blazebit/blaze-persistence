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
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSingularSubselectTupleListTransformer extends AbstractCorrelatedSubselectTupleListTransformer {

    public CorrelatedSingularSubselectTupleListTransformer(ExpressionFactory ef, Correlator correlator, EntityViewManagerImpl evm, ManagedViewType<?> viewRootType, String viewRootAlias, ManagedViewType<?> embeddingViewType, String embeddingViewPath, String correlationResult, String correlationBasisExpression, String correlationKeyExpression,
                                                           CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, evm, viewRootType, viewRootAlias, embeddingViewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, viewRootIndex, embeddingViewIndex, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
    }

    @Override
    protected void populateResult(Map<Object, Map<Object, TuplePromise>> correlationValues, List<Object[]> list) {
        for (Object[] element : (List<Object[]>) (List<?>) list) {
            correlationValues.get(element[VIEW_INDEX]).get(element[keyIndex]).onResult(element[VALUE_INDEX], this);
        }
    }

    @Override
    public Object copy(Object o) {
        // Nothing to copy here
        return o;
    }

    @Override
    protected Object createDefaultResult() {
        return null;
    }
}
