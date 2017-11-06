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
import com.blazebit.persistence.impl.expression.ExpressionFactory;
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
public class CorrelatedListSubselectTupleListTransformerFactory extends AbstractCorrelatedSubselectTupleListTransformerFactory {

    public CorrelatedListSubselectTupleListTransformerFactory(Correlator correlator, Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, String viewRootAlias, String correlationResult, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                              int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity) {
        super(correlator, criteriaBuilderRoot, viewRootType, viewRootAlias, correlationResult, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, tupleIndex, correlationBasisType, correlationBasisEntity);
    }

    @Override
    public TupleListTransformer create(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return new CorrelatedListSubselectTupleListTransformer(queryBuilder.getService(ExpressionFactory.class), correlator, criteriaBuilderRoot, viewRootType, viewRootAlias, correlationResult, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration);
    }

}
