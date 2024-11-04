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
 * @since 1.1.0
 */
public class InsertCriteriaBuilderImpl<T> extends BaseInsertCriteriaBuilderImpl<T, InsertCriteriaBuilder<T>, Void> implements InsertCriteriaBuilder<T> {

    public InsertCriteriaBuilderImpl(MainQuery mainQuery, Class<T> clazz) {
        super(mainQuery, null, true, clazz, null, null, null, null);
    }

    @Override
    AbstractCommonQueryBuilder<T, InsertCriteriaBuilder<T>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> copy(QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        throw new UnsupportedOperationException("This should only be used on CTEs!");
    }
}
