/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder that can terminate the build process for general case when expressions.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CaseWhenThenBuilder<T extends CaseWhenBuilder<?>> {

    /**
     * Adds the constructed when expression with the then expression to the case when builder.
     *
     * @param expression The then expression
     * @return This case when builder
     */
    public T thenExpression(String expression);

    /**
     * Adds the constructed when expression with the then parameter value to the case when builder rendered as literal.
     *
     * @param value The then parameter value
     * @return This case when builder
     * @since 1.4.0
     */
    public T thenLiteral(Object value);

    /**
     * Adds the constructed when expression with the then parameter value to the case when builder.
     *
     * @param value The then parameter value
     * @return This case when builder
     */
    public T then(Object value);
    
    // TODO: add subqueries variants?
}
