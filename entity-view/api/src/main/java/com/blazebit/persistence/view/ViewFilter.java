/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a named filter to an entity view.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ViewFilters.class)
public @interface ViewFilter {
    
    /**
     * Returns the name of the filter.
     * 
     * @return The name of the filter
     */
    String name();

    /**
     * The filter class that should be used for filtering.
     *
     * @return The filter class
     */
    Class<? extends ViewFilterProvider> value();
}
