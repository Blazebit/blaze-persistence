/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for simple case when expressions.
 *
 * The left hand expression also referred to as case operand, will be compared to the when expressions defined via
 * {@link SimpleCaseWhenBuilder#when(java.lang.String, java.lang.String)}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SimpleCaseWhenBuilder<T> extends SimpleCaseWhenStarterBuilder<T> {

    /**
     * Adds the given else expression to the case when builder.
     *
     * @param elseExpression The else expression
     * @return The parent builder
     */
    public T otherwise(String elseExpression);

    /**
     * Adds the given else value to the case when builder rendered as literal.
     *
     * @param elseValue The else value
     * @return The parent builder
     * @since 1.4.0
     */
    public T otherwiseLiteral(Object elseValue);

    // TODO: otherwiseValue?
    // TODO: subqueries?
}
