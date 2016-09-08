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
package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;
import com.blazebit.persistence.view.impl.objectbuilder.ViewTypeObjectBuilderTemplate;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.AbstractMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedSubqueryTupleTransformerFactory<T> implements TupleTransformerFactory {

    private static final String CORRELATION_PARAM_PREFIX = "correlationParam_";

    private final Class<?> criteriaBuilderRoot;
    private final ManagedViewType<?> viewRootType;
    private final CorrelationProviderFactory correlationProviderFactory;
    protected final int tupleIndex;

    public AbstractCorrelatedSubqueryTupleTransformerFactory(Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, CorrelationProviderFactory correlationProviderFactory, int tupleIndex) {
        this.criteriaBuilderRoot = criteriaBuilderRoot;
        this.viewRootType = viewRootType;
        this.correlationProviderFactory = correlationProviderFactory;
        this.tupleIndex = tupleIndex;
    }

    protected final Map.Entry<CriteriaBuilder<T>, CorrelatedSubqueryViewRootJpqlMacro> createCriteriaBuilder(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, String paramName) {
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
        String correlationRoot = provider.applyCorrelation(cb, ':' + paramName);
        // TODO: take special care when handling parameters. some must be copied, others should probably be moved to optional parameters
        return new AbstractMap.SimpleEntry<CriteriaBuilder<T>, CorrelatedSubqueryViewRootJpqlMacro>(finishCriteriaBuilder(cb, optionalParameters, correlationRoot), macro);
    }

    protected abstract CriteriaBuilder<T> finishCriteriaBuilder(CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, String correlationRoot);

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
