/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import com.blazebit.persistence.WhereBuilder;

/**
 * A view filter provider is an object that applies restrictions on a {@link WhereBuilder}.
 * 
 * View filter providers must have a no-arg constructor if they are used in conjunction with {@link ViewFilter}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class ViewFilterProvider {

    /**
     * Applies restrictions on the given where builder.
     *
     * @param <T>                 The actual type of the where builder
     * @param whereBuilder        The where builder on which the restrictions should be applied
     * @return The where builder after applying restrictions
     */
    public abstract <T extends WhereBuilder<T>> T apply(T whereBuilder);
}
