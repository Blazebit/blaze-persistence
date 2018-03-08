/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ArithmeticExpression extends AbstractNumericExpression {

    private Expression left;
    private Expression right;
    private final ArithmeticOperator op;

    public ArithmeticExpression(Expression left, Expression right, ArithmeticOperator op) {
        super(resolveType(left, right));
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public ArithmeticOperator getOp() {
        return op;
    }

    private static NumericType resolveType(Expression left, Expression right) {
        if (!(left instanceof NumericExpression && right instanceof NumericExpression)) {
            return null;
        }
        
        NumericExpression numericLeft = (NumericExpression) left;
        NumericExpression numericRight = (NumericExpression) right;

        if (numericLeft == null || numericLeft.getNumericType() == null) {
            return numericRight == null ? null : numericRight.getNumericType();
        } else if (numericRight == null || numericRight.getNumericType() == null) {
            return numericLeft.getNumericType();
        }

        return NumericType.values()[Math.max(numericLeft.getNumericType().ordinal(), numericRight.getNumericType().ordinal())];
    }

    @Override
    public Expression clone(boolean resolved) {
        return new ArithmeticExpression(left.clone(resolved), right.clone(resolved), op);
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
        if (!(o instanceof ArithmeticExpression)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ArithmeticExpression that = (ArithmeticExpression) o;

        if (left != null ? !left.equals(that.left) : that.left != null) {
            return false;
        }
        if (right != null ? !right.equals(that.right) : that.right != null) {
            return false;
        }
        return op == that.op;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        result = 31 * result + (op != null ? op.hashCode() : 0);
        return result;
    }
}
