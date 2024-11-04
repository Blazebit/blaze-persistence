/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class AbstractNumericExpression extends AbstractExpression implements NumericExpression {

    private final NumericType numericType;

    public AbstractNumericExpression(NumericType numericType) {
        this.numericType = numericType;
    }

    @Override
    public NumericType getNumericType() {
        return numericType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractNumericExpression)) {
            return false;
        }

        AbstractNumericExpression that = (AbstractNumericExpression) o;

        return numericType == that.numericType;

    }

    @Override
    public int hashCode() {
        return numericType != null ? numericType.hashCode() : 0;
    }

    @Override
    public abstract Expression copy(ExpressionCopyContext copyContext);
}
