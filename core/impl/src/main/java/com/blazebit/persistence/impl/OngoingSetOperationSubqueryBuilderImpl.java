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

import com.blazebit.persistence.MiddleOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.OngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.OngoingSetOperationSubqueryBuilder;
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
public class OngoingSetOperationSubqueryBuilderImpl<T, Z> extends BaseSubqueryBuilderImpl<T, OngoingSetOperationSubqueryBuilder<T, Z>, OngoingSetOperationSubqueryBuilder<T, Z>, StartOngoingSetOperationSubqueryBuilder<T, MiddleOngoingSetOperationSubqueryBuilder<T, Z>>> implements OngoingSetOperationSubqueryBuilder<T, Z> {

    private final Z endSetResult;

    public OngoingSetOperationSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, T result, SubqueryBuilderListener<T> listener, OngoingFinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder, Z endSetResult) {
        super(mainQuery, queryContext, aliasManager, parentJoinManager, expressionFactory, result, listener, finalSetOperationBuilder);
        this.endSetResult = endSetResult;
    }

    public OngoingSetOperationSubqueryBuilderImpl(BaseSubqueryBuilderImpl<T, OngoingSetOperationSubqueryBuilder<T, Z>, OngoingSetOperationSubqueryBuilder<T, Z>, StartOngoingSetOperationSubqueryBuilder<T, MiddleOngoingSetOperationSubqueryBuilder<T, Z>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.endSetResult = null;
    }

    @Override
    AbstractCommonQueryBuilder<Tuple, OngoingSetOperationSubqueryBuilder<T, Z>, OngoingSetOperationSubqueryBuilder<T, Z>, StartOngoingSetOperationSubqueryBuilder<T, MiddleOngoingSetOperationSubqueryBuilder<T, Z>>, BaseFinalSetOperationSubqueryBuilderImpl<T, ?>> copy(QueryContext queryContext) {
        return new OngoingSetOperationSubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }

    @Override
    public Z endSet() {
        subListener.verifySubqueryBuilderEnded();
        this.setOperationEnded = true;
        // Only check the query if it's not empty
        if (!isEmpty()) {
            prepareAndCheck();
        }
        listener.onBuilderEnded(this);
        finalSetOperationBuilder.setOperationEnded = true;
        return endSetResult;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public OngoingFinalSetOperationSubqueryBuilder<Z> endSetWith() {
        subListener.verifySubqueryBuilderEnded();
        this.setOperationEnded = true;
        // Only check the query if it's not empty
        if (!isEmpty()) {
            prepareAndCheck();
        }
        listener.onBuilderEnded(this);
        return (OngoingFinalSetOperationSubqueryBuilder<Z>) (OngoingFinalSetOperationSubqueryBuilder) finalSetOperationBuilder;
    }

    @Override
    protected BaseFinalSetOperationSubqueryBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, true);
    }

    @Override
    protected OngoingSetOperationSubqueryBuilder<T, Z> createSetOperand(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        return createOngoing(finalSetOperationBuilder, endSetResult);
    }

    @Override
    protected StartOngoingSetOperationSubqueryBuilder<T, MiddleOngoingSetOperationSubqueryBuilder<T, Z>> createSubquerySetOperand(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder, BaseFinalSetOperationSubqueryBuilderImpl<T, ?> resultFinalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        MiddleOngoingSetOperationSubqueryBuilder<T, Z> resultCb = createStartOngoing(resultFinalSetOperationBuilder, endSetResult);
        return createStartOngoing(finalSetOperationBuilder, resultCb);
    }

}
