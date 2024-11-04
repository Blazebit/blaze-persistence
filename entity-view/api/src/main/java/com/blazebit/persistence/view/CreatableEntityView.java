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
 * Specifies that the class is a creatable entity view.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CreatableEntityView {

    /**
     * Specifies whether the persistability based on the updatable attributes should be validated on startup.
     *
     * @return Whether persistability should be validated
     */
    public boolean validatePersistability() default true;

    /**
     * A set of entity attributes that should be excluded from the persistability validation.
     *
     * @return Entity attributes that are excluded from validation
     */
    public String[] excludedEntityAttributes() default {};

}
