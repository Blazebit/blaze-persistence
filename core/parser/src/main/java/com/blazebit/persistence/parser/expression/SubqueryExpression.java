/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 * SubqueryExpressions can never be returned by the parser and are therefore never cached. Thus, the clone method
 * does not need to do deep cloning.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryExpression extends AbstractExpression {

    private final Subquery subquery;

    public SubqueryExpression(Subquery builder) {
        this.subquery = builder;
    }

    @Override
    public SubqueryExpression copy(ExpressionCopyContext copyContext) {
        return new SubqueryExpression(subquery);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Subquery getSubquery() {
        return subquery;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.subquery != null ? this.subquery.hashCode() : 0);
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
        final SubqueryExpression other = (SubqueryExpression) obj;
        if (this.subquery != other.subquery && (this.subquery == null || !this.subquery.equals(other.subquery))) {
            return false;
        }
        return true;
    }
}
