/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;


/**
 * Represents an order by element.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface OrderByElement {

    /**
     * The position of the order by expression in the select clause.
     *
     * @return The position
     */
    public int getPosition();

    /**
     * Whether sorting is ascending or descending.
     *
     * @return True if ascending, false otherwise
     */
    public boolean isAscending();

    /**
     * Whether the expression may produce null values.
     *
     * @return True if nullable, false otherwise
     * @since 1.3.0
     */
    public boolean isNullable();

    /**
     * Whether nulls have precedence or non-nulls.
     *
     * @return True if nulls come first, false otherwise
     */
    public boolean isNullsFirst();
    
}
