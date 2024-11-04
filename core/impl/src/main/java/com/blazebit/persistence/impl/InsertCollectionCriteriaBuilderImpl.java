/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InsertCollectionCriteriaBuilderImpl<T> extends AbstractInsertCollectionCriteriaBuilder<T, InsertCriteriaBuilder<T>, Void> implements InsertCriteriaBuilder<T> {

    public InsertCollectionCriteriaBuilderImpl(MainQuery mainQuery, Class<T> clazz, String collectionName) {
        super(mainQuery, null, true, clazz, null, null, null, null, collectionName);
    }

    @Override
    AbstractCommonQueryBuilder<T, InsertCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        throw new UnsupportedOperationException("This should only be used on CTEs!");
    }
}
