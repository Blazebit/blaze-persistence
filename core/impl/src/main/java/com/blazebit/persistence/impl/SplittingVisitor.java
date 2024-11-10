/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.LazyCopyingResultVisitorAdapter;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.spi.JpaProvider;

import jakarta.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SplittingVisitor extends LazyCopyingResultVisitorAdapter {

    private final EntityMetamodel metamodel;
    private final JpaProvider jpaProvider;
    private final AliasManager aliasManager;
    private Expression expressionToSplit;
    private String subAttribute;

    public SplittingVisitor(EntityMetamodel metamodel, JpaProvider jpaProvider, AliasManager aliasManager) {
        this.metamodel = metamodel;
        this.jpaProvider = jpaProvider;
        this.aliasManager = aliasManager;
    }

    @Override
    public Expression visit(MapKeyExpression expression) {
        if (expression == expressionToSplit) {
            List<PathElementExpression> expressions = new ArrayList<>(2);
            expressions.add(expression);
            for (String subAttributePart : subAttribute.split("\\.")) {
                expressions.add(new PropertyExpression(subAttributePart));
            }

            JoinNode node = ((JoinNode) expression.getPath().getBaseNode()).getKeyJoinNode();
            String field = subAttribute;
            Class<?> fieldClass = jpaProvider.getJpaMetamodelAccessor().getAttributePath(metamodel, node.getManagedType(), field).getAttributeClass();
            Type<?> fieldType = metamodel.type(fieldClass);

            return new PathExpression(
                    expressions,
                    new SimplePathReference(node, field, fieldType),
                    false,
                    false
            );
        }
        return expression;
    }

    @Override
    public Expression visit(MapValueExpression expression) {
        if (expression == expressionToSplit) {
            Expression newExpression = expression.getPath().accept(this);
            if (newExpression != expression.getPath()) {
                // A split map value expression is not surrounded by the map value operator anymore
                return newExpression;
            }
        } else {
            return expression.getPath().accept(this);
        }

        return expression;
    }

    @Override
    public Expression visit(PathExpression expression) {
        if (expression.getBaseNode() == null) {
            Expression aliasedExpression = ((SelectInfo) aliasManager.getAliasInfo(expression.toString())).getExpression();
            Expression newExpression = aliasedExpression.accept(this);
            return aliasedExpression == newExpression ? expression : newExpression;
        }
        if (expression == expressionToSplit) {
            List<PathElementExpression> expressions = new ArrayList<>(expression.getExpressions());
            for (String subAttributePart : subAttribute.split("\\.")) {
                expressions.add(new PropertyExpression(subAttributePart));
            }

            String field;
            if (expression.getField() == null) {
                field = subAttribute;
            } else {
                field = expression.getField() + "." + subAttribute;
            }
            JoinNode node = (JoinNode) expression.getBaseNode();
            Class<?> fieldClass = jpaProvider.getJpaMetamodelAccessor().getAttributePath(metamodel, node.getManagedType(), field).getAttributeClass();
            Type<?> fieldType = metamodel.type(fieldClass);

            return new PathExpression(
                    expressions,
                    new SimplePathReference(node, field, fieldType),
                    expression.isUsedInCollectionFunction(),
                    expression.isCollectionQualifiedPath()
            );
        }

        return expression;
    }

    public Expression splitOff(Expression expression, Expression expressionToSplit, String subAttribute) {
        this.expressionToSplit = expressionToSplit;
        this.subAttribute = subAttribute;
        try {
            return expression.accept(this);
        } finally {
            this.expressionToSplit = null;
            this.subAttribute = null;
        }
    }
}
