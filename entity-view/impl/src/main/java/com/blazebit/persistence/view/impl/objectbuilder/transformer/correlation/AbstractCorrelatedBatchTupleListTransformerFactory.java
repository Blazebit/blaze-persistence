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
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformerFactory;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedBatchTupleListTransformerFactory<T> implements TupleListTransformerFactory {

    private static final String CORRELATION_PARAM_PREFIX = "correlationParam_";

    private final Class<?> criteriaBuilderRoot;
    private final ManagedViewType<?> viewRootType;
    private final String correlationResult;
    private final CorrelationProviderFactory correlationProviderFactory;
    private final String attributePath;
    private final int batchSize;
    protected final int tupleIndex;
    protected final Class<?> correlationBasisEntity;

    public AbstractCorrelatedBatchTupleListTransformerFactory(Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, int tupleIndex, int batchSize, Class<?> correlationBasisEntity) {
        this.criteriaBuilderRoot = criteriaBuilderRoot;
        this.viewRootType = viewRootType;
        this.correlationResult = correlationResult;
        this.correlationProviderFactory = correlationProviderFactory;
        this.tupleIndex = tupleIndex;
        this.batchSize = batchSize;
        this.attributePath = attributePath;
        this.correlationBasisEntity = correlationBasisEntity;
    }

    protected final int getBatchSize(EntityViewConfiguration configuration) {
        return configuration.getBatchSize(attributePath, batchSize);
    }

    protected final Map.Entry<CriteriaBuilder<T>, CorrelatedSubqueryViewRootJpqlMacro> createCriteriaBuilder(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration, int batchSize, String paramName) {
        CorrelationProvider provider = correlationProviderFactory.create(queryBuilder, optionalParameters);
        CriteriaBuilder<?> cb = queryBuilder.getCriteriaBuilderFactory().create(queryBuilder.getEntityManager(), criteriaBuilderRoot);

        String idAttributePath = null;
        ManagedType<?> managedType = queryBuilder.getMetamodel().entity(viewRootType.getEntityClass());
        if (managedType instanceof IdentifiableType<?>) {
            IdentifiableType<?> identifiableType = (IdentifiableType<?>) managedType;
            idAttributePath = identifiableType.getId(identifiableType.getIdType().getJavaType()).getName();
        }

        CorrelatedSubqueryViewRootJpqlMacro macro = new CorrelatedSubqueryViewRootJpqlMacro(cb, optionalParameters, viewRootType.getEntityClass(), idAttributePath);
        cb.registerMacro("view_root", macro);
        SubqueryCorrelationBuilder correlationBuilder = new SubqueryCorrelationBuilder(cb, optionalParameters, entityViewConfiguration, this, batchSize, correlationResult);
        provider.applyCorrelation(correlationBuilder, ':' + paramName);
        // TODO: take special care when handling parameters. some must be copied, others should probably be moved to optional parameters

        return new AbstractMap.SimpleEntry<CriteriaBuilder<T>, CorrelatedSubqueryViewRootJpqlMacro>((CriteriaBuilder<T>) cb, macro);
    }

    protected abstract void finishCriteriaBuilder(CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration, int batchSize, String correlationRoot);

    protected final String generateCorrelationParamName(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters) {
        int paramNumber = 0;
        String paramName;
        while (true) {
            paramName = CORRELATION_PARAM_PREFIX + paramNumber;
            if (queryBuilder.getParameter(paramName) != null) {
                paramNumber++;
            } else if (optionalParameters.containsKey(paramName)) {
                paramNumber++;
            } else {
                return paramName;
            }
        }
    }

}
