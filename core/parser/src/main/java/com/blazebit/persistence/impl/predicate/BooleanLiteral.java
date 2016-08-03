package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.AbstractExpression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 03.08.2016.
 */
public class BooleanLiteral extends AbstractExpression implements Predicate {

    private final boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Predicate clone() {
        return new BooleanLiteral(value);
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
        if (this == o) return true;
        if (!(o instanceof BooleanLiteral)) return false;

        BooleanLiteral that = (BooleanLiteral) o;

        return value == that.value;

    }

    @Override
    public int hashCode() {
        return Boolean.valueOf(value).hashCode();
    }
}
