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

import com.blazebit.persistence.OngoingFinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.OngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingSetOperationCTECriteriaBuilderImpl<T, Z extends AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> extends AbstractCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>, OngoingSetOperationCTECriteriaBuilder<T, Z>, StartOngoingSetOperationCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>>> implements StartOngoingSetOperationCTECriteriaBuilder<T, Z> {
    
    protected final Z endSetResult;
    
    public OngoingSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, String cteName, Class<Object> clazz, T result, CTEBuilderListener listener, OngoingFinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder, Z endSetResult) {
        super(mainQuery, cteName, clazz, result, listener, finalSetOperationBuilder);
        this.endSetResult = endSetResult;
    }

    @Override
    public OngoingSetOperationCTECriteriaBuilder<T, Z> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public OngoingSetOperationCTECriteriaBuilder<T, Z> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && endSetResult.isEmpty();
    }

    @Override
    public Z endSet() {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);

        if (isEmpty() && finalSetOperationBuilder.isEmpty()) {
            finalSetOperationBuilder.setOperationManager.replaceOperand(this, endSetResult);
        }

        return endSetResult;
    }
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public OngoingFinalSetOperationCTECriteriaBuilder<Z> endSetWith() {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return (OngoingFinalSetOperationCTECriteriaBuilder<Z>) (OngoingFinalSetOperationCTECriteriaBuilder) finalSetOperationBuilder;
    }

    @Override
    protected BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return createFinalSetOperationBuilder(operator, nested, true);
    }
    
    @Override
    protected OngoingSetOperationCTECriteriaBuilder<T, Z> createSetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return createOngoing(finalSetOperationBuilder, endSetResult);
    }

    @Override
    protected StartOngoingSetOperationCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>> createSubquerySetOperand(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> resultFinalSetOperationBuilder) {
        subListener.verifyBuilderEnded();
        listener.onBuilderEnded(this);
        OngoingSetOperationCTECriteriaBuilderImpl<T, Z> resultCb = createOngoing(resultFinalSetOperationBuilder, endSetResult);
        return (StartOngoingSetOperationCTECriteriaBuilder) createOngoing(finalSetOperationBuilder, resultCb);
    }

}
