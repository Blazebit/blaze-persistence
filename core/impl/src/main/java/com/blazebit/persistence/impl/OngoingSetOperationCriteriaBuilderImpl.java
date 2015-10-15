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

import com.blazebit.persistence.OngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingSetOperationCriteriaBuilderImpl<T, Z> extends AbstractCommonQueryBuilder<T, OngoingSetOperationCriteriaBuilder<T, Z>, OngoingSetOperationCriteriaBuilder<T, Z>, StartOngoingSetOperationCriteriaBuilder<T, OngoingSetOperationCriteriaBuilder<T, Z>>, FinalSetOperationCriteriaBuilderImpl<T>> implements StartOngoingSetOperationCriteriaBuilder<T, Z> {
	
    private final Z result;
    
    public OngoingSetOperationCriteriaBuilderImpl(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, FinalSetOperationCriteriaBuilderImpl<T> finalSetOperationBuilder, Z result) {
        super(mainQuery, isMainQuery, DbmsStatementType.SELECT, clazz, null, finalSetOperationBuilder);
        this.result = result;
    }

    @Override
    public OngoingSetOperationCriteriaBuilder<T, Z> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public OngoingSetOperationCriteriaBuilder<T, Z> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    public Z endSet() {
        return result;
    }
    
    @Override
    protected FinalSetOperationCriteriaBuilderImpl<T> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        boolean wasMainQuery = isMainQuery;
        this.isMainQuery = false;
        return new FinalSetOperationCriteriaBuilderImpl<T>(mainQuery, wasMainQuery, resultType, operator, nested);
    }

    @Override
    public StartOngoingSetOperationCriteriaBuilder<T, OngoingSetOperationCriteriaBuilder<T, Z>> startSet() {
        return (StartOngoingSetOperationCriteriaBuilder<T, OngoingSetOperationCriteriaBuilder<T, Z>>) super.startSet();
    }

    @Override
    protected OngoingSetOperationCriteriaBuilder<T, Z> createSetOperand(FinalSetOperationCriteriaBuilderImpl<T> finalSetOperationBuilder) {
        return new OngoingSetOperationCriteriaBuilderImpl<T, Z>(mainQuery, false, resultType, (FinalSetOperationCriteriaBuilderImpl<T>) finalSetOperationBuilder, result);
    }

    @Override
    protected StartOngoingSetOperationCriteriaBuilder<T, OngoingSetOperationCriteriaBuilder<T, Z>> createSubquerySetOperand(FinalSetOperationCriteriaBuilderImpl<T> finalSetOperationBuilder, FinalSetOperationCriteriaBuilderImpl<T> resultFinalSetOperationBuilder) {
        OngoingSetOperationCriteriaBuilderImpl<T, Z> resultCb = new OngoingSetOperationCriteriaBuilderImpl<T, Z>(mainQuery, false, resultType, (FinalSetOperationCriteriaBuilderImpl<T>) resultFinalSetOperationBuilder, result);
        return new OngoingSetOperationCriteriaBuilderImpl<T, OngoingSetOperationCriteriaBuilder<T,Z>>(mainQuery, false, resultType, (FinalSetOperationCriteriaBuilderImpl<T>) finalSetOperationBuilder, resultCb);
    }

}
