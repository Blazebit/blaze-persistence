package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.AbstractExpression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 03.08.2016.
 */
public class BooleanLiteral extends AbstractPredicate {

    private final boolean value;

    public BooleanLiteral(boolean value) {
        super(false);
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
        if (!super.equals(o)) return false;

        BooleanLiteral that = (BooleanLiteral) o;

        return value == that.value;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value ? 1 : 0);
        return result;
    }
}
