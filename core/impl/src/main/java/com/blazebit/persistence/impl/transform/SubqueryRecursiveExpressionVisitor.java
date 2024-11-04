/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubqueryRecursiveExpressionVisitor extends VisitorAdapter implements ExpressionModifierVisitor<ExpressionModifier> {

    @Override
    public void visit(ExpressionModifier expressionModifier, ClauseType clauseType) {
        expressionModifier.get().accept(this);
    }

    @Override
    public void visit(SubqueryExpression expression) {
        // TODO: this is ugly
        // IMO this should be part of JoinVisitor
//        if (expression.getSubquery() instanceof AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) {
//            ((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery()).applyExpressionTransformersAndBuildGroupByClauses(null);
//        }
    }

}
