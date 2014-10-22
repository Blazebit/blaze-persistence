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

import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.PathExpression;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ResolvingQueryGenerator extends SimpleQueryGenerator {

    private boolean resolveSelectAliases = true;
    private final AliasManager aliasManager;
    private final JPAInfo jpaInfo;

    public ResolvingQueryGenerator(AliasManager aliasManager, JPAInfo jpaInfo) {
        this.aliasManager = aliasManager;
        this.jpaInfo = jpaInfo;
    }

    @Override
    public void visit(FunctionExpression expression) {
        if (ExpressionUtils.isOuterFunction(expression)) {
            expression.getExpressions().get(0).accept(this);
        } else if (ExpressionUtils.isFunctionFunctionExpression(expression) && !jpaInfo.isJPA21) {
            // resolve function
            String functionName = expression.getExpressions().get(0).toString();
            functionName = functionName.substring(1, functionName.length() - 1); // remove quotes
            sb.append(functionName);
            sb.append('(');
            if (!expression.getExpressions().isEmpty()) {
                for (int i = 1; i < expression.getExpressions().size(); i++) {
                    sb.append(",");
                    expression.getExpressions().get(i).accept(this);
                }
            }
            sb.append(')');
        } else {
            super.visit(expression);
        }
    }

    @Override
    public void visit(PathExpression expression) {
        if (resolveSelectAliases) {
            // if path expression should not be replaced by select aliases we
            // check for select aliases that have to be replaced with the corresponding
            // path expressions
            if (expression.getBaseNode() == null) {
                AliasInfo aliasInfo;
                if ((aliasInfo = aliasManager.getAliasInfo(expression.toString())) != null) {
                    if (aliasInfo instanceof SelectInfo) {
                        SelectInfo selectAliasInfo = (SelectInfo) aliasInfo;
//                        if (!ExpressionUtils.containsSubqueryExpression(selectAliasInfo.getExpression())) {
                        if (((SelectInfo) aliasInfo).getExpression() instanceof PathExpression) {
                            selectAliasInfo.getExpression().accept(this);
                            return;
                        }
                    }
                }
            }
        }
        if (expression.getBaseNode() == null) {
            sb.append(expression.getPath());
        } else if (expression.getField() == null) {
            JoinNode baseNode = (JoinNode) expression.getBaseNode();
            sb.append(baseNode.getAliasInfo().getAlias());
        } else {
            JoinNode baseNode = (JoinNode) expression.getBaseNode();
            sb.append(baseNode.getAliasInfo().getAlias())
                    .append(".")
                    .append(expression.getField());
        }
    }

    public boolean isResolveSelectAliases() {
        return resolveSelectAliases;
    }

    public void setResolveSelectAliases(boolean replaceSelectAliases) {
        this.resolveSelectAliases = replaceSelectAliases;
    }

    @Override
    public void visit(ArrayExpression expression) {
    }
}
