package com.blazebit.persistence.impl.expression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 03.08.2016.
 */
public class BooleanLiteral extends AbstractExpression implements BooleanExpression {

    private final String value;

    public BooleanLiteral(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public BooleanExpression clone() {
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

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
