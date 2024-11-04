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
 * Defines the inheritance selection for the entity view when a super type is queried.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewInheritanceMapping {

    /**
     * The selection predicate which is used to test whether this entity view type should be used as target type for an entity.
     *
     * @return The selection predicate
     */
    String value();
}
