/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.spi.SetOperationType;

import jakarta.persistence.Tuple;
import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LeafOngoingSetOperationSubqueryBuilderImpl<T> extends BaseSubqueryBuilderImpl<T, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>> implements LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingFinalSetOperationSubqueryBuilder<T> {

    public LeafOngoingSetOperationSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, T result, SubqueryBuilderListener<T> listener, FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder) {
        super(mainQuery, queryContext, aliasManager, parentJoinManager, expressionFactory, result, false, listener, finalSetOperationBuilder);
    }

    public LeafOngoingSetOperationSubqueryBuilderImpl(BaseSubqueryBuilderImpl<T, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext,
                                                      Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<Tuple, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>, BaseFinalSetOperationSubqueryBuilderImpl<T, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping,
                                                                                                                                                                                                                                                                             ExpressionCopyContext copyContext) {
        return new LeafOngoingSetOperationSubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public FinalSetOperationSubqueryBuilder<T> endSet() {
        subListener.verifySubqueryBuilderEnded();
        this.setOperationEnded = true;
        // Only check the query if it's not empty
        if (isEmpty()) {
            if (finalSetOperationBuilder.setOperationManager.hasSetOperations()) {
                finalSetOperationBuilder.setOperationManager.removeOperand(this);
            }
        } else {
            prepareAndCheck(null);
        }
        listener.onBuilderEnded(this);
        return (FinalSetOperationSubqueryBuilder<T>) (FinalSetOperationSubqueryBuilder) finalSetOperationBuilder;
    }
    
    @Override
    protected BaseFinalSetOperationSubqueryBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, nested);
    }

    @Override
    protected LeafOngoingSetOperationSubqueryBuilderImpl<T> createSetOperand(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>> createSubquerySetOperand(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder, BaseFinalSetOperationSubqueryBuilderImpl<T, ?> resultFinalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        LeafOngoingFinalSetOperationSubqueryBuilder<T> leafCb = createLeaf(resultFinalSetOperationBuilder);
        return createStartOngoing(finalSetOperationBuilder, leafCb);
    }

}
