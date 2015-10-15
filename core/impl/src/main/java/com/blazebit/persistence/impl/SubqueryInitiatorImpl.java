/*
 * Copyright 2014 Blazebit.
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

    public SubqueryInitiatorImpl(MainQuery mainQuery, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, X result, SubqueryBuilderListener<X> listener) {
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.parentJoinManager = parentJoinManager;
        this.expressionFactory = expressionFactory;
        this.result = result;
        this.listener = listener;
    }

    @Override
    public SubqueryBuilder<X> from(Class<?> clazz) {
        return from(clazz, null);
    }

    @Override
    public SubqueryBuilder<X> from(Class<?> clazz, String alias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, aliasManager, parentJoinManager, expressionFactory, result, listener);
        subqueryBuilder.from(clazz, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public StartOngoingSetOperationSubqueryBuilder<X, LeafOngoingSetOperationSubqueryBuilder<X>> startSet() {
        // TODO implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
