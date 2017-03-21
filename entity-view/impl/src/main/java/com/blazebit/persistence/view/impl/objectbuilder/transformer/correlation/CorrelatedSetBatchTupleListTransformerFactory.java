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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSetBatchTupleListTransformerFactory extends AbstractCorrelatedBatchTupleListTransformerFactory {

    public CorrelatedSetBatchTupleListTransformerFactory(Correlator correlator, Class<?> criteriaBuilderResult, ManagedViewType<?> viewRoot, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity) {
        super(correlator, criteriaBuilderResult, viewRoot, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity);
    }

    @Override
    public TupleListTransformer create(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return new CorrelatedSetBatchTupleListTransformer(correlator, criteriaBuilderRoot, viewRootType, correlationResult, correlationProviderFactory, attributePath, fetches, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
    }

}
