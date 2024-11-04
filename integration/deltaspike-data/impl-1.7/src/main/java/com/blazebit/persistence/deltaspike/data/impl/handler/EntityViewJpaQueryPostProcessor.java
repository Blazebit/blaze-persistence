/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.handler;

import javax.persistence.Query;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.handler.JpaQueryPostProcessor} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface EntityViewJpaQueryPostProcessor {

    Query postProcess(EntityViewCdiQueryInvocationContext context, Query query);
}