/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SimpleCaseExpression extends GeneralCaseExpression {

    private Expression caseOperand;

    public SimpleCaseExpression(Expression caseOperand, List<WhenClauseExpression> whenClauses, Expression defaultExpr) {
        super(whenClauses, defaultExpr);
        this.caseOperand = caseOperand;
    }

    @Override
    public SimpleCaseExpression copy(ExpressionCopyContext copyContext) {
        int size = whenClauses.size();
        List<WhenClauseExpression> newWhenClauses = new ArrayList<WhenClauseExpression>(size);

        for (int i = 0; i < size; i++) {
            newWhenClauses.add(whenClauses.get(i).copy(copyContext));
        }

        if (defaultExpr == null) {
            return new SimpleCaseExpression(caseOperand.copy(copyContext), newWhenClauses, null);
        } else {
            return new SimpleCaseExpression(caseOperand.copy(copyContext), newWhenClauses, defaultExpr.copy(copyContext));
        }
    }

    public Expression getCaseOperand() {
        return caseOperand;
    }

    public void setCaseOperand(Expression caseOperand) {
        this.caseOperand = caseOperand;
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
        int hash = super.hashCode();
        hash = 89 * hash + (this.caseOperand != null ? this.caseOperand.hashCode() : 0);
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
        final SimpleCaseExpression other = (SimpleCaseExpression) obj;
        if (!super.equals(other)) {
            return false;
        } else if (this.caseOperand != other.caseOperand && (this.caseOperand == null || !this.caseOperand.equals(other.caseOperand))) {
            return false;
        }
        return true;
    }
}
