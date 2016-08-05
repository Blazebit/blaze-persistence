package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.AbstractExpression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 05.08.2016.
 */
public abstract class AbstractPredicate extends AbstractExpression implements Predicate {

    protected boolean negated;

    public AbstractPredicate(boolean negated) {
        this.negated = negated;
    }

    @Override
    public boolean isNegated() {
        return negated;
    }

    @Override
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    @Override
    public void negate() {
        this.negated = !this.negated;
    }

    @Override
    public abstract Predicate clone();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPredicate)) return false;

        AbstractPredicate that = (AbstractPredicate) o;

        return negated == that.negated;

    }

    @Override
    public int hashCode() {
        return (negated ? 1 : 0);
    }
}
