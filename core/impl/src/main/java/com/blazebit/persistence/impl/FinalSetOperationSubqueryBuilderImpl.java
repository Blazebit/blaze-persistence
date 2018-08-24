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
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FinalSetOperationSubqueryBuilderImpl<T> extends BaseFinalSetOperationSubqueryBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>> implements FinalSetOperationSubqueryBuilder<T> {

    public FinalSetOperationSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, T result, SetOperationType operator, boolean nested, SubqueryBuilderListener<T> listener, SubqueryBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, result, operator, nested, listener, initiator);
    }

    public FinalSetOperationSubqueryBuilderImpl(BaseFinalSetOperationBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>, BaseFinalSetOperationSubqueryBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, FinalSetOperationSubqueryBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationSubqueryBuilderImpl<T, FinalSetOperationSubqueryBuilder<T>>> copy(QueryContext queryContext) {
        return new FinalSetOperationSubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }

    @Override
    protected void applyImplicitJoins(JoinVisitor parentVisitor) {
        // There is nothing to do here for final builders as they don't have any nodes
    }

    @Override
    public T end() {
        subListener.verifySubqueryBuilderEnded();
        this.setOperationEnded = true;
        listener.onBuilderEnded(this);
        return result;
    }
    
}
