/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

/**
 * An {@link javax.persistence.criteria.Expression} for a window enabled function.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.6.4
 */
public interface BlazeWindowFunctionExpression<X> extends BlazeFunctionExpression<X> {

    /**
     * Returns the window for this window function.
     *
     * @return the window
     */
    public BlazeWindow getWindow();

    /**
     * Sets the window for this window function.
     *
     * @param window The window to set
     * @return <code>this</code> for method chaining
     */
    public BlazeWindowFunctionExpression<X> window(BlazeWindow window);
}
