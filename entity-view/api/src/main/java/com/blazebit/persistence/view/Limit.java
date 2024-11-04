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
 * Limits the amount of elements to fetch for the annotated attribute.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
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
     * Paths that are not fully qualified i.e. relative paths that use no root alias, are prefixed with the mapping result.
     *
     * @return order to use for the limit
     */
    String[] order();
}
