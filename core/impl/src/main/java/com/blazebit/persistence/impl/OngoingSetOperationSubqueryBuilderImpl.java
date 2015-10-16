/*
 * Copyright 2015 Blazebit.
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

import com.blazebit.persistence.OngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingSetOperationSubqueryBuilderImpl<T, Z> extends BaseSubqueryBuilderImpl<T, OngoingSetOperationSubqueryBuilder<T, Z>, OngoingSetOperationSubqueryBuilder<T, Z>, StartOngoingSetOperationSubqueryBuilder<T, OngoingSetOperationSubqueryBuilder<T, Z>>> implements StartOngoingSetOperationSubqueryBuilder<T, Z> {
	
    private final Z endSetResult;
    
    public OngoingSetOperationSubqueryBuilderImpl(MainQuery mainQuery, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, T result, SubqueryBuilderListener<T> listener, FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder, Z endSetResult) {
        super(mainQuery, aliasManager, parentJoinManager, expressionFactory, result, listener, finalSetOperationBuilder);
        this.endSetResult = endSetResult;
    }

    @Override
    public OngoingSetOperationSubqueryBuilder<T, Z> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public OngoingSetOperationSubqueryBuilder<T, Z> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    public Z endSet() {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        return endSetResult;
    }
    
    @Override
    protected FinalSetOperationSubqueryBuilderImpl<T> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        // TODO: not quite sure about the listener passing
        FinalSetOperationSubqueryBuilderImpl<T> currentSetOperationBuilder = (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder;
        return new FinalSetOperationSubqueryBuilderImpl<T>(mainQuery, currentSetOperationBuilder.getResult(), operator, nested, currentSetOperationBuilder.getListener(), currentSetOperationBuilder.getInitiator());
    }

    @Override
    protected OngoingSetOperationSubqueryBuilderImpl<T, Z> createSetOperand(FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        return createOngoing(finalSetOperationBuilder, endSetResult);
    }

    @Override
    protected StartOngoingSetOperationSubqueryBuilder<T, OngoingSetOperationSubqueryBuilder<T, Z>> createSubquerySetOperand(FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder, FinalSetOperationSubqueryBuilderImpl<T> resultFinalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        OngoingSetOperationSubqueryBuilder<T, Z> resultCb = createOngoing(resultFinalSetOperationBuilder, endSetResult);
        return createOngoing(finalSetOperationBuilder, resultCb);
    }

}
