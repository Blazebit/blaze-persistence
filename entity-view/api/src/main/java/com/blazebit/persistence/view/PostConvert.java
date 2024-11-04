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
 * Annotation to mark a method to be executed after a call to {@link EntityViewManager#convert(Object, Class, ConvertOption...)}.
 * A method annotated with <code>@PostConvert</code> must define no or a single parameter for the source entity view of type Object.
 * A method annotated with <code>@PostConvert</code> may optionally have the following parameters
 * <ul>
 *     <li>An {@link EntityViewManager}</li>
 *     <li>A {@linkplain Object} for the source entity view object</li>
 * </ul>
 * There may only be one method in a class annotated with <code>@PostConvert</code> and it must return <code>void</code>.
 * Super type methods annotated with <code>@PostConvert</code> are ignored if an entity view defines a <code>@PostConvert</code> method.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConvert {
}
