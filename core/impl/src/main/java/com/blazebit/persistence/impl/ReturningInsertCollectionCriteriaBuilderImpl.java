/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ReturningInsertCollectionCriteriaBuilderImpl<T, Y> extends AbstractInsertCollectionCriteriaBuilder<T, ReturningInsertCriteriaBuilder<T, Y>, Y> implements ReturningInsertCriteriaBuilder<T, Y> {

    public ReturningInsertCollectionCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, Class<T> clazz, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener, String collectionName) {
        super(mainQuery, queryContext, false, clazz, cteKey, cteClass, result, listener, collectionName);
    }

    public ReturningInsertCollectionCriteriaBuilderImpl(AbstractInsertCollectionCriteriaBuilder<T, ReturningInsertCriteriaBuilder<T, Y>, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, ReturningInsertCriteriaBuilder<T, Y>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new ReturningInsertCollectionCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected void buildExternalQueryString(StringBuilder sbSelectFrom) {
        super.buildExternalQueryString(sbSelectFrom);
        applyJpaReturning(sbSelectFrom);
    }

}
