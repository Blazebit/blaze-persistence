package com.blazebit.persistence.impl.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public class EnumLiteral extends AbstractExpression {

    private final Enum<?> value;
    private final String originalExpression;

    public EnumLiteral(Enum<?> value, String originalExpression) {
        this.value = value;
        this.originalExpression = originalExpression;
    }

    public Enum<?> getValue() {
        return value;
    }

    public String getOriginalExpression() {
        return originalExpression;
    }

    @Override
    public Expression clone() {
        return new EnumLiteral(value, originalExpression);
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
        if (!(o instanceof EnumLiteral)) return false;

        EnumLiteral that = (EnumLiteral) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
