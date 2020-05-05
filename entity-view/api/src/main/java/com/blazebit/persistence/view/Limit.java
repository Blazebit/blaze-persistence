/*
 * Copyright 2014 - 2020 Blazebit.
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
 * Limits the amount of elements to fetch for the annotated attribute.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {

    /**
     * The maximum amount of elements to fetch for the annotated attribute.
     * Can be an integer literal e.g. <code>5</code> or a parameter expression <code>:myParam</code>.
     *
     * @return The limit
     */
    String limit();

    /**
     * The amount of elements to skip for the annotated attribute.
     * Can be an integer literal e.g. <code>5</code> or a parameter expression <code>:myParam</code>.
     *
     * @return The offset
     */
    String offset() default "";

    /**
     * The order to use for the elements for the limit. This will not necessarily order the elements in a collection!
     * The syntax is like for a JPQL.next order by item i.e. something like <code>age DESC NULLS LAST</code>.
     *
     * @return order to use for the limit
     */
    String[] order();
}
