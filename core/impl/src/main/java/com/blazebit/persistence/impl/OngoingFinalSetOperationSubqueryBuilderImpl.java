/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.OngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingFinalSetOperationSubqueryBuilderImpl<T> extends BaseFinalSetOperationSubqueryBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>> implements OngoingFinalSetOperationSubqueryBuilder<T> {

    public OngoingFinalSetOperationSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, T result, boolean endResultAsJoinOnBuilder, SetOperationType operator, boolean nested, SubqueryBuilderListener<T> listener, SubqueryBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, result, endResultAsJoinOnBuilder, operator, nested, listener, initiator);
    }

    public OngoingFinalSetOperationSubqueryBuilderImpl(BaseFinalSetOperationBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>, BaseFinalSetOperationSubqueryBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, OngoingFinalSetOperationSubqueryBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationSubqueryBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new OngoingFinalSetOperationSubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }
}
