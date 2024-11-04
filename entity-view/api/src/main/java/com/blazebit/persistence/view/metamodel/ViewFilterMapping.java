/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.ViewFilterProvider;

/**
 * Represents the mapping of a named filter on an entity view.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ViewFilterMapping extends FilterMapping<ViewFilterProvider> {
    
    /**
     * Returns the declaring view type.
     *
     * @return The declaring view type
     */
    public ViewType<?> getDeclaringType();
    
}
