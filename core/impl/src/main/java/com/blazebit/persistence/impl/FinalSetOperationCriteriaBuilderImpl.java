/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FinalSetOperationCriteriaBuilderImpl<T> extends BaseFinalSetOperationCriteriaBuilderImpl<T, FinalSetOperationCriteriaBuilder<T>> implements FinalSetOperationCriteriaBuilder<T> {

    
    public FinalSetOperationCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, SetOperationType operator, boolean nested, BuilderListener<Object> listener) {
        super(mainQuery, queryContext, isMainQuery, clazz, operator, nested, listener, null);
    }

    public FinalSetOperationCriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, FinalSetOperationCriteriaBuilder<T>, BaseFinalSetOperationCriteriaBuilderImpl<T, FinalSetOperationCriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, FinalSetOperationCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationCriteriaBuilderImpl<T, FinalSetOperationCriteriaBuilder<T>>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new FinalSetOperationCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected JoinVisitor applyImplicitJoins(JoinVisitor parentVisitor) {
        // There is nothing to do here for final builders as they don't have any nodes
        return null;
    }
}
