/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.ReturningUpdateCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ReturningUpdateCollectionCriteriaBuilderImpl<T, Y> extends AbstractUpdateCollectionCriteriaBuilder<T, ReturningUpdateCriteriaBuilder<T, Y>, Y> implements ReturningUpdateCriteriaBuilder<T, Y> {

    public ReturningUpdateCollectionCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, String alias, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, false, clazz, alias, cteKey, cteClass, result, listener, collectionName);
    }

    public ReturningUpdateCollectionCriteriaBuilderImpl(ReturningUpdateCollectionCriteriaBuilderImpl<T, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, ReturningUpdateCriteriaBuilder<T, Y>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new ReturningUpdateCollectionCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected void buildExternalQueryString(StringBuilder sbSelectFrom) {
        super.buildExternalQueryString(sbSelectFrom);
        applyJpaReturning(sbSelectFrom);
    }

}
