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
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSingularSubselectTupleListTransformer extends AbstractCorrelatedSubselectTupleListTransformer {

    public CorrelatedSingularSubselectTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewType<?> viewRootType, String viewRootAlias, String correlationResult, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int tupleIndex, Class<?> correlationBasisType,
                                                           Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, viewRootType, viewRootAlias, correlationResult, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
    }

    @Override
    protected void populateResult(boolean usesViewRoot, Map<Object, Map<Object, TuplePromise>> correlationValues, List<Object[]> list) {
        if (usesViewRoot) {
            for (Object[] element : (List<Object[]>) (List<?>) list) {
                correlationValues.get(element[0]).get(element[1]).onResult(element[2], this);
            }
        } else {
            for (Object[] element : (List<Object[]>) (List<?>) list) {
                correlationValues.get(null).get(element[0]).onResult(element[1], this);
            }
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
