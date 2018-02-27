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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 *
 */
public class InPredicate extends AbstractPredicate {

    private Expression left;
    private List<Expression> right;

    public InPredicate(boolean negated, Expression left, Expression... right) {
        this(negated, left, new ArrayList<Expression>(Arrays.asList(right)));
    }

    public InPredicate(Expression left, Expression... right) {
        this(false, left, new ArrayList<Expression>(Arrays.asList(right)));
    }

    public InPredicate(boolean negated, Expression left, List<Expression> right) {
        super(negated);
        this.left = left;
        this.right = right;
    }

    public InPredicate(Expression left, List<Expression> right) {
        this(false, left, right);
    }

    @Override
    public InPredicate clone(boolean resolved) {
        List<Expression> rightCloned = new ArrayList<Expression>(right.size());
        for (Expression expr : right) {
            rightCloned.add(expr.clone(resolved));
        }
        return new InPredicate(negated, left.clone(resolved), rightCloned);
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public List<Expression> getRight() {
        return right;
    }

    public void setRight(List<Expression> right) {
        this.right = right;
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
        if (!(o instanceof InPredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        InPredicate that = (InPredicate) o;
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
