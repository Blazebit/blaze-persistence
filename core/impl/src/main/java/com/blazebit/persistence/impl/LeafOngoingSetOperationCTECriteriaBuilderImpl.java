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

import com.blazebit.persistence.FinalSetOperationCTECriteriaBuilder;
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
public class LeafOngoingSetOperationCTECriteriaBuilderImpl<T> extends AbstractCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>> implements LeafOngoingSetOperationCTECriteriaBuilder<T>, LeafOngoingFinalSetOperationCTECriteriaBuilder<T> {

    public LeafOngoingSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, String cteName, Class<Object> clazz, T result, CTEBuilderListener listener, FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder) {
        super(mainQuery, queryContext, cteName, clazz, result, listener, finalSetOperationBuilder);
    }

    public LeafOngoingSetOperationCTECriteriaBuilderImpl(AbstractCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<Object, LeafOngoingSetOperationCTECriteriaBuilder<T>, LeafOngoingSetOperationCTECriteriaBuilder<T>, StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>>, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?>> copy(QueryContext queryContext) {
        return new LeafOngoingSetOperationCTECriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public FinalSetOperationCTECriteriaBuilder<T> endSet() {
        subListener.verifyBuilderEnded();
        this.setOperationEnded = true;
        // Only check the query if it's not empty
        if (isEmpty()) {
            if (finalSetOperationBuilder.setOperationManager.hasSetOperations()) {
                finalSetOperationBuilder.setOperationManager.removeOperand(this);
            }
        } else {
            prepareAndCheck();
        }
        listener.onBuilderEnded(this);
        return (FinalSetOperationCTECriteriaBuilder<T>) (FinalSetOperationCTECriteriaBuilder) finalSetOperationBuilder;
    }

    @Override
    protected BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, nested);
    }

    @Override
    protected LeafOngoingSetOperationCTECriteriaBuilderImpl<T> createSetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>> createSubquerySetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> resultFinalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        LeafOngoingSetOperationCTECriteriaBuilderImpl<T> leafCb = createLeaf(resultFinalSetOperationBuilder);
        return (StartOngoingSetOperationCTECriteriaBuilder) createStartOngoing(finalSetOperationBuilder, leafCb);
    }

}
