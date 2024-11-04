/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.OngoingFinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingFinalSetOperationCTECriteriaBuilderImpl<T> extends BaseFinalSetOperationCTECriteriaBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>> implements OngoingFinalSetOperationCTECriteriaBuilder<T>, CTEInfoBuilder {

    public OngoingFinalSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, T result, SetOperationType operator, boolean nested, CTEBuilderListener listener, FullSelectCTECriteriaBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, clazz, result, operator, nested, listener, initiator);
    }

    public OngoingFinalSetOperationCTECriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>, BaseFinalSetOperationCTECriteriaBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, OngoingFinalSetOperationCTECriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationCTECriteriaBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new OngoingFinalSetOperationCTECriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }
}
