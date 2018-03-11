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

package com.blazebit.persistence.deltaspike.data.rest;

import com.blazebit.persistence.deltaspike.data.Sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation, heavily inspired by Spring Data's <code>SortDefault</code>,
 * for configuring the {@link com.blazebit.persistence.deltaspike.data.Pageable} sort default values to use when no query parameters are given.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SortDefault {

    /**
     * The paths to use for <code>sort</code> of a {@link com.blazebit.persistence.deltaspike.data.Pageable} when no sort query parameter is defined.
     *
     * @return The default paths to sort by
     */
    String[] sort() default {};

    /**
     * The sort direction to use for <code>sort</code> of a {@link com.blazebit.persistence.deltaspike.data.Pageable} when no sort direction is defined for a path. Defaults to {@link com.blazebit.persistence.deltaspike.data.Sort.Direction#ASC}.
     *
     * @return The default direction to sort by
     */
    Sort.Direction direction() default Sort.Direction.ASC;

    /**
     * The null handling to use for <code>sort</code> of a {@link com.blazebit.persistence.deltaspike.data.Pageable} when no null handling is defined for a path. Defaults to {@link com.blazebit.persistence.deltaspike.data.Sort.NullHandling#NATIVE}.
     *
     * @return The default null handling to use for sorting
     */
    Sort.NullHandling nulls() default Sort.NullHandling.NATIVE;

    /**
     * Wrapper annotation to allow declaring multiple {@link SortDefault} annotations on a method parameter.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface SortDefaults {

        /**
         * The individual {@link SortDefault} declarations to be sorted by.
         *
         * @return The sort defaults
         */
        SortDefault[] value();
    }
}
