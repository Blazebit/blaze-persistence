/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.InplaceModificationResultVisitorAdapter;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.util.ExpressionUtils;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class AliasReplacementVisitor extends InplaceModificationResultVisitorAdapter {

    private final Expression substitute;
    private final String alias;

    public AliasReplacementVisitor(Expression substitute, String alias) {
        this.substitute = substitute;
        this.alias = alias;
    }

    @Override
    public Expression visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();

        if (size == 1) {
            PathElementExpression elementExpression = expressions.get(0);
            if (elementExpression instanceof PropertyExpression) {
                if (alias.equals(((PropertyExpression) elementExpression).getProperty())) {
                    Expression newExpression = substitute.copy(ExpressionCopyContext.EMPTY);
                    if (newExpression instanceof PathExpression) {
                        PathExpression newPathExpression = (PathExpression) newExpression;
                        newPathExpression.setUsedInCollectionFunction(expression.isUsedInCollectionFunction());
                        newPathExpression.setCollectionQualifiedPath(expression.isCollectionQualifiedPath());
                    }
                    return newExpression;
                }
            }
        } else {
            PathExpression leftMost = ExpressionUtils.getLeftMostPathExpression(expression);
            if (leftMost.getExpressions().get(0) instanceof PropertyExpression && alias.equals(((PropertyExpression) leftMost.getExpressions().get(0)).getProperty())) {
                if (substitute instanceof PathExpression) {
                    leftMost.getExpressions().remove(0);
                    leftMost.getExpressions().addAll(0, ((PathExpression) substitute).getExpressions());
                } else {
                    leftMost.getExpressions().set(0, (PathElementExpression) substitute);
                }
            }
        }
        return expression;
    }

}
