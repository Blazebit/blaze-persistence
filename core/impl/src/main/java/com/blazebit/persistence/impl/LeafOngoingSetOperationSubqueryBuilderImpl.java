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

import com.blazebit.persistence.FinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LeafOngoingSetOperationSubqueryBuilderImpl<T> extends AbstractCommonQueryBuilder<Tuple, LeafOngoingSetOperationSubqueryBuilder<T>, LeafOngoingSetOperationSubqueryBuilder<T>, StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingSetOperationSubqueryBuilder<T>>, FinalSetOperationSubqueryBuilderImpl<Tuple>> implements LeafOngoingSetOperationSubqueryBuilder<T> {

    // Very tricky for subquery, thank god we have raw types
    @SuppressWarnings("unchecked")
    public LeafOngoingSetOperationSubqueryBuilderImpl(MainQuery mainQuery, FinalSetOperationSubqueryBuilderImpl<T> finalSetOperationBuilder) {
        super(mainQuery, false, DbmsStatementType.SELECT, Tuple.class, null, (FinalSetOperationSubqueryBuilderImpl<Tuple>) finalSetOperationBuilder);
    }

    @Override
    public LeafOngoingSetOperationSubqueryBuilder<T> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public LeafOngoingSetOperationSubqueryBuilder<T> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FinalSetOperationSubqueryBuilder<T> endSet() {
        return (FinalSetOperationSubqueryBuilder<T>) finalSetOperationBuilder;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected FinalSetOperationSubqueryBuilderImpl<Tuple> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        FinalSetOperationSubqueryBuilderImpl<T> currentSetOperationBuilder = (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder;
        return (FinalSetOperationSubqueryBuilderImpl<Tuple>) new FinalSetOperationSubqueryBuilderImpl<T>(mainQuery, currentSetOperationBuilder.getResult(), operator, nested, currentSetOperationBuilder.getListener(), currentSetOperationBuilder.getInitiator());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected LeafOngoingSetOperationSubqueryBuilder<T> createSetOperand(FinalSetOperationSubqueryBuilderImpl<Tuple> finalSetOperationBuilder) {
        return new LeafOngoingSetOperationSubqueryBuilderImpl<T>(mainQuery, (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingSetOperationSubqueryBuilder<T>> createSubquerySetOperand(FinalSetOperationSubqueryBuilderImpl<Tuple> finalSetOperationBuilder, FinalSetOperationSubqueryBuilderImpl<Tuple> resultFinalSetOperationBuilder) {
        LeafOngoingSetOperationSubqueryBuilderImpl<T> leafCb = new LeafOngoingSetOperationSubqueryBuilderImpl<T>(mainQuery, (FinalSetOperationSubqueryBuilderImpl<T>) resultFinalSetOperationBuilder);
        return new OngoingSetOperationSubqueryBuilderImpl<T, LeafOngoingSetOperationSubqueryBuilder<T>>(mainQuery, (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder, leafCb);
    }

}
