/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FinalSetOperationSubqueryBuilderImpl<T> extends BaseFinalSetOperationSubqueryBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>> implements FinalSetOperationSubqueryBuilder<T> {

    public FinalSetOperationSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, T result, boolean endResultAsJoinOnBuilder, SetOperationType operator, boolean nested, SubqueryBuilderListener<T> listener, SubqueryBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, result, endResultAsJoinOnBuilder, operator, nested, listener, initiator);
    }

    public FinalSetOperationSubqueryBuilderImpl(BaseFinalSetOperationBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>, BaseFinalSetOperationSubqueryBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, FinalSetOperationSubqueryBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationSubqueryBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new FinalSetOperationSubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected JoinVisitor applyImplicitJoins(JoinVisitor parentVisitor) {
        // There is nothing to do here for final builders as they don't have any nodes
        return null;
    }

    @Override
    public T end() {
        subListener.verifySubqueryBuilderEnded();
        this.setOperationEnded = true;
        listener.onBuilderEnded(this);
        if (endResultAsJoinOnBuilder) {
            return (T) ((JoinOnBuilder<?>) result).end();
        }
        return result;
    }
    
}
