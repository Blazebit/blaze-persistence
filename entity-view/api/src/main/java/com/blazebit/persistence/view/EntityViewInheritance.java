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
 * Specifies that the entity view should consider subtypes of the entity view when queried.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewInheritance {

    /**
     * The entity view subtypes that may be considered when querying.
     * An empty value has the meaning of all (registered) subtypes.
     *
     * @return The entity view subtype classes
     */
    Class<?>[] value() default {};
}
