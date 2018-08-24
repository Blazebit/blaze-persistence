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

import com.blazebit.persistence.OngoingFinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingFinalSetOperationCTECriteriaBuilderImpl<T> extends BaseFinalSetOperationCTECriteriaBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>> implements OngoingFinalSetOperationCTECriteriaBuilder<T>, CTEInfoBuilder {

    public OngoingFinalSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, T result, SetOperationType operator, boolean nested, CTEBuilderListener listener, FullSelectCTECriteriaBuilderImpl<?> initiator) {
        super(mainQuery, queryContext, clazz, result, operator, nested, listener, initiator);
    }

    public OngoingFinalSetOperationCTECriteriaBuilderImpl(BaseFinalSetOperationBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>, BaseFinalSetOperationCTECriteriaBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, OngoingFinalSetOperationCTECriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationCTECriteriaBuilderImpl<T, OngoingFinalSetOperationCTECriteriaBuilder<T>>> copy(QueryContext queryContext) {
        return new OngoingFinalSetOperationCTECriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }
}
