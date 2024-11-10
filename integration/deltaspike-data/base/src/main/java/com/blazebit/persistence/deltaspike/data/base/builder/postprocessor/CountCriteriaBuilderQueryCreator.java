/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.postprocessor;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderQueryCreator;

import jakarta.persistence.Query;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CountCriteriaBuilderQueryCreator implements CriteriaBuilderQueryCreator {

    @Override
    public Query createQuery(FullQueryBuilder<?, ?> queryBuilder) {
        return queryBuilder.getCountQuery();
    }
}