/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.OngoingFinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingFinalSetOperationCriteriaBuilderImpl<T> extends BaseFinalSetOperationCriteriaBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>> implements OngoingFinalSetOperationCriteriaBuilder<T> {
    
    public OngoingFinalSetOperationCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, SetOperationType operator, boolean nested, BuilderListener<Object> listener) {
        super(mainQuery, queryContext, isMainQuery, clazz, operator, nested, listener, null);
    }

    public OngoingFinalSetOperationCriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>, BaseFinalSetOperationCriteriaBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, OngoingFinalSetOperationCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationCriteriaBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new OngoingFinalSetOperationCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }
}
