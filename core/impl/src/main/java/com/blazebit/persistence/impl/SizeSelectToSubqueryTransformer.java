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

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.Subquery;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

/**
 *
 * @author Moritz Becker
 */
public class SizeSelectToSubqueryTransformer implements SelectInfoTransformer {

    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final AliasManager aliasManager;
    private final DeepSizeSelectToSubqueryTransformer deepTransformer = new DeepSizeSelectToSubqueryTransformer();

    public SizeSelectToSubqueryTransformer(SubqueryInitiatorFactory subqueryInitFactory, AliasManager aliasManager) {
        this.subqueryInitFactory = subqueryInitFactory;
        this.aliasManager = aliasManager;
    }

    @Override
    public void transform(SelectInfo info) {
        if (ExpressionUtils.isSizeFunction(info.getExpression())) {
            info.setExpression(info.getExpression().accept(deepTransformer));
        } else {
            info.getExpression().accept(deepTransformer);
        }
    }

    private class DeepSizeSelectToSubqueryTransformer extends SizeTransformationVisitor {

        @Override
        public Expression visit(PathExpression expression) {
            // performance short-cut
            return expression;
        }

        @Override
        public Expression visit(FunctionExpression expression) {
            if (ExpressionUtils.isSizeFunction(expression)) {
                PathExpression sizeArg = (PathExpression) expression.getExpressions().get(0);
                Class<?> collectionPropertyClass = ((JoinNode) sizeArg.getBaseNode()).getPropertyClass();
                String baseAlias = ((JoinNode) sizeArg.getBaseNode()).getAliasInfo().getAlias();
                String collectionPropertyName = sizeArg.getField() != null ? sizeArg.getField() : baseAlias;
                String collectionPropertyAlias = collectionPropertyName;
                String collectionPropertyClassName = collectionPropertyClass.getSimpleName().toLowerCase();
                String collectionPropertyClassAlias = collectionPropertyClassName;

                if (aliasManager.getAliasInfo(collectionPropertyClassName) != null) {
                    collectionPropertyClassAlias = aliasManager.generatePostfixedAlias(collectionPropertyClassName);
                }
                if (aliasManager.getAliasInfo(collectionPropertyName) != null) {
                    collectionPropertyAlias = aliasManager.generatePostfixedAlias(collectionPropertyName);
                }
                
                Subquery countSubquery = (Subquery) subqueryInitFactory.createSubqueryInitiator(null, new SubqueryBuilderListenerImpl<Object>())
                    .from(collectionPropertyClass, collectionPropertyClassAlias)
                    .select(new StringBuilder("COUNT(").append(collectionPropertyAlias).append(")").toString())
                    .leftJoin(new StringBuilder(collectionPropertyClassAlias).append('.').append(collectionPropertyName).toString(), collectionPropertyAlias)
                    .where(collectionPropertyClassAlias)
                    .eqExpression(baseAlias);

                return new SubqueryExpression(countSubquery);
            }
            return expression;
        }
    }
}
