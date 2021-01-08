/*
 * Copyright 2014 - 2021 Blazebit.
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
 * Annotation to declare an entity view listener that should be registered for the given entity view type.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityViewListener {

    /**
     * The entity view class for which to register this listener.
     * When using the default, <code>Object</code>, the listener is only registered for view types that are compatible with the type variable assignment.
     *
     * @return The entity view class
     */
    Class<?> entityView() default Object.class;

    /**
     * The entity class for which to register this listener.
     * When using the default, <code>Object</code>, the listener is only registered for entity types that are compatible with the type variable assignment.
     *
     * @return The entity class
     */
    Class<?> entity() default Object.class;
}
