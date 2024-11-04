/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.ReturningDeleteCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class ReturningDeleteCriteriaBuilderImpl<T, Y> extends BaseDeleteCriteriaBuilderImpl<T, ReturningDeleteCriteriaBuilder<T, Y>, Y> implements ReturningDeleteCriteriaBuilder<T, Y> {

    public ReturningDeleteCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, String alias, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener) {
        super(mainQuery, queryContext, false, clazz, alias, cteKey, cteClass, result, listener);
    }

    public ReturningDeleteCriteriaBuilderImpl(AbstractModificationCriteriaBuilder<T, ReturningDeleteCriteriaBuilder<T, Y>, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, ReturningDeleteCriteriaBuilder<T, Y>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new ReturningDeleteCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected void buildExternalQueryString(StringBuilder sbSelectFrom) {
        super.buildExternalQueryString(sbSelectFrom);
        applyJpaReturning(sbSelectFrom);
    }

}
