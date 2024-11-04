/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for general case when expressions.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CaseWhenBuilder<T> extends CaseWhenStarterBuilder<T> {

    /**
     * Adds the given else expression to the case when builder.
     *
     * @param elseExpression The else expression
     * @return The parent builder
     */
    public T otherwiseExpression(String elseExpression);

    /**
     * Adds the given else parameter value to the case when builder rendered as literal.
     *
     * @param value The else parameter value
     * @return The parent builder
     * @since 1.4.0
     */
    public T otherwiseLiteral(Object value);

    /**
     * Adds the given else parameter value to the case when builder.
     *
     * @param value The else parameter value
     * @return The parent builder
     */
    public T otherwise(Object value);
    
    // TODO: add subqueries variants?
}
