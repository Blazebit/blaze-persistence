/*
 * Copyright 2014 - 2018 Blazebit.
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
 * Defines a possible subtype that should be materialized when the selection predicate is satisfied.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingInheritanceSubtype {

    /**
     * The selection predicate which is used to test whether the entity view type denoted by {@link #value()} should be used as target type for an entity.
     * The default is to use the predicate defined on the entity view type if it is defined.
     *
     * @return The selection predicate
     */
    String mapping() default "";

    /**
     * The entity view subtype of the annotated subview attribute.
     *
     * @return The entity view subtype
     */
    Class<?> value();
}
