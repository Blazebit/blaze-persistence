/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class OrderByExpression {

    private final boolean ascending;
    private final boolean nullFirst;
    private final Expression expression;
    private final boolean nullable;
    private final boolean unique;
    private final boolean resultUnique;

    public OrderByExpression(boolean ascending, boolean nullFirst, Expression expression, boolean nullable, boolean unique, boolean resultUnique) {
        this.ascending = ascending;
        this.nullFirst = nullFirst;
        this.expression = expression;
        this.nullable = nullable;
        this.unique = unique;
        this.resultUnique = resultUnique;
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

    public boolean isNullable() {
        return nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isResultUnique() {
        return resultUnique;
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
        final OrderByExpression other = (OrderByExpression) obj;
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
