/*
 * Copyright 2014 - 2022 Blazebit.
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
