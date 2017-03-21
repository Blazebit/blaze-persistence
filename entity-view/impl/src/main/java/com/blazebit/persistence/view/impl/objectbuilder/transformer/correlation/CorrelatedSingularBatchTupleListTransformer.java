/*
 * Copyright 2014 - 2017 Blazebit.
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
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import javax.persistence.NonUniqueResultException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSingularBatchTupleListTransformer extends AbstractCorrelatedBatchTupleListTransformer {

    public CorrelatedSingularBatchTupleListTransformer(Correlator correlator, Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                       int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, EntityViewConfiguration entityViewConfiguration) {
        super(correlator, criteriaBuilderRoot, viewRootType, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
    }

    @Override
    protected void populateResult(Map<Object, TuplePromise> correlationValues, Object defaultKey, List<Object> list) {
        if (batchSize == 1) {
            switch (list.size()) {
                case 0:
                    correlationValues.get(defaultKey).onResult(null, this);
                    return;
                case 1:
                    correlationValues.get(defaultKey).onResult(list.get(0), this);
                    return;
                default:
                    throw new NonUniqueResultException("Expected a single result for subquery!");
            }
        } else {
            for (Object[] element : (List<Object[]>) (List<?>) list) {
                correlationValues.get(element[0]).onResult(element[1], this);
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
