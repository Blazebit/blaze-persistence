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
 * Adds a named filter to an entity view attribute.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AttributeFilters.class)
public @interface AttributeFilter {
    String DEFAULT_NAME = "";
    /**
     * Returns the name of the filter. There may be one default element which defaults to the attribute name.
     * 
     * @return The name of the filter
     */
    String name() default DEFAULT_NAME;

    /**
     * The filter class that should be used for filtering.
     *
     * @return The filter class
     */
    Class<? extends AttributeFilterProvider> value();
}
