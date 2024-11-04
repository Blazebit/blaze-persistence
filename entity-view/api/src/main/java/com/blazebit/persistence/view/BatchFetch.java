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
 * If {@link FetchStrategy#SELECT} is used on a property, this annotation configures the default batching.
 * Beware that if multiple properties of an entity view use {@link FetchStrategy#SELECT},
 * they will only be loaded together in one batch if the batch sizes match.
 *
 * The batch fetch annotation can also be applied on the entity view class level which makes it's value the default for all properties.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchFetch {

    /**
     * The size of the batch.
     *
     * @return The batch size
     */
    int size();
}
