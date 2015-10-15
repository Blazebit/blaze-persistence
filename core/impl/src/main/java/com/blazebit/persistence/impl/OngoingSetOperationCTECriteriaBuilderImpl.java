/*
 * Copyright 2015 Blazebit.
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

import com.blazebit.persistence.OngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingSetOperationCTECriteriaBuilderImpl<T, Z> extends AbstractCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>, OngoingSetOperationCTECriteriaBuilder<T, Z>, StartOngoingSetOperationCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>>, FinalSetOperationCTECriteriaBuilderImpl<Object>> implements StartOngoingSetOperationCTECriteriaBuilder<T, Z> {
	
    private final Z setResult;
    
    public OngoingSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, Class<Object> clazz, FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder, T result, CTEBuilderListener listener, Z setResult) {
        super(mainQuery, clazz, finalSetOperationBuilder, result, listener);
        this.setResult = setResult;
    }
    
    public OngoingSetOperationCTECriteriaBuilderImpl(MainQuery mainQuery, Class<Object> clazz, FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder, Z setResult) {
        super(mainQuery, clazz, finalSetOperationBuilder, null, null);
        this.setResult = setResult;
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
    public Z endSet() {
        return setResult;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected FinalSetOperationCTECriteriaBuilderImpl<Object> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        return new FinalSetOperationCTECriteriaBuilderImpl<Object>(mainQuery, resultType, result, operator, nested, listener, ((FinalSetOperationCTECriteriaBuilderImpl<T>) finalSetOperationBuilder).getInitiator());
    }

    @Override
    public StartOngoingSetOperationCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>> startSet() {
        return (StartOngoingSetOperationCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>>) super.startSet();
    }

    @Override
    protected OngoingSetOperationCTECriteriaBuilder<T, Z> createSetOperand(FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder) {
        return new OngoingSetOperationCTECriteriaBuilderImpl<T, Z>(mainQuery, resultType, (FinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder, result, listener, setResult);
    }

    @Override
    protected StartOngoingSetOperationCTECriteriaBuilder<T, OngoingSetOperationCTECriteriaBuilder<T, Z>> createSubquerySetOperand(FinalSetOperationCTECriteriaBuilderImpl<Object> finalSetOperationBuilder, FinalSetOperationCTECriteriaBuilderImpl<Object> resultFinalSetOperationBuilder) {
        OngoingSetOperationCTECriteriaBuilderImpl<T, Z> resultCb = new OngoingSetOperationCTECriteriaBuilderImpl<T, Z>(mainQuery, resultType, (FinalSetOperationCTECriteriaBuilderImpl<Object>) resultFinalSetOperationBuilder, result, listener, setResult);
        return new OngoingSetOperationCTECriteriaBuilderImpl<T, OngoingSetOperationCTECriteriaBuilder<T, Z>>(mainQuery, resultType, (FinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder, resultCb);
    }

}
