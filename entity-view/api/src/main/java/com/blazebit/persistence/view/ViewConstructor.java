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
 * A naming mechanism for entity view constructors. The {@linkplain ViewConstructor} annotation can be applied to constructors
 * of an entity view. It is necessary to use them if an entity view has more than one constructor.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewConstructor {

    /**
     * The name of the view constructor which should be unique within the entity view.
     *
     * @return The name of the view constructor
     */
    String value();
}
