/*
 * Copyright 2014 - 2023 Blazebit.
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
 * Annotation to mark a method to be executed before removing an entity view due to orphan removal, delete cascading or removal.
 * A method annotated with <code>@PreRemove</code> may optionally have the following parameters
 * <ul>
 *     <li>An {@link EntityViewManager}</li>
 *     <li>An {@link EntityManager}</li>
 * </ul>
 * There may only be one method in a class annotated with <code>@PreRemove</code> and it may return <code>boolean</code> or <code>void</code>.
 * If it declares a <code>boolean</code> return type, returning <code>true</code> will cause the removal operation to be done and
 * returning <code>false</code> will cause the removal operation to be cancelled.
 * Super type methods annotated with <code>@PreRemove</code> are ignored if an entity view defines a <code>@PreRemove</code> method.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PreRemove {
}
