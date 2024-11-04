/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder;


import com.blazebit.persistence.deltaspike.data.impl.builder.result.EntityViewDefaultQueryResult;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.WrappedQueryBuilder} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class WrappedEntityViewQueryBuilder extends EntityViewQueryBuilder {

    private final EntityViewQueryBuilder delegate;

    public WrappedEntityViewQueryBuilder(EntityViewQueryBuilder delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object execute(EntityViewCdiQueryInvocationContext ctx) {
        return new EntityViewDefaultQueryResult(delegate, ctx);
    }
}