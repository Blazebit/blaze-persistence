/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewJpaQueryPostProcessor;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.postprocessor.FlushModePostProcessor} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class FlushModePostProcessor implements EntityViewJpaQueryPostProcessor {

    private final FlushModeType flushMode;

    public FlushModePostProcessor(FlushModeType flushMode) {
        this.flushMode = flushMode;
    }

    @Override
    public Query postProcess(EntityViewCdiQueryInvocationContext context, Query query) {
        query.setFlushMode(flushMode);
        return query;
    }
}