/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.BaseNode;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects all expressions that are correlated.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
class CorrelatedExpressionGatheringVisitor extends VisitorAdapter {

    private final AliasManager aliasManager;
    private final Set<Expression> expressions = new LinkedHashSet<Expression>();

    public CorrelatedExpressionGatheringVisitor(AliasManager aliasManager) {
        this.aliasManager = aliasManager;
    }

    public Set<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public void visit(PathExpression expression) {
        BaseNode baseNode = expression.getBaseNode();
        if (!(baseNode instanceof JoinNode)) {
            throw new IllegalArgumentException("Unexpected base node type: " + baseNode);
        }

        if (aliasManager == ((JoinNode) baseNode).getAliasInfo().getAliasOwner()) {
            expressions.add(expression);
        }
    }

    @Override
    public void visit(TreatExpression expression) {
        Expression subExpression = expression.getExpression();
        boolean beforeContained = expressions.contains(subExpression);
        subExpression.accept(this);
        if (expressions.contains(subExpression)) {
            // If the expression only got added for this TREAT expression, we replace it with the treat expression
            if (!beforeContained) {
                expressions.remove(subExpression);
            }
            expressions.add(expression);
        }
    }

    @Override
    public void visit(SubqueryExpression expression) {
        if (!(expression.getSubquery() instanceof SubqueryInternalBuilder<?>)) {
            throw new IllegalArgumentException("Unexpected subquery subtype: " + expression.getSubquery());
        }
        SubqueryInternalBuilder<?> builder = (SubqueryInternalBuilder<?>) expression.getSubquery();
        expressions.addAll(builder.getCorrelatedExpressions(aliasManager));
    }
}