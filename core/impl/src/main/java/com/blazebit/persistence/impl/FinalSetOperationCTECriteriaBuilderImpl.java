/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FinalSetOperationCTECriteriaBuilderImpl<T> extends BaseFinalSetOperationCTECriteriaBuilderImpl<T, FinalSetOperationCTECriteriaBuilder<T>> implements FinalSetOperationCTECriteriaBuilder<T>, CTEInfoBuilder {

    public FinalSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, T result, SetOperationType operator, boolean nested, CTEBuilderListener listener, FullSelectCTECriteriaBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, clazz, result, operator, nested, listener, initiator);
    }

    public FinalSetOperationCTECriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, FinalSetOperationCTECriteriaBuilder<T>, BaseFinalSetOperationCTECriteriaBuilderImpl<T, FinalSetOperationCTECriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, FinalSetOperationCTECriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationCTECriteriaBuilderImpl<T, FinalSetOperationCTECriteriaBuilder<T>>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new FinalSetOperationCTECriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected JoinVisitor applyImplicitJoins(JoinVisitor parentVisitor) {
        // There is nothing to do here for final builders as they don't have any nodes
        return null;
    }

    @Override
    public T end() {
        subListener.verifyBuilderEnded();
        this.setOperationEnded = true;
        listener.onBuilderEnded(this);
        return result;
    }

}
