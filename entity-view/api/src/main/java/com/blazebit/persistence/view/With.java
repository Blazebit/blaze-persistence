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
 * Adds {@link CTEProvider}s that should be invoked when applying the annotated entity view.
 *
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface With {

    /**
     * The CTE providers.
     *
     * @return the CTE providers
     */
    Class<? extends CTEProvider>[] value();

}
