/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The Priority annotation can be applied to classes to indicate in what order the {@link CriteriaBuilderConfigurationContributor}
 * should be registered.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
@Target(value = TYPE)
@Retention(value = RUNTIME)
@Documented
public @interface Priority {

    /**
     * The priority value. The range 0-500 is reserved for internal uses. 500 - 1000 is reserved for libraries and 1000+
     * is for user provided contributors.
     * @return The priority value.
     */
    int value();

}
