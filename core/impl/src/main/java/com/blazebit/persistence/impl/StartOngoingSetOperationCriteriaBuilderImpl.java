/*
 * Copyright 2014 - 2017 Blazebit.
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
import com.blazebit.persistence.MiddleOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.OngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class StartOngoingSetOperationCriteriaBuilderImpl<T, Z> extends AbstractCriteriaBuilder<T, StartOngoingSetOperationCriteriaBuilder<T, Z>, OngoingSetOperationCriteriaBuilder<T, Z>, StartOngoingSetOperationCriteriaBuilder<T, MiddleOngoingSetOperationCriteriaBuilder<T, Z>>> implements StartOngoingSetOperationCriteriaBuilder<T, Z> {

    private final Z endSetResult;
    
    public StartOngoingSetOperationCriteriaBuilderImpl(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, BuilderListener<Object> listener, OngoingFinalSetOperationCriteriaBuilderImpl<T> finalSetOperationBuilder, Z endSetResult) {
        super(mainQuery, isMainQuery, clazz, null, listener, finalSetOperationBuilder);
        this.endSetResult = endSetResult;
    }

    @Override
    public Z endSet() {
        subListener.verifyBuilderEnded();
        this.setOperationEnded = true;
        // Only check the query if it's not empty
        if (!isEmpty()) {
            prepareAndCheck();
        }
        listener.onBuilderEnded(this);
        finalSetOperationBuilder.setOperationEnded = true;
        return endSetResult;
    }
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public OngoingFinalSetOperationCriteriaBuilder<Z> endSetWith() {
        subListener.verifyBuilderEnded();
        this.setOperationEnded = true;
        // Only check the query if it's not empty
        if (!isEmpty()) {
            prepareAndCheck();
        }
        listener.onBuilderEnded(this);
        return (OngoingFinalSetOperationCriteriaBuilder<Z>) (OngoingFinalSetOperationCriteriaBuilder) finalSetOperationBuilder;
    }

    @Override
    protected BaseFinalSetOperationCriteriaBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, true);
    }

    @Override
    protected OngoingSetOperationCriteriaBuilder<T, Z> createSetOperand(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return createOngoing(finalSetOperationBuilder, endSetResult);
    }

    @Override
    protected StartOngoingSetOperationCriteriaBuilder<T, MiddleOngoingSetOperationCriteriaBuilder<T, Z>> createSubquerySetOperand(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder, BaseFinalSetOperationCriteriaBuilderImpl<T, ?> resultFinalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        MiddleOngoingSetOperationCriteriaBuilder<T, Z> resultCb = createStartOngoing(resultFinalSetOperationBuilder, endSetResult);
        return createStartOngoing(finalSetOperationBuilder, resultCb);
    }

}
