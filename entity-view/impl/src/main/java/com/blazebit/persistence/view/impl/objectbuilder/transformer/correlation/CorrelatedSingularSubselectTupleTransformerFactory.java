/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformer;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSingularSubselectTupleTransformerFactory extends AbstractCorrelatedSubselectTupleTransformerFactory {

    public CorrelatedSingularSubselectTupleTransformerFactory(Correlator correlator, EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewRoot, String viewRootAlias, ManagedViewTypeImplementor<?> embeddingViewType, String embeddingViewPath,
                                                              Expression correlationResult, String correlationBasisExpression, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory,
                                                              String attributePath, String[] fetches, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter) {
        super(correlator, evm, viewRoot, viewRootAlias, embeddingViewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, viewRootIndex, embeddingViewIndex, tupleIndex, correlationBasisType, correlationBasisEntity, limiter);
    }

    @Override
    public TupleTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return new CorrelatedSingularSubselectTupleTransformer(entityViewConfiguration.getExpressionFactory(), correlator, evm, viewRootType, viewRootAlias, embeddingViewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, correlationProviderFactory,
                attributePath, fetches, viewRootIndex, embeddingViewIndex, correlationBasisIndex, correlationBasisType, correlationBasisEntity, limiter, entityViewConfiguration);
    }

}
