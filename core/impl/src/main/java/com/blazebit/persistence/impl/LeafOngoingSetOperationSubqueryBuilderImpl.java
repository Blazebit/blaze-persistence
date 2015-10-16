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

import com.blazebit.persistence.FinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LeafOngoingSetOperationSubqueryBuilderImpl<T> extends BaseSubqueryBuilderImpl<T, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingSetOperationSubqueryBuilder<T>>> implements LeafOngoingSetOperationSubqueryBuilder<T> {

    public LeafOngoingSetOperationSubqueryBuilderImpl(MainQuery mainQuery, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, T result, SubqueryBuilderListener<T> listener, FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder) {
        super(mainQuery, aliasManager, parentJoinManager, expressionFactory, result, listener, finalSetOperationBuilder);
    }

    @Override
    public FinalSetOperationSubqueryBuilder<T> endSet() {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        return finalSetOperationBuilder;
    }
    
    @Override
    protected FinalSetOperationSubqueryBuilderImpl<T> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        // TODO: not quite sure about the listener passing
        return new FinalSetOperationSubqueryBuilderImpl<T>(mainQuery, finalSetOperationBuilder.getResult(), operator, nested, finalSetOperationBuilder.getListener(), finalSetOperationBuilder.getInitiator());
    }

    @Override
    protected LeafOngoingSetOperationSubqueryBuilderImpl<T> createSetOperand(FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingSetOperationSubqueryBuilder<T>> createSubquerySetOperand(FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder, FinalSetOperationSubqueryBuilderImpl<T> resultFinalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onBuilderEnded(this);
        LeafOngoingSetOperationSubqueryBuilder<T> leafCb = createLeaf(resultFinalSetOperationBuilder);
        return createOngoing(finalSetOperationBuilder, leafCb);
    }

}
