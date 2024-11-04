/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSingularSubselectTupleTransformer extends AbstractCorrelatedSubselectTupleTransformer {

    public CorrelatedSingularSubselectTupleTransformer(ExpressionFactory ef, Correlator correlator, EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewRootType, String viewRootAlias, ManagedViewTypeImplementor<?> embeddingViewType, String embeddingViewPath, Expression correlationResult, String correlationBasisExpression, String correlationKeyExpression,
                                                       CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, null, evm, viewRootType, viewRootAlias, embeddingViewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, EMPTY, null, null, viewRootIndex, embeddingViewIndex, tupleIndex, correlationBasisType, correlationBasisEntity, limiter, entityViewConfiguration);
    }

}
