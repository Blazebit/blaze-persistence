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

import com.blazebit.persistence.OngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingFinalSetOperationSubqueryBuilderImpl<T> extends BaseFinalSetOperationSubqueryBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>> implements OngoingFinalSetOperationSubqueryBuilder<T> {

    public OngoingFinalSetOperationSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, T result, SetOperationType operator, boolean nested, SubqueryBuilderListener<T> listener, SubqueryBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, result, operator, nested, listener, initiator);
    }

    public OngoingFinalSetOperationSubqueryBuilderImpl(BaseFinalSetOperationBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>, BaseFinalSetOperationSubqueryBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, OngoingFinalSetOperationSubqueryBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationSubqueryBuilderImpl<T, OngoingFinalSetOperationSubqueryBuilder<T>>> copy(QueryContext queryContext) {
        return new OngoingFinalSetOperationSubqueryBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }
}
