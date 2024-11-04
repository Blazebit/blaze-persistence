/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds multiple {@linkplain ViewFilter}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewFilters {
    
    /**
     * Returns the filters.
     * 
     * @return The filters
     */
    ViewFilter[] value();
}
