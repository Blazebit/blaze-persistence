/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;

/**
 * Finds a PathExpression, and while traversing back along the stack, capture if we are in a disjunction.
 * This is necessary for the EXISTS subquery rewrite for associations used in the ON clause.
 * We must know, if the association represented by the path expression, is used in a context,
 * where the cardinality 0 might be problematic. In a disjunctive or null aware context, cardinality is important.
 * We say, it is "inConjunction" if the path expression is used in no disjunctive or null aware context.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ConjunctionPathExpressionFindingVisitor extends AbortableVisitorAdapter {

    private PathExpression pathExpression;
    private boolean disjunction;

    public boolean isInConjunction(CompoundPredicate predicate, PathExpression pathExpression) {
        this.pathExpression = pathExpression;
        this.disjunction = false;
        try {
            predicate.accept(this);
            return !disjunction;
        } finally {
            this.pathExpression = null;
        }
    }

    @Override
    public Boolean visit(PathExpression expression) {
        if (pathExpression == expression) {
            return true;
        }
        return super.visit(expression);
    }

    @Override
    public Boolean visit(IsNullPredicate predicate) {
        if (super.visit(predicate)) {
            // When the path is used in a NULL aware context, we say it is in a disjunctive context
            disjunction = true;
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        if (super.visit(expression)) {
            if ("COALESCE".equalsIgnoreCase(expression.getFunctionName()) || "NULLIF".equalsIgnoreCase(expression.getFunctionName())) {
                // When the path is used in a NULL aware context, we say it is in a disjunctive context
                disjunction = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean visit(CompoundPredicate predicate) {
        if (super.visit(predicate)) {
            disjunction = disjunction || predicate.getOperator() == CompoundPredicate.BooleanOperator.OR ^ predicate.isNegated();
            return true;
        }
        return false;
    }
}
