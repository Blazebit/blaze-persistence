/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
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
