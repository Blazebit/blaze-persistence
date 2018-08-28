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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCriteriaBuilder;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.TypedQuery;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class CriteriaBuilderImpl<T> extends AbstractFullQueryBuilder<T, CriteriaBuilder<T>, LeafOngoingSetOperationCriteriaBuilder<T>, StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>>, BaseFinalSetOperationCriteriaBuilderImpl<T, ?>> implements CriteriaBuilder<T> {

    protected String cachedQueryRootCountQueryString;
    protected String cachedExternalQueryRootCountQueryString;

    public CriteriaBuilderImpl(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, String alias) {
        super(mainQuery, isMainQuery, clazz, alias, null);
    }

    @Override
    protected void prepareForModification(ClauseType changedClause) {
        super.prepareForModification(changedClause);
        cachedQueryRootCountQueryString = null;
        cachedExternalQueryRootCountQueryString = null;
    }

    @Override
    public TypedQuery<Long> getQueryRootCountQuery() {
        if (!havingManager.isEmpty()) {
            throw new IllegalStateException("Cannot count a HAVING query yet!");
        }
        return getCountQuery(getCountQueryRootQueryStringWithoutCheck());
    }

    @Override
    public String getQueryRootCountQueryString() {
        return getExternalQueryRootCountQueryString();
    }

    private String getCountQueryRootQueryStringWithoutCheck() {
        if (cachedQueryRootCountQueryString == null) {
            cachedQueryRootCountQueryString = buildPageCountQueryString(false, false);
        }

        return cachedQueryRootCountQueryString;
    }

    private String getExternalQueryRootCountQueryString() {
        if (cachedExternalQueryRootCountQueryString == null) {
            cachedExternalQueryRootCountQueryString = buildPageCountQueryString(true, false);
        }

        return cachedExternalQueryRootCountQueryString;
    }

    @Override
    public <Y> CriteriaBuilder<Y> copy(Class<Y> resultClass) {
        return (CriteriaBuilder<Y>) super.copy(resultClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<CriteriaBuilder<Y>>) super.selectNew(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (CriteriaBuilder<Y>) super.selectNew(builder);
    }

    @Override
    protected void verifySetBuilderEnded() {
        super.verifySetBuilderEnded();
        if (finalSetOperationBuilder != null) {
            throw new IllegalStateException("The original criteria builder should not be accessed anymore after connecting it with a set operation. Use the result of the set operation instead!");
        }
    }

    @Override
    protected BaseFinalSetOperationCriteriaBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested) {
        boolean wasMainQuery = isMainQuery;
        this.isMainQuery = false;
        BuilderListener<Object> newListener = finalSetOperationBuilder == null ? null : finalSetOperationBuilder.getSubListener();
        
        if (nested) {
            return new OngoingFinalSetOperationCriteriaBuilderImpl<T>(mainQuery, queryContext, wasMainQuery, resultType, operator, nested, newListener);
        } else {
            return new FinalSetOperationCriteriaBuilderImpl<T>(mainQuery, queryContext, wasMainQuery, resultType, operator, nested, newListener);
        }
    }

    @Override
    protected LeafOngoingSetOperationCriteriaBuilder<T> createSetOperand(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder) {
        return createLeaf(finalSetOperationBuilder);
    }

    @Override
    protected StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingFinalSetOperationCriteriaBuilder<T>> createSubquerySetOperand(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder, BaseFinalSetOperationCriteriaBuilderImpl<T, ?> resultFinalSetOperationBuilder) {
        LeafOngoingFinalSetOperationCriteriaBuilder<T> leafCb = createLeaf(resultFinalSetOperationBuilder);
        return createOngoing(finalSetOperationBuilder, leafCb);
    }

    @SuppressWarnings("unchecked")
    private LeafOngoingSetOperationCriteriaBuilderImpl<T> createLeaf(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder) {
        BuilderListener<Object> newListener = finalSetOperationBuilder.getSubListener();
        LeafOngoingSetOperationCriteriaBuilderImpl<T> next = new LeafOngoingSetOperationCriteriaBuilderImpl<T>(mainQuery, queryContext, false, resultType, newListener, (FinalSetOperationCriteriaBuilderImpl<T>) finalSetOperationBuilder);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    private <Y> StartOngoingSetOperationCriteriaBuilderImpl<T, Y> createOngoing(BaseFinalSetOperationCriteriaBuilderImpl<T, ?> finalSetOperationBuilder, Y endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);
        
        BuilderListener<Object> newListener = finalSetOperationBuilder.getSubListener();
        StartOngoingSetOperationCriteriaBuilderImpl<T, Y> next = new StartOngoingSetOperationCriteriaBuilderImpl<T, Y>(mainQuery, queryContext, false, resultType, newListener, (OngoingFinalSetOperationCriteriaBuilderImpl<T>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

}
