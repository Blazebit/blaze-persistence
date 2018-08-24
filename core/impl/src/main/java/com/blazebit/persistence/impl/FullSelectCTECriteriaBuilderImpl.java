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

import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class FullSelectCTECriteriaBuilderImpl<T> extends AbstractCTECriteriaBuilder<T, FullSelectCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>> implements FullSelectCTECriteriaBuilder<T> {

    public FullSelectCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, String cteName, Class<Object> clazz, T result, CTEBuilderListener listener) {
        super(mainQuery, queryContext, cteName, clazz, result, listener, null);
    }

    public FullSelectCTECriteriaBuilderImpl(AbstractCTECriteriaBuilder<T, FullSelectCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<Object, FullSelectCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?>> copy(QueryContext queryContext) {
        return new FullSelectCTECriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }

    @Override
    protected BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, nested, this);
    }

    @Override
    protected LeafOngoingSetOperationCTECriteriaBuilderImpl<T> createSetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onReplaceBuilder(this, finalSetOperationBuilder);
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>> createSubquerySetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> resultFinalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        LeafOngoingSetOperationCTECriteriaBuilderImpl<T> leafCb = createSetOperand(resultFinalSetOperationBuilder);
        return (StartOngoingSetOperationCTECriteriaBuilder) createStartOngoing(finalSetOperationBuilder, leafCb);
    }

}
