/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

/**
 * An {@link javax.persistence.criteria.Expression} for a function.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.6.4
 */
public interface BlazeFunctionExpression<X> extends BlazeExpression<X> {

    /**
     * Returns the name of the function.
     *
     * @return the name of the function
     */
    public String getFunctionName();
}
