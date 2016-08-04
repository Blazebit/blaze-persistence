package com.blazebit.persistence.impl.expression;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 04.08.2016.
 */
public class EntityLiteral extends AbstractExpression {

    private final Class<?> value;
    private final String originalExpression;

    public EntityLiteral(Class<?> value, String originalExpression) {
        this.value = value;
        this.originalExpression = originalExpression;
    }

    public Class<?> getValue() {
        return value;
    }

    public String getOriginalExpression() {
        return originalExpression;
    }

    @Override
    public Expression clone() {
        return new EntityLiteral(value, originalExpression);
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
        if (!(o instanceof EntityLiteral)) return false;

        EntityLiteral that = (EntityLiteral) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
