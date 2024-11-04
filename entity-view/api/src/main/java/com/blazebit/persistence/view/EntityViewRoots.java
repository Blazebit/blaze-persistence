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
 * Annotation to register entity view roots.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewRoots {

    /**
     * The entity view root definitions.
     *
     * @return The entity view root definitions
     */
    EntityViewRoot[] value();
}
