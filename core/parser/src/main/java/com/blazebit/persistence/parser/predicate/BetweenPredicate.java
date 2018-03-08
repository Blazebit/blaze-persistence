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
public class BetweenPredicate extends AbstractPredicate {

    private Expression left;
    private Expression start;
    private Expression end;

    public BetweenPredicate(Expression left, Expression start, Expression end) {
        this(left, start, end, false);
    }

    public BetweenPredicate(Expression left, Expression start, Expression end, boolean negated) {
        super(negated);
        this.left = left;
        this.start = start;
        this.end = end;
    }

    @Override
    public BetweenPredicate clone(boolean resolved) {
        return new BetweenPredicate(left.clone(resolved), start.clone(resolved), end.clone(resolved), negated);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BetweenPredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BetweenPredicate that = (BetweenPredicate) o;

        if (left != null ? !left.equals(that.left) : that.left != null) {
            return false;
        }
        if (start != null ? !start.equals(that.start) : that.start != null) {
            return false;
        }
        return end != null ? end.equals(that.end) : that.end == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }
}
