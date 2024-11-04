/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UpdateCollectionCriteriaBuilderImpl<T> extends AbstractUpdateCollectionCriteriaBuilder<T, UpdateCriteriaBuilder<T>, Void> implements UpdateCriteriaBuilder<T> {

    public UpdateCollectionCriteriaBuilderImpl(MainQuery mainQuery, Class<T> updateOwnerClass, String alias, String collectionName) {
        super(mainQuery, null, true, updateOwnerClass, alias, null, null, null, null, collectionName);
    }

    public UpdateCollectionCriteriaBuilderImpl(AbstractUpdateCollectionCriteriaBuilder<T, UpdateCriteriaBuilder<T>, Void> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    AbstractCommonQueryBuilder<T, UpdateCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        return new UpdateCollectionCriteriaBuilderImpl<>(this, queryContext.getParent().mainQuery, queryContext, joinManagerMapping, copyContext);
    }
}
