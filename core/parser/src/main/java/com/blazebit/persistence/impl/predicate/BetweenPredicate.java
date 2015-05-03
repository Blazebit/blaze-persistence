/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.SimpleQueryGenerator;
import com.blazebit.persistence.impl.expression.AbstractExpression;
import com.blazebit.persistence.impl.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class BetweenPredicate extends AbstractExpression implements Predicate, Negatable {
    private Expression left;
    private Expression start;
    private Expression end;
    private boolean negated;

    public BetweenPredicate(Expression left, Expression start, Expression end) {
        this(left, start, end, false);
    }

    public BetweenPredicate(Expression left, Expression start, Expression end, boolean negated) {
        this.negated = negated;
        this.left = left;
        this.start = start;
        this.end = end;
    }

    @Override
    public BetweenPredicate clone() {
        return new BetweenPredicate(left.clone(), start.clone(), end.clone(), negated);
    }

    @Override
    public boolean isNegated() {
        return negated;
    }

    @Override
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getStart() {
        return start;
    }

    public Expression getEnd() {
        return end;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public void setStart(Expression start) {
        this.start = start;
    }

    public void setEnd(Expression end) {
        this.end = end;
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
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.left != null ? this.left.hashCode() : 0);
        hash = 61 * hash + (this.start != null ? this.start.hashCode() : 0);
        hash = 61 * hash + (this.end != null ? this.end.hashCode() : 0);
        hash = 61 * hash + (this.negated ? 1 : 0);
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
        final BetweenPredicate other = (BetweenPredicate) obj;
        if (this.left != other.left && (this.left == null || !this.left.equals(other.left))) {
            return false;
        }
        if (this.start != other.start && (this.start == null || !this.start.equals(other.start))) {
            return false;
        }
        if (this.end != other.end && (this.end == null || !this.end.equals(other.end))) {
            return false;
        }
        if (this.negated != other.negated) {
            return false;
        }
        return true;
    }
    
}
