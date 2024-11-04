/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.AttributeFilterProvider;
import com.blazebit.persistence.view.ViewFilterProvider;

/**
 * Represents the mapping of a named filter.
 *
 * @param <T> The base filter type which is either {@linkplain AttributeFilterProvider} or {@linkplain ViewFilterProvider}. 
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface FilterMapping<T> {
    
    /**
     * Returns the name of the filter.
     * 
     * @return The name of the filter
     */
    public String getName();
    
    /**
     * Returns true if this is a {@link ViewFilterMapping}, false if this is a {@link AttributeFilterMapping}.
     * 
     * @return True if this is a {@link ViewFilterMapping}, false if this is a {@link AttributeFilterMapping}
     */
    public boolean isViewFilter();
    
    /**
     * Returns the filter class.
     * 
     * @return The filter class
     */
    public Class<? extends T> getFilterClass();
}
