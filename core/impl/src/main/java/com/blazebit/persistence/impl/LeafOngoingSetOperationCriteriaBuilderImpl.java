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

import com.blazebit.persistence.FinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LeafOngoingSetOperationCriteriaBuilderImpl<T> extends AbstractCommonQueryBuilder<T, LeafOngoingSetOperationCriteriaBuilder<T>, LeafOngoingSetOperationCriteriaBuilder<T>, StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingSetOperationCriteriaBuilder<T>>, FinalSetOperationCriteriaBuilderImpl<T>> implements LeafOngoingSetOperationCriteriaBuilder<T> {

    public LeafOngoingSetOperationCriteriaBuilderImpl(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, FinalSetOperationCriteriaBuilderImpl<T> finalSetOperationBuilder) {
        super(mainQuery, isMainQuery, DbmsStatementType.SELECT, clazz, null, finalSetOperationBuilder);
    }

    @Override
    public LeafOngoingSetOperationCriteriaBuilder<T> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public LeafOngoingSetOperationCriteriaBuilder<T> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    public FinalSetOperationCriteriaBuilder<T> endSet() {
        return finalSetOperationBuilder;
    }
    
    @Override
    protected FinalSetOperationCriteriaBuilderImpl<T> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        boolean wasMainQuery = isMainQuery;
        this.isMainQuery = false;
        return new FinalSetOperationCriteriaBuilderImpl<T>(mainQuery, wasMainQuery, resultType, operator, nested);
    }

    @Override
    protected LeafOngoingSetOperationCriteriaBuilder<T> createSetOperand(FinalSetOperationCriteriaBuilderImpl<T> finalSetOperationBuilder) {
        return new LeafOngoingSetOperationCriteriaBuilderImpl<T>(mainQuery, false, resultType, finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingSetOperationCriteriaBuilder<T>> createSubquerySetOperand(FinalSetOperationCriteriaBuilderImpl<T> finalSetOperationBuilder, FinalSetOperationCriteriaBuilderImpl<T> resultFinalSetOperationBuilder) {
        LeafOngoingSetOperationCriteriaBuilderImpl<T> leafCb = new LeafOngoingSetOperationCriteriaBuilderImpl<T>(mainQuery, false, resultType, resultFinalSetOperationBuilder);
        return new OngoingSetOperationCriteriaBuilderImpl<T, LeafOngoingSetOperationCriteriaBuilder<T>>(mainQuery, false, resultType, finalSetOperationBuilder, leafCb);
    }

}
