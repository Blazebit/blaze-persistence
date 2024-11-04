/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure the EntityViewManager for a specific repository.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EntityViewManagerConfig {
    /**
     * References the type which provides the EntityViewManager for a specific repository.
     * Must be resolvable over the BeanManager.
     *
     * @return The entity manager resolver class
     */
    Class<? extends EntityViewManagerResolver> entityViewManagerResolver() default EntityViewManagerResolver.class;
}
