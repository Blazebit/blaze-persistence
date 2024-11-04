/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public final class Order {

    private final String expression;
    private final boolean ascending;
    private final boolean nullsFirst;

    public Order(String expression, Boolean ascending, Boolean nullsFirst) {
        this.expression = expression;

        if (Boolean.FALSE.equals(ascending)) {
            this.ascending = false;
            // Default NULLS FIRST
            if (nullsFirst == null) {
                this.nullsFirst = true;
            } else {
                this.nullsFirst = nullsFirst;
            }
        } else {
            this.ascending = true;
            // Default NULLS LAST
            if (nullsFirst == null) {
                this.nullsFirst = false;
            } else {
                this.nullsFirst = nullsFirst;
            }
        }
    }

    public String getExpression() {
        return expression;
    }

    public boolean isAscending() {
        return ascending;
    }

    public boolean isNullsFirst() {
        return nullsFirst;
    }
}
