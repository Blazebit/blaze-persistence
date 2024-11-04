/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import javax.persistence.EntityManager;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method to be executed after a transaction that flushes the type containing this method was rolled back.
 * A method annotated with <code>@PostRollback</code> may optionally have the following parameters
 * <ul>
 *     <li>An {@link EntityViewManager}</li>
 *     <li>An {@link EntityManager}</li>
 *     <li>A {@link ViewTransition}</li>
 * </ul>
 * There may only be one method in a class annotated with <code>@PostRollback</code> and it must return <code>void</code>.
 * Super type methods annotated with <code>@PostRollback</code> are ignored if an entity view defines a <code>@PostRollback</code> method.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostRollback {
    /**
     * The view transitions to handle by this listener.
     *
     * @return the view transitions
     */
    ViewTransition[] transitions() default { ViewTransition.PERSIST, ViewTransition.UPDATE, ViewTransition.REMOVE };
}
