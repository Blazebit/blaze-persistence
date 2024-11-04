/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.deltaspike.data.impl.builder.EntityViewQueryBuilder;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.handler.QueryRunner} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface EntityViewQueryRunner {

    Object executeQuery(EntityViewQueryBuilder builder, EntityViewCdiQueryInvocationContext context) throws Throwable;
}