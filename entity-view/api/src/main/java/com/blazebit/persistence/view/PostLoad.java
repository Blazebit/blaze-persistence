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
 * Annotation to mark a method to be executed after the loading of an entity view.
 * A method annotated with <code>@PostLoad</code> must define no or a single parameter of the type {@link EntityViewManager}.
 * There may only be one method in a class annotated with <code>@PostLoad</code> and it must return <code>void</code>.
 * Super type methods annotated with <code>@PostLoad</code> are ignored if an entity view defines a <code>@PostLoad</code> method.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostLoad {
}
