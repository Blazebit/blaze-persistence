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

import com.blazebit.persistence.ReturningInsertCriteriaBuilder;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ReturningInsertCollectionCriteriaBuilderImpl<T, Y> extends AbstractInsertCollectionCriteriaBuilder<T, ReturningInsertCriteriaBuilder<T, Y>, Y> implements ReturningInsertCriteriaBuilder<T, Y> {

    public ReturningInsertCollectionCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, false, clazz, cteName, cteClass, result, listener, collectionName);
    }

    public ReturningInsertCollectionCriteriaBuilderImpl(AbstractInsertCollectionCriteriaBuilder<T, ReturningInsertCriteriaBuilder<T, Y>, Y> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, ReturningInsertCriteriaBuilder<T, Y>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext) {
        return new ReturningInsertCollectionCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext);
    }

    @Override
    protected void buildExternalQueryString(StringBuilder sbSelectFrom) {
        super.buildExternalQueryString(sbSelectFrom);
        applyJpaReturning(sbSelectFrom);
    }

}
