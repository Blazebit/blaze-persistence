/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.Tuple;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LeafOngoingSetOperationSubqueryBuilderImpl<T> extends BaseSubqueryBuilderImpl<T, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>> implements LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingFinalSetOperationSubqueryBuilder<T> {

    public LeafOngoingSetOperationSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, T result, SubqueryBuilderListener<T> listener, FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder) {
        super(mainQuery, queryContext, aliasManager, parentJoinManager, expressionFactory, result, listener, finalSetOperationBuilder);
    }

    public LeafOngoingSetOperationSubqueryBuilderImpl(BaseSubqueryBuilderImpl<T, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<Tuple, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>, BaseFinalSetOperationSubqueryBuilderImpl<T, ?>> copy(QueryContext queryContext) {
        return new LeafOngoingSetOperationSubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
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
            prepareAndCheck();
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
