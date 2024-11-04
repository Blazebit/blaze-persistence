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
 * Annotation to specify whether a change of the annotated attribute will cause optimistic locking.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OptimisticLock {

    /**
     * Whether a change of the annotated attribute should cause an optimistic lock.
     * When <code>true</code>, a change will <b>NOT</b> cause an optimistic lock.
     *
     * @return <code>false</code> if an attribute change should cause optimistic locking, <code>true</code> otherwise
     */
    public boolean exclude() default false;
}
