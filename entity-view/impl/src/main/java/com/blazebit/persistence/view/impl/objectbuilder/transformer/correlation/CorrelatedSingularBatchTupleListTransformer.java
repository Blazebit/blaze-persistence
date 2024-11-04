/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;

import javax.persistence.NonUniqueResultException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSingularBatchTupleListTransformer extends AbstractCorrelatedBatchTupleListTransformer {

    public CorrelatedSingularBatchTupleListTransformer(ExpressionFactory ef, Correlator correlator, ManagedViewTypeImplementor<?> viewRootType, ManagedViewTypeImplementor<?> embeddingViewType, Expression correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                       boolean correlatesThis, int viewRootIndex, int embeddingViewIndex, int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter, EntityViewConfiguration entityViewConfiguration) {
        super(ef, correlator, null, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, EMPTY, null, null, correlatesThis, viewRootIndex, embeddingViewIndex, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, limiter, entityViewConfiguration);
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
                correlationValues.get(element[keyIndex]).onResult(element[valueIndex], this);
            }
        }
    }
}
