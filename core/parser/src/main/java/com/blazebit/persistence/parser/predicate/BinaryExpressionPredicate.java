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

package com.blazebit.persistence.parser.predicate;

import com.blazebit.persistence.parser.expression.Expression;

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
    public abstract BinaryExpressionPredicate clone(boolean resolved);

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
