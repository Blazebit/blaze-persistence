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
 * Annotation to declare an entity view listener that should be registered for the given entity view type.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewListener {

    /**
     * The entity view class for which to register this listener.
     * When using the default, <code>Object</code>, the listener is only registered for view types that are compatible with the type variable assignment.
     *
     * @return The entity view class
     */
    Class<?> entityView() default Object.class;

    /**
     * The entity class for which to register this listener.
     * When using the default, <code>Object</code>, the listener is only registered for entity types that are compatible with the type variable assignment.
     *
     * @return The entity class
     */
    Class<?> entity() default Object.class;
}
