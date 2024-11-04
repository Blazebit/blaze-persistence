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
 * Defines a possible subtype that should be materialized when the selection predicate is satisfied.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingInheritanceSubtype {

    /**
     * The selection predicate which is used to test whether the entity view type denoted by {@link #value()} should be used as target type for an entity.
     * The default is to use the predicate defined on the entity view type if it is defined.
     *
     * @return The selection predicate
     */
    String mapping() default "";

    /**
     * The entity view subtype of the annotated subview attribute.
     *
     * @return The entity view subtype
     */
    Class<?> value();
}
