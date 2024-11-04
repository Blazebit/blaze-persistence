/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class OrderByItem {

    private final boolean ascending;
    private final boolean nullFirst;
    private Expression expression;

    public OrderByItem(boolean ascending, boolean nullFirst, Expression expression) {
        this.ascending = ascending;
        this.nullFirst = nullFirst;
        this.expression = expression;
    }

    public OrderByItem copy(ExpressionCopyContext copyContext) {
        return new OrderByItem(ascending, nullFirst, expression.copy(copyContext));
    }

    public boolean isAscending() {
        return ascending;
    }

    public boolean isDescending() {
        return !ascending;
    }

    public boolean isNullFirst() {
        return nullFirst;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.ascending ? 1 : 0);
        hash = 37 * hash + (this.nullFirst ? 1 : 0);
        hash = 37 * hash + (this.expression != null ? this.expression.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderByItem other = (OrderByItem) obj;
        if (this.ascending != other.ascending) {
            return false;
        }
        if (this.nullFirst != other.nullFirst) {
            return false;
        }
        if (this.expression != other.expression && (this.expression == null || !this.expression.equals(other.expression))) {
            return false;
        }
        return true;
    }
}
