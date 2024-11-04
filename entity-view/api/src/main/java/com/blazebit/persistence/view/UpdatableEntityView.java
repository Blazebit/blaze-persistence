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
 * Specifies that the class is an updatable entity view.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdatableEntityView {

    /**
     * Specifies the flush mode to use for the updatable entity view.
     *
     * @return The flush mode
     * @since 1.2.0
     */
    public FlushMode mode() default FlushMode.LAZY;

    /**
     * The strategy to use for flushing changes to the JPA model.
     *
     * @return The flush strategy
     * @since 1.2.0
     */
    public FlushStrategy strategy() default FlushStrategy.QUERY;

    /**
     * The lock mode to use for the updatable entity view.
     *
     * @return The lock mode
     * @since 1.2.0
     */
    public LockMode lockMode() default LockMode.AUTO;
}
