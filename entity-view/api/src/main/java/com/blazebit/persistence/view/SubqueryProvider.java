/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import com.blazebit.persistence.SubqueryInitiator;

/**
 * Provides a subquery to a {@link SubqueryInitiator}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SubqueryProvider {

    /**
     * Applies a subquery to the given {@link SubqueryInitiator}.
     *
     * @param subqueryInitiator The subquery initiator on which the subquery should be applied
     * @param <T>               The type of the parent builder
     * @return The parent builder
     */
    public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator);
}
