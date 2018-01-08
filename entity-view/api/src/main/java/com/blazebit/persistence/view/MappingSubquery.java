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
 * Maps the annotated attribute as subquery.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingSubquery {

    /**
     * The class which provides the subquery.
     *
     * @return The subquery provider
     */
    Class<? extends SubqueryProvider> value();

    /**
     * The expression around the subquery.
     *
     * @see com.blazebit.persistence.BaseQueryBuilder#selectSubquery(java.lang.String,java.lang.String,java.lang.String)
     * @return The expression
     */
    String expression() default "";

    /**
     * The subquery alias.
     *
     * @see com.blazebit.persistence.BaseQueryBuilder#selectSubquery(java.lang.String,java.lang.String,java.lang.String)
     * @return The subquery alias
     */
    String subqueryAlias() default "";
}
