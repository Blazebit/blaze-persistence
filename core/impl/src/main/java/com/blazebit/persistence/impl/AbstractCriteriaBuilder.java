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

import com.blazebit.persistence.BaseCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.SetOperationType;

/**
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @param <Z> The builder type that should be returned on set operations
 * @param <W> The builder type that should be returned on subquery set operations
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractCriteriaBuilder<T, X extends BaseCriteriaBuilder<T, X>, Z, W> extends AbstractCommonQueryBuilder<T, X, Z, W, BaseFinalSetOperationCriteriaBuilderImpl<T, ?>> {
    
    protected final BuilderListener<Object> listener;
    protected final BuilderListenerImpl<Object> subListener;

    public AbstractCriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, BuilderListener<Object> listener, BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder) {
        super(mainQuery, queryContext, isMainQuery, DbmsStatementType.SELECT, clazz, alias, finalSetOperationBuilder);
        this.listener = listener;
        this.subListener = new BuilderListenerImpl<Object>();
    }

    public AbstractCriteriaBuilder(AbstractCommonQueryBuilder<T, ?, ?, ?, ?> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.listener = null;
        this.subListener = null;
    }

    public BuilderListenerImpl<Object> getSubListener() {
        return subListener;
    }

    protected BaseFinalSetOperationCriteriaBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested, boolean isSubquery) {
        boolean wasMainQuery = isMainQuery;
        this.isMainQuery = false;
        BuilderListener<Object> newListener = finalSetOperationBuilder == null ? null : finalSetOperationBuilder.getSubListener();
        
        if (isSubquery) {
            return new OngoingFinalSetOperationCriteriaBuilderImpl<T>(mainQuery, queryContext, wasMainQuery, resultType, operator, nested, newListener);
        } else {
            return new FinalSetOperationCriteriaBuilderImpl<T>(mainQuery, queryContext, wasMainQuery, resultType, operator, nested, newListener);
        }
    }

    @SuppressWarnings("unchecked")
    protected LeafOngoingSetOperationCriteriaBuilderImpl<T> createLeaf(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder) {
        BuilderListener<Object> newListener = finalSetOperationBuilder.getSubListener();
        LeafOngoingSetOperationCriteriaBuilderImpl<T> next = new LeafOngoingSetOperationCriteriaBuilderImpl<T>(mainQuery, queryContext, false, resultType, newListener, (FinalSetOperationCriteriaBuilderImpl<T>) finalSetOperationBuilder);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <Y> StartOngoingSetOperationCriteriaBuilderImpl<T, Y> createStartOngoing(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder, Y endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);
        
        BuilderListener<Object> newListener = finalSetOperationBuilder.getSubListener();
        StartOngoingSetOperationCriteriaBuilderImpl<T, Y> next = new StartOngoingSetOperationCriteriaBuilderImpl<T, Y>(mainQuery, queryContext, false, resultType, newListener, (OngoingFinalSetOperationCriteriaBuilderImpl<T>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <Y> OngoingSetOperationCriteriaBuilderImpl<T, Y> createOngoing(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder, Y endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);

        BuilderListener<Object> newListener = finalSetOperationBuilder.getSubListener();
        OngoingSetOperationCriteriaBuilderImpl<T, Y> next = new OngoingSetOperationCriteriaBuilderImpl<T, Y>(mainQuery, queryContext, false, resultType, newListener, (OngoingFinalSetOperationCriteriaBuilderImpl<T>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

}
