package com.blazebit.persistence.impl.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public class ArithmeticFactor extends AbstractNumericExpression {

    private Expression expression;
    private boolean invertSignum;

    public ArithmeticFactor(Expression expression, boolean invertSignum) {
        super(expression instanceof NumericExpression ? ((NumericExpression) expression).getNumericType() : null);
        this.expression = expression;
        this.invertSignum = invertSignum;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public boolean isInvertSignum() {
        return invertSignum;
    }

    public void setInvertSignum(boolean invertSignum) {
        this.invertSignum = invertSignum;
    }

    @Override
    public Expression clone() {
        return new ArithmeticFactor(expression, invertSignum);
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
        if (!(o instanceof ArithmeticFactor)) return false;
        if (!super.equals(o)) return false;

        ArithmeticFactor that = (ArithmeticFactor) o;

        if (invertSignum != that.invertSignum) return false;
        return expression != null ? expression.equals(that.expression) : that.expression == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (invertSignum ? 1 : 0);
        return result;
    }
}
