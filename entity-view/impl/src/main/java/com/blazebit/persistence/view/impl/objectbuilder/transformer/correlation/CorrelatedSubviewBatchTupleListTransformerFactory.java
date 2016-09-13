/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSubviewBatchTupleListTransformerFactory<T> extends AbstractCorrelatedSubviewBatchTupleListTransformerFactory<T> {

    public CorrelatedSubviewBatchTupleListTransformerFactory(ManagedViewType<T> managedViewType, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, int tupleIndex, int batchSize, Class<?> correlationBasisEntity, EntityViewManagerImpl evm, ExpressionFactory ef, String viewName) {
        super(managedViewType, viewRootType, correlationResult, correlationProviderFactory, attributePath, tupleIndex, batchSize, correlationBasisEntity, evm, ef, viewName);
    }

    @Override
    public TupleListTransformer create(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        String paramName = generateCorrelationParamName(queryBuilder, optionalParameters);
        int batchSize = getBatchSize(entityViewConfiguration);
        Map.Entry<CriteriaBuilder<T>, CorrelatedSubqueryViewRootJpqlMacro> entry = createCriteriaBuilder(queryBuilder, optionalParameters, entityViewConfiguration, batchSize, paramName);
        return new CorrelatedBasicTupleListTransformer(entry.getKey(), entry.getValue(), paramName, tupleIndex, batchSize, correlationBasisEntity);
    }

}
