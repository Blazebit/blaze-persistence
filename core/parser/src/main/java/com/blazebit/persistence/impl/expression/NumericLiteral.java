package com.blazebit.persistence.impl.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public class NumericLiteral extends AbstractNumericExpression {

    private final String value;

    public NumericLiteral(String value, NumericType numericType) {
        super(numericType);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Expression clone() {
        return new NumericLiteral(value, getNumericType());
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
        if (!(o instanceof NumericLiteral)) return false;
        if (!super.equals(o)) return false;

        NumericLiteral that = (NumericLiteral) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}

