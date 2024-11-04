/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.AttributeFilterProvider;

/**
 * Represents the mapping of a named filter on an entity view attribute.
 *
 * @param <X> The source type
 * @param <FilterValue> The filter value type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface AttributeFilterMapping<X, FilterValue> extends FilterMapping<AttributeFilterProvider<FilterValue>> {
    
    /**
     * Returns the declaring attribute.
     *
     * @return The declaring attribute
     */
    public MethodAttribute<X, ?> getDeclaringAttribute();
    
}
