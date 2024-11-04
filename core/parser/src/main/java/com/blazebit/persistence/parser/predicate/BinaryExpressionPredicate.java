/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.predicate;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class BinaryExpressionPredicate extends AbstractPredicate {

    protected Expression left;
    protected Expression right;

    public BinaryExpressionPredicate(Expression left, Expression right) {
        this(left, right, false);
    }

    public BinaryExpressionPredicate(Expression left, Expression right, boolean negated) {
        super(negated);
        this.left = left;
        this.right = right;
    }

    @Override
    public abstract BinaryExpressionPredicate copy(ExpressionCopyContext copyContext);

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BinaryExpressionPredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BinaryExpressionPredicate that = (BinaryExpressionPredicate) o;

        if (left != null ? !left.equals(that.left) : that.left != null) {
            return false;
        }
        return right != null ? right.equals(that.right) : that.right == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
