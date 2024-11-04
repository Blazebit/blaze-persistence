/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewJpaQueryPostProcessor;

import javax.persistence.Query;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.postprocessor.MaxResultPostProcessor} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MaxResultPostProcessor implements EntityViewJpaQueryPostProcessor {

    private final int max;

    public MaxResultPostProcessor(int max) {
        this.max = max;
    }

    @Override
    public Query postProcess(EntityViewCdiQueryInvocationContext context, Query query) {
        query.setMaxResults(max);
        return query;
    }
}