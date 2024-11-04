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
 * Annotation to mark a method to be executed after a call to {@link EntityManager#persist(Object)} when flushing
 * the creatable entity view containing this method via {@link EntityViewManager#save(EntityManager, Object)}.
 * A method annotated with <code>@PostPersist</code> may optionally have the following parameters
 * <ul>
 *     <li>An {@link EntityViewManager}</li>
 *     <li>An {@link EntityManager}</li>
 *     <li>The entity object. The entity type of the entity view and all super types are allowed.</li>
 * </ul>
 * There may only be one method in a class annotated with <code>@PostPersist</code> and it must return <code>void</code>.
 * Super type methods annotated with <code>@PostPersist</code> are ignored if an entity view defines a <code>@PostPersist</code> method.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostPersist {
}
