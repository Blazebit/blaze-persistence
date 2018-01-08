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

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.PathExpression;

import javax.persistence.metamodel.Attribute;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class JpaUtils {

    private static final Logger LOG = Logger.getLogger(JpaUtils.class.getName());

    private JpaUtils() {
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, PathExpression expression) {
        JoinNode expressionBaseNode = ((JoinNode) expression.getPathReference().getBaseNode());
        String firstElementString = expression.getExpressions().get(0).toString();
        String baseNodeAlias;
        JoinNode baseNode = expressionBaseNode;

        do {
            baseNodeAlias = baseNode.getAlias();
        } while (!firstElementString.equals(baseNodeAlias) && (baseNode = baseNode.getParent()) != null);

        if (baseNode == null) {
            baseNodeAlias = null;
            if (expressionBaseNode.getParent() == null) {
                baseNode = expressionBaseNode;
            } else {
                baseNode = expressionBaseNode.getParent();
            }
        }

        return getAttributeForJoining(metamodel, baseNode.getType(), expression, baseNodeAlias);
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, Class<?> type, Expression joinExpression, String baseNodeAlias) {
        PathTargetResolvingExpressionVisitor visitor = new PathTargetResolvingExpressionVisitor(metamodel, type, baseNodeAlias);
        joinExpression.accept(visitor);

        if (visitor.getPossibleTargets().size() > 1) {
            throw new IllegalArgumentException("Multiple possible target types for expression: " + joinExpression);
        }

        Map.Entry<Attribute<?, ?>, Class<?>> entry = visitor.getPossibleTargets().entrySet().iterator().next();
        return new AttributeHolder(entry.getKey(), entry.getValue());
    }
}
