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
 * Annotation, heavily inspired by Spring Data's <code>PageableDefault</code>,
 * for configuring the {@link com.blazebit.persistence.deltaspike.data.Pageable} default values to use when no query parameters are given.
 * It's illegal to supply both, a {@link SortDefault} or {@link com.blazebit.persistence.deltaspike.data.rest.SortDefault.SortDefaults} configuration
 * and {@link #sort()} and {@link #direction()}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PageableDefault {

    /**
     * The page size to use for a {@link com.blazebit.persistence.deltaspike.data.Pageable} when no page size query parameter is defined.
     *
     * @return The default page size
     */
    int size() default 10;

    /**
     * The page to use for a {@link com.blazebit.persistence.deltaspike.data.Pageable} when no page query parameter is defined.
     *
     * @return The default page
     */
    int page() default 0;

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
     * The query parameter name for the page parameter.
     *
     * @return The page query parameter name
     */
    String pageName() default "";

    /**
     * The query parameter name for the page size parameter.
     *
     * @return The page size query parameter name
     */
    String pageSizeName() default "";

    /**
     * The query parameter name for the sort parameter.
     *
     * @return The sort query parameter name
     */
    String sortName() default "";

    /**
     * Whether the page is 1-based rather than 0-based.
     *
     * @return <code>true</code> if 1-base, <code>false</code> if 0-based
     */
    boolean oneIndexed() default false;
}
