/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewJpaQueryPostProcessor;

import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.postprocessor.LockModePostProcessor} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class LockModePostProcessor implements EntityViewJpaQueryPostProcessor {

    private final LockModeType lockMode;

    public LockModePostProcessor(LockModeType lockMode) {
        this.lockMode = lockMode;
    }

    @Override
    public Query postProcess(EntityViewCdiQueryInvocationContext context, Query query) {
        query.setLockMode(lockMode);
        return query;
    }
}