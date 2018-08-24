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

import com.blazebit.persistence.OngoingFinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingFinalSetOperationCriteriaBuilderImpl<T> extends BaseFinalSetOperationCriteriaBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>> implements OngoingFinalSetOperationCriteriaBuilder<T> {
    
    public OngoingFinalSetOperationCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, SetOperationType operator, boolean nested, BuilderListener<Object> listener) {
        super(mainQuery, queryContext, isMainQuery, clazz, operator, nested, listener, null);
    }

    public OngoingFinalSetOperationCriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>, BaseFinalSetOperationCriteriaBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, OngoingFinalSetOperationCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationCriteriaBuilderImpl<T, OngoingFinalSetOperationCriteriaBuilder<T>>> copy(QueryContext queryContext) {
        return new OngoingFinalSetOperationCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }
}
