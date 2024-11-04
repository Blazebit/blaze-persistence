/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class WhenClauseExpression extends AbstractExpression {

    private Expression condition;
    private Expression result;

    public WhenClauseExpression(Expression condition, Expression result) {
        this.condition = condition;
        this.result = result;
    }

    @Override
    public WhenClauseExpression copy(ExpressionCopyContext copyContext) {
        return new WhenClauseExpression(condition.copy(copyContext), result.copy(copyContext));
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public Expression getResult() {
        return result;
    }

    public void setResult(Expression result) {
        this.result = result;
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
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.condition != null ? this.condition.hashCode() : 0);
        hash = 61 * hash + (this.result != null ? this.result.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WhenClauseExpression other = (WhenClauseExpression) obj;
        if (this.condition != other.condition && (this.condition == null || !this.condition.equals(other.condition))) {
            return false;
        }
        if (this.result != other.result && (this.result == null || !this.result.equals(other.result))) {
            return false;
        }
        return true;
    }
}
