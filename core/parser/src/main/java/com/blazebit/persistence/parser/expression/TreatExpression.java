/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TreatExpression extends AbstractExpression implements PathElementExpression {

    // Can be either a PathExpression or a KEY/VALUE expression
    private Expression expression;
    private final String type;

    public TreatExpression(Expression expression, String type) {
        this.expression = expression;
        this.type = type;
    }

    @Override
    public TreatExpression copy(ExpressionCopyContext copyContext) {
        return new TreatExpression(expression.copy(copyContext), type);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TreatExpression)) {
            return false;
        }

        TreatExpression that = (TreatExpression) o;

        if (expression != null ? !expression.equals(that.expression) : that.expression != null) {
            return false;
        }
        return type != null ? type.equals(that.type) : that.type == null;

    }

    @Override
    public int hashCode() {
        int result = expression != null ? expression.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
