/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.SetOperationType;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FullSelectCTECriteriaBuilderImpl<T> extends AbstractCTECriteriaBuilder<T, FullSelectCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>> implements FullSelectCTECriteriaBuilder<T> {

    public FullSelectCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, CTEManager.CTEKey cteKey, boolean inline, Class<Object> clazz, T result, CTEBuilderListener listener, AliasManager parentAliasManager, JoinManager parentJoinManager) {
        super(mainQuery, queryContext, cteKey, inline, clazz, result, listener, null, parentAliasManager, parentJoinManager);
    }

    public FullSelectCTECriteriaBuilderImpl(AbstractCTECriteriaBuilder<T, FullSelectCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<Object, FullSelectCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new FullSelectCTECriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, nested, this);
    }

    @Override
    protected LeafOngoingSetOperationCTECriteriaBuilderImpl<T> createSetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onReplaceBuilder(this, finalSetOperationBuilder);
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>> createSubquerySetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> resultFinalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        LeafOngoingSetOperationCTECriteriaBuilderImpl<T> leafCb = createSetOperand(resultFinalSetOperationBuilder);
        return (StartOngoingSetOperationCTECriteriaBuilder) createStartOngoing(finalSetOperationBuilder, leafCb);
    }

}
