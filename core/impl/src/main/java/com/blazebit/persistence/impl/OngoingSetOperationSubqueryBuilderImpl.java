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

import javax.persistence.Tuple;

import com.blazebit.persistence.OngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class OngoingSetOperationSubqueryBuilderImpl<T, Z> extends AbstractCommonQueryBuilder<Tuple, OngoingSetOperationSubqueryBuilder<T, Z>, OngoingSetOperationSubqueryBuilder<T, Z>, StartOngoingSetOperationSubqueryBuilder<T, OngoingSetOperationSubqueryBuilder<T, Z>>, FinalSetOperationSubqueryBuilderImpl<Tuple>> implements StartOngoingSetOperationSubqueryBuilder<T, Z> {
	
    private final Z result;
    
    // Very tricky for subquery, thank god we have raw types
    @SuppressWarnings("unchecked")
    public OngoingSetOperationSubqueryBuilderImpl(MainQuery mainQuery, FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder, Z result) {
        super(mainQuery, false, DbmsStatementType.SELECT, Tuple.class, null, (FinalSetOperationSubqueryBuilderImpl<Tuple>) finalSetOperationBuilder);
        this.result = result;
    }

    @Override
    public OngoingSetOperationSubqueryBuilder<T, Z> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public OngoingSetOperationSubqueryBuilder<T, Z> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    public Z endSet() {
        return result;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected FinalSetOperationSubqueryBuilderImpl<Tuple> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        FinalSetOperationSubqueryBuilderImpl<T> currentSetOperationBuilder = (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder;
        return (FinalSetOperationSubqueryBuilderImpl<Tuple>) new FinalSetOperationSubqueryBuilderImpl<T>(mainQuery, currentSetOperationBuilder.getResult(), operator, nested, currentSetOperationBuilder.getListener(), currentSetOperationBuilder.getInitiator());
    }

    @Override
    public StartOngoingSetOperationSubqueryBuilder<T, OngoingSetOperationSubqueryBuilder<T, Z>> startSet() {
        return (StartOngoingSetOperationSubqueryBuilder<T, OngoingSetOperationSubqueryBuilder<T, Z>>) super.startSet();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected OngoingSetOperationSubqueryBuilder<T, Z> createSetOperand(FinalSetOperationSubqueryBuilderImpl<Tuple> finalSetOperationBuilder) {
        return new OngoingSetOperationSubqueryBuilderImpl<T, Z>(mainQuery, (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder, result);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected StartOngoingSetOperationSubqueryBuilder<T, OngoingSetOperationSubqueryBuilder<T, Z>> createSubquerySetOperand(FinalSetOperationSubqueryBuilderImpl<Tuple> finalSetOperationBuilder, FinalSetOperationSubqueryBuilderImpl<Tuple> resultFinalSetOperationBuilder) {
        OngoingSetOperationSubqueryBuilderImpl<T, Z> resultCb = new OngoingSetOperationSubqueryBuilderImpl<T, Z>(mainQuery, (FinalSetOperationSubqueryBuilderImpl<T>) resultFinalSetOperationBuilder, result);
        return new OngoingSetOperationSubqueryBuilderImpl<T, OngoingSetOperationSubqueryBuilder<T,Z>>(mainQuery, (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder, resultCb);
    }

}
