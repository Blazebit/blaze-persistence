/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.LazyCopyingResultVisitorAdapter;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SplittingVisitor extends LazyCopyingResultVisitorAdapter {

    private final EntityMetamodel metamodel;
    private PathExpression expressionToSplit;
    private String subAttribute;

    public SplittingVisitor(EntityMetamodel metamodel) {
        this.metamodel = metamodel;
    }

    @Override
    public Expression visit(PathExpression expression) {
        if (expression == expressionToSplit) {
            List<PathElementExpression> expressions = new ArrayList<>(expression.getExpressions());
            for (String subAttributePart : subAttribute.split("\\.")) {
                expressions.add(new PropertyExpression(subAttributePart));
            }

            String field = expression.getField() + "." + subAttribute;
            JoinNode node = (JoinNode) expression.getBaseNode();
            Class<?> fieldClass = JpaMetamodelUtils.getAttributePath(metamodel, node.getManagedType(), field).getAttributeClass();
            Type<?> fieldType = metamodel.type(fieldClass);

            return new PathExpression(
                    expressions,
                    new SimplePathReference(node, field, fieldType),
                    expression.isUsedInCollectionFunction(),
                    expression.isCollectionKeyPath()
            );
        }

        return expression;
    }

    public Expression splitOff(Expression expression, PathExpression expressionToSplit, String subAttribute) {
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
