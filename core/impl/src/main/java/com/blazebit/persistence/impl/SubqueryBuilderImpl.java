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

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.LeafOngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.Tuple;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryBuilderImpl<T> extends BaseSubqueryBuilderImpl<T, SubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>> implements SubqueryBuilder<T>, SubqueryInternalBuilder<T> {

    public SubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, T result, SubqueryBuilderListener<T> listener) {
        super(mainQuery, queryContext, aliasManager, parentJoinManager, expressionFactory, result, listener, null);
    }

    public SubqueryBuilderImpl(BaseSubqueryBuilderImpl<T, SubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<Tuple, SubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>>, BaseFinalSetOperationSubqueryBuilderImpl<T, ?>> copy(QueryContext queryContext) {
        return new SubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }

    @Override
    public List<Expression> getSelectExpressions() {
        List<Expression> selectExpressions = new ArrayList<Expression>(selectManager.getSelectInfos().size());

        for (SelectInfo info : selectManager.getSelectInfos()) {
            selectExpressions.add(info.getExpression());
        }

        return selectExpressions;
    }

    @Override
    public T end() {
        listener.onBuilderEnded(this);
        return result;
    }

    public T getResult() {
        return result;
    }
    
    @Override
    protected BaseFinalSetOperationSubqueryBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, nested, this);
    }

    @Override
    protected LeafOngoingSetOperationSubqueryBuilderImpl<T> createSetOperand(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        listener.onReplaceBuilder(this, finalSetOperationBuilder);
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>> createSubquerySetOperand(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder, BaseFinalSetOperationSubqueryBuilderImpl<T, ?> resultFinalSetOperationBuilder) {
        subListener.verifySubqueryBuilderEnded();
        LeafOngoingFinalSetOperationSubqueryBuilder<T> leafCb = createSetOperand(resultFinalSetOperationBuilder);
        return createStartOngoing(finalSetOperationBuilder, leafCb);
    }
}
