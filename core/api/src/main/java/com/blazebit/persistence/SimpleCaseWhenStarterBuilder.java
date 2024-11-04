/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * The first builder for simple case when expressions.
 *
 * This builder is used to enforce the correct usage of case when by disallowing an immediate call to
 * {@link SimpleCaseWhenBuilder#otherwise(java.lang.String)}.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SimpleCaseWhenStarterBuilder<T> {

    /**
     * Adds the given when expression with the then expression to the case when builder.
     *
     * @param expression The when expression
     * @param thenExpression The then expression
     * @return This simple case when builder
     */
    public SimpleCaseWhenBuilder<T> when(String expression, String thenExpression);
    // TODO: literal variants? or wait until we know the LHS type?
    // TODO: subqueries?
}
