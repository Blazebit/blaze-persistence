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
 * Annotation to register an entity view listener for multiple entity view types.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewListeners {

    /**
     * The entity view listener definitions.
     *
     * @return The entity view listener definitions
     */
    EntityViewListener[] value();
}
