package com.blazebit.persistence.impl.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public class ArithmeticExpression extends AbstractNumericExpression {

    private Expression left;
    private Expression right;
    private final ArithmeticOperator op;

    public ArithmeticExpression(Expression left, Expression right, ArithmeticOperator op) {
        super(resolveType(left, right));
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public ArithmeticOperator getOp() {
        return op;
    }

    private static NumericType resolveType(Expression left, Expression right) {
        if (!(left instanceof NumericExpression && right instanceof NumericExpression)) {
            return null;
        }
        
        NumericExpression numericLeft = (NumericExpression) left;
        NumericExpression numericRight = (NumericExpression) right;

        if (numericLeft == null || numericLeft.getNumericType() == null) {
            return numericRight == null ? null : numericRight.getNumericType();
        } else if (numericRight == null || numericRight.getNumericType() == null) {
            return numericLeft.getNumericType();
        }

        return NumericType.values()[Math.max(numericLeft.getNumericType().ordinal(), numericRight.getNumericType().ordinal())];
    }

    @Override
    public Expression clone() {
        return new ArithmeticExpression(left.clone(), right.clone(), op);
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
        if (!(o instanceof ArithmeticExpression)) return false;
        if (!super.equals(o)) return false;

        ArithmeticExpression that = (ArithmeticExpression) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;
        return op == that.op;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        result = 31 * result + (op != null ? op.hashCode() : 0);
        return result;
    }
}
