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
 * Defines the subtype mappings for a subview attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingInheritance {

    /**
     * Specifies that the base type is not considered in the inheritance subtype selection, but only it's subtypes.
     *
     * @return Whether the base type should be considered for inheritance subtype selection
     */
    boolean onlySubtypes() default false;

    /**
     * The subtype mappings of this annotated attribute that should be considered for inheritance.
     *
     * @return The subtype mappings
     */
    MappingInheritanceSubtype[] value();
}
