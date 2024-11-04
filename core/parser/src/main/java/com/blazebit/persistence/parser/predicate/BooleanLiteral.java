/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.predicate;

import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class BooleanLiteral extends AbstractPredicate {

    private final boolean value;

    public BooleanLiteral(boolean value) {
        super(false);
        this.value = value;
    }

    public BooleanLiteral(boolean value, boolean negated) {
        super(negated);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Predicate copy(ExpressionCopyContext copyContext) {
        return new BooleanLiteral(value, negated);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BooleanLiteral)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BooleanLiteral that = (BooleanLiteral) o;

        return value == that.value;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return Boolean.toString(value != negated);
    }
}
