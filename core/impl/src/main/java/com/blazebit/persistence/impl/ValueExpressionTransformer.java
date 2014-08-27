/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ValueExpressionTransformer implements ExpressionTransformer {

    private final JPAInfo jpaInfo;
    private final Set<PathExpression> transformedExpressions = Collections.newSetFromMap(new IdentityHashMap<PathExpression, Boolean>());

    public ValueExpressionTransformer(JPAInfo jpaInfo) {
        this.jpaInfo = jpaInfo;
    }

    @Override
    public Expression transform(Expression original, boolean selectClause) {
        return transform(original);
    }

    @Override
    public Expression transform(Expression original) {
        if (original instanceof CompositeExpression) {
            CompositeExpression composite = (CompositeExpression) original;
            CompositeExpression transformed = new CompositeExpression(new ArrayList<Expression>());
            for (Expression e : composite.getExpressions()) {
                transformed.getExpressions().add(transform(e));
            }
            return transformed;
        }

        if (!(original instanceof PathExpression)) {
            return original;
        }

        PathExpression deepPath = (PathExpression) original; // deepPath != path in case path is an alias
        PathExpression path = deepPath;

        if (path.isCollectionKeyPath() || path.isUsedInCollectionFunction()) {
            return path;
        }

        if (path.getBaseNode() == null) {
            /**
             * Path might be a select alias. However, the alias points to the
             * select expression which will be transformed separately so we
             * don't have to do anything here.
             */
            return original;
        }
        String collectionValueFunction;
        JoinNode baseNode = (JoinNode) deepPath.getBaseNode();
        if (baseNode.getParentTreeNode() != null && baseNode.getParentTreeNode().isCollection() && deepPath.getField() == null && (collectionValueFunction = jpaInfo.getCollectionValueFunction()) != null) {
            if (!transformedExpressions.contains(path)) {
                List<Expression> transformedExpr = new ArrayList<Expression>();
                transformedExpr.add(new FooExpression(collectionValueFunction + "("));
                transformedExpr.add(path);
                transformedExpr.add(new FooExpression(")"));
                transformedExpressions.add(path);
                original = new CompositeExpression(transformedExpr);
            }
        }
        return original;
    }
}
