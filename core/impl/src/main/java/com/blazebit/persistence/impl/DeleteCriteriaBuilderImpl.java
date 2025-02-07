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
 * @since 1.1.0
 */
public class DeleteCriteriaBuilderImpl<T> extends BaseDeleteCriteriaBuilderImpl<T, DeleteCriteriaBuilder<T>, Void> implements DeleteCriteriaBuilder<T> {

    public DeleteCriteriaBuilderImpl(MainQuery mainQuery, Class<T> clazz, String alias) {
        super(mainQuery, null, true, clazz, alias, null, null, null, null);
    }

    @Override
    AbstractCommonQueryBuilder<T, DeleteCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        throw new UnsupportedOperationException("This should only be used on CTEs!");
    }
}
