/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

/**
 * An order by item.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class OrderByItem {

    private final String expression;
    private final boolean ascending;
    private final boolean nullsFirst;

    /**
     * Creates a new order by item.
     *
     * @param expression The order expression
     * @param ascending Whether the ordering should be ascending
     * @param nullsFirst Whether nulls should be ordered first
     */
    public OrderByItem(String expression, boolean ascending, boolean nullsFirst) {
        this.expression = expression;
        this.ascending = ascending;
        this.nullsFirst = nullsFirst;
    }

    /**
     * Returns the order expression.
     *
     * @return the order expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Returns whether the ordering should be ascending.
     *
     * @return whether the ordering should be ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Returns whether nulls should be ordered first.
     *
     * @return whether nulls should be ordered first
     */
    public boolean isNullsFirst() {
        return nullsFirst;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderByItem)) {
            return false;
        }

        OrderByItem that = (OrderByItem) o;

        if (isAscending() != that.isAscending()) {
            return false;
        }
        if (isNullsFirst() != that.isNullsFirst()) {
            return false;
        }
        return getExpression().equals(that.getExpression());
    }

    @Override
    public int hashCode() {
        int result = getExpression().hashCode();
        result = 31 * result + (isAscending() ? 1 : 0);
        result = 31 * result + (isNullsFirst() ? 1 : 0);
        return result;
    }
}
