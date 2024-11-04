/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.JoinManager;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.parser.util.ExpressionUtils;

import java.util.HashSet;

/**
 * This Transformer runs through the expressions of the query
 * For each OUTER(pp) expression it performs an implicitJoin for the join manager
 * of the surrounding query and replaces the OUTER(pp) expression with the base node alias '.' the field.
 *
 * We need a join manager hierarchy to do this.
 * We have decided to limit the outer statement to the join manager of the directly surrounding query so that the
 * user can specify the absolute path in a normalized form.
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class OuterFunctionVisitor extends ClauseAndJoinAwareVisitor implements ExpressionModifierVisitor<ExpressionModifier> {

    private final JoinManager joinManager;

    public OuterFunctionVisitor(JoinManager joinManager) {
        this.joinManager = joinManager;
    }

    @Override
    public void visit(ExpressionModifier expressionModifier, ClauseType clauseType) {
        visit(clauseType, expressionModifier.get());
    }

    public void visit(FunctionExpression expression) {
        if (ExpressionUtils.isOuterFunction(expression)) {
            PathExpression path = (PathExpression) expression.getExpressions().get(0);

            if (joinManager.getParent() != null) {
                joinManager.getParent().implicitJoin(path, true, true, true, null, fromClause, null, null, new HashSet<String>(), false, true, joinRequired, false, false, false);
            }
        }

        super.visit(expression);
    }

}
