/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies multiple {@link GraphQLDefaultFetch} elements.
 *
 * @author Christian Beikov
 * @since 1.6.16
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLDefaultFetches {
    /**
     * The default fetches.
     *
     * @return The default fetches
     */
    GraphQLDefaultFetch[] value() default {};
}
