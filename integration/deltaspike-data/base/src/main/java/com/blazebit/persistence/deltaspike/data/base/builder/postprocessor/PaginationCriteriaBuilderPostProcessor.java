/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.postprocessor;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.deltaspike.data.base.handler.CriteriaBuilderPostProcessor;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class PaginationCriteriaBuilderPostProcessor implements CriteriaBuilderPostProcessor {

    private final int firstResult;
    private final int maxResults;

    public PaginationCriteriaBuilderPostProcessor(int firstResult, int maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public FullQueryBuilder<?, ?> postProcess(FullQueryBuilder<?, ?> queryBuilder) {
        return queryBuilder.page(firstResult, maxResults);
    }
}