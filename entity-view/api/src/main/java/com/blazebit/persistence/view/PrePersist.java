/*
 * Copyright 2014 - 2019 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view;

import javax.persistence.EntityManager;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method to be executed before a call to {@link javax.persistence.EntityManager#persist(Object)} when flushing
 * the creatable entity view containing this method via {@link EntityViewManager#save(EntityManager, Object)}.
 * A method annotated with <code>@PrePersist</code> may optionally have the following parameters
 * <ul>
 *     <li>An {@link EntityViewManager}</li>
 *     <li>An {@link EntityManager}</li>
 *     <li>The entity object. The entity type of the entity view and all super types are allowed.</li>
 * </ul>
 * There may only be one method in a class annotated with <code>@PrePersist</code> and it must return <code>void</code>.
 * Super type methods annotated with <code>@PrePersist</code> are ignored if an entity view defines a <code>@PrePersist</code> method.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PrePersist {
}
