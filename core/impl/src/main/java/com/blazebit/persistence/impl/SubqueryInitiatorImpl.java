/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.LeafOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.ExpressionFactory;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0
 */
public class SubqueryInitiatorImpl<X> implements SubqueryInitiator<X> {

    private final MainQuery mainQuery;
    private final AliasManager aliasManager;
    private final JoinManager parentJoinManager;
    private final ExpressionFactory expressionFactory;
    
    private final X result;
    private final SubqueryBuilderListener<X> listener;

    public SubqueryInitiatorImpl(MainQuery mainQuery, AliasManager aliasManager, JoinManager parentJoinManager, X result, SubqueryBuilderListener<X> listener) {
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.parentJoinManager = parentJoinManager;
        this.expressionFactory = mainQuery.subqueryExpressionFactory;
        this.result = result;
        this.listener = listener;
    }

    @Override
    public SubqueryBuilder<X> from(Class<?> clazz) {
        return from(clazz, null);
    }

    @Override
    public SubqueryBuilder<X> from(Class<?> clazz, String alias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, listener);
        subqueryBuilder.from(clazz, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> from(String correlationPath) {
        return from(correlationPath, null);
    }

    @Override
    public SubqueryBuilder<X> from(String correlationPath, String alias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, listener);
        subqueryBuilder.from(correlationPath, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public StartOngoingSetOperationSubqueryBuilder<X, LeafOngoingSetOperationSubqueryBuilder<X>> startSet() {
        FinalSetOperationSubqueryBuilderImpl<X> parentFinalSetOperationBuilder = new FinalSetOperationSubqueryBuilderImpl<X>(mainQuery, result, null, false, listener, null);
        OngoingFinalSetOperationSubqueryBuilderImpl<X> subFinalSetOperationBuilder = new OngoingFinalSetOperationSubqueryBuilderImpl<X>(mainQuery, null, null, true, parentFinalSetOperationBuilder.getSubListener(), null);
        listener.onBuilderStarted(parentFinalSetOperationBuilder);
        
        LeafOngoingSetOperationSubqueryBuilderImpl<X> leafCb = new LeafOngoingSetOperationSubqueryBuilderImpl<X>(mainQuery, aliasManager, parentJoinManager, expressionFactory, result, parentFinalSetOperationBuilder.getSubListener(), parentFinalSetOperationBuilder);
        OngoingSetOperationSubqueryBuilderImpl<X, LeafOngoingSetOperationSubqueryBuilder<X>> cb = new OngoingSetOperationSubqueryBuilderImpl<X, LeafOngoingSetOperationSubqueryBuilder<X>>(
                mainQuery, aliasManager, parentJoinManager, expressionFactory, result, subFinalSetOperationBuilder.getSubListener(), subFinalSetOperationBuilder, leafCb
        );
        
        subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(cb);
        parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);
        
        subFinalSetOperationBuilder.getSubListener().onBuilderStarted(cb);
        parentFinalSetOperationBuilder.getSubListener().onBuilderStarted(leafCb);
        return cb;
    }

}
