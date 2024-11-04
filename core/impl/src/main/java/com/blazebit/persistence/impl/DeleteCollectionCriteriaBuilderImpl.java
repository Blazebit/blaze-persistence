/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DeleteCollectionCriteriaBuilderImpl<T> extends AbstractDeleteCollectionCriteriaBuilder<T, DeleteCriteriaBuilder<T>, Void> implements DeleteCriteriaBuilder<T> {

    public DeleteCollectionCriteriaBuilderImpl(MainQuery mainQuery, Class<T> deleteOwnerClass, String alias, String collectionName) {
        super(mainQuery, null, true, deleteOwnerClass, alias, null, null, null, null, collectionName);
    }

    @Override
    AbstractCommonQueryBuilder<T, DeleteCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        throw new UnsupportedOperationException("This should only be used on CTEs!");
    }
}
