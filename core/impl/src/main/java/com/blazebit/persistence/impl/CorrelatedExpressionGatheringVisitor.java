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

        if (aliasManager != ((JoinNode) baseNode).getAliasInfo().getAliasOwner()) {
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
        expressions.addAll(builder.getCorrelatedExpressions());
    }
}