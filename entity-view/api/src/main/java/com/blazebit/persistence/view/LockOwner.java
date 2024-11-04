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
 * Annotation to specify the owner of a lock that should be used for locking.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface LockOwner {

    /**
     * The attribute path to the actual lock owner, relative to the annotated entity view type's entity.
     *
     * @return The attribute path to the lock owner
     */
    public String value();
}
