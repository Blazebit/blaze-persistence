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
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CompoundPredicate extends AbstractPredicate {

    private final List<Predicate> children;
    private final BooleanOperator operator;

    public CompoundPredicate(BooleanOperator operator) {
        this(operator, new ArrayList<Predicate>());
    }

    public CompoundPredicate(BooleanOperator operator, Predicate... children) {
        super(false);
        this.operator = operator;
        this.children = new ArrayList<Predicate>(Arrays.asList(children));
    }

    public CompoundPredicate(BooleanOperator operator, List<Predicate> children) {
        super(false);
        this.operator = operator;
        this.children = children;
    }

    public CompoundPredicate(BooleanOperator operator, List<Predicate> children, boolean negated) {
        super(negated);
        this.operator = operator;
        this.children = children;
    }

    public List<Predicate> getChildren() {
        return children;
    }

    public BooleanOperator getOperator() {
        return operator;
    }

    @Override
    public CompoundPredicate clone(boolean resolved) {
        List<Predicate> clonedChildren = new ArrayList<>(children.size());
        for (Predicate child : children) {
            clonedChildren.add(child.clone(resolved));
        }
        return new CompoundPredicate(operator, clonedChildren, negated);
    }

    @Override
    public void accept(Expression.Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(Expression.ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompoundPredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        CompoundPredicate that = (CompoundPredicate) o;

        if (children != null ? !children.equals(that.children) : that.children != null) {
            return false;
        }
        return operator == that.operator;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        return result;
    }

    /**
     * @author Moritz Becker
     * @since 1.2.0
     */
    public enum BooleanOperator {
        AND,
        OR;

        public BooleanOperator invert() {
            if (this == AND) {
                return OR;
            } else {
                return AND;
            }
        }
    }

}
