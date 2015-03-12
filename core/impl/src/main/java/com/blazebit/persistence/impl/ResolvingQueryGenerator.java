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

import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ResolvingQueryGenerator extends SimpleQueryGenerator {

    private boolean resolveSelectAliases = true;
    private final AliasManager aliasManager;
    private final JPAInfo jpaInfo;
    protected String aliasPrefix;
    private final Set<String> registeredFunctions;
    
    public ResolvingQueryGenerator(AliasManager aliasManager, JPAInfo jpaInfo, Set<String> registeredFunctions) {
        this.aliasManager = aliasManager;
        this.jpaInfo = jpaInfo;
        this.registeredFunctions = registeredFunctions;
    }

    @Override
    public void visit(FunctionExpression expression) {
        if (ExpressionUtils.isOuterFunction(expression)) {
            expression.getExpressions().get(0).accept(this);
        } else if (ExpressionUtils.isFunctionFunctionExpression(expression)) {
            String functionName = ExpressionUtils.unwrapStringLiteral(expression.getExpressions().get(0).toString());
            if(registeredFunctions.contains(functionName.toLowerCase())) {
                if (jpaInfo.isHibernate) {
                    // resolve function
                    sb.append(functionName);
                    sb.append('(');
                    if (expression.getExpressions().size() > 1) {
                        expression.getExpressions().get(1).accept(this);
                        for (int i = 2; i < expression.getExpressions().size(); i++) {
                            sb.append(",");
                            expression.getExpressions().get(i).accept(this);
                        }
                    }
                    sb.append(')');
                } else {
                    // EclipseLink operator style
                    sb.append("OPERATOR('");
                    sb.append(functionName);
                    sb.append('\'');

                    for (int i = 1; i < expression.getExpressions().size(); i++) {
                        sb.append(',');
                        expression.getExpressions().get(i).accept(this);
                    }

                    sb.append(')');
                }
            } else if (jpaInfo.isJPA21) {
                // Add the JPA 2.1 Function style function
                sb.append("FUNCTION('");
                sb.append(functionName);
                sb.append('\'');
                
                for (int i = 1; i < expression.getExpressions().size(); i++) {
                    sb.append(',');
                    expression.getExpressions().get(i).accept(this);
                }
                
                sb.append(')');
            } else {
                throw new IllegalArgumentException("Unknown function [" + functionName + "] is used!");
            }
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
            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }
            
            JoinNode baseNode = (JoinNode) expression.getBaseNode();
            sb.append(baseNode.getAliasInfo().getAlias());
        } else {
            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }
            
            JoinNode baseNode = (JoinNode) expression.getBaseNode();
            sb.append(baseNode.getAliasInfo().getAlias())
                    .append(".")
                    .append(expression.getField());
        }
    }
    
    @Override
    public void visit(ParameterExpression expression) {
        String paramName;
        if (expression.getName() == null) {
            throw new IllegalStateException("Unsatisfied parameter " + expression.getName());
        } else {
            paramName = expression.getName();
        }
        // Workaround for hibernate
        // TODO: Remove when HHH-7407 is fixed
        boolean needsBrackets = jpaInfo.isHibernate && expression.getValue() instanceof List<?> && ((List<?>) expression.getValue()).size() > 1;
        
        if (needsBrackets) {
            sb.append('(');
        }
        
        sb.append(":");
        sb.append(paramName);
        
        if (needsBrackets) {
            sb.append(')');
        }
    }

    public boolean isResolveSelectAliases() {
        return resolveSelectAliases;
    }

    public void setResolveSelectAliases(boolean replaceSelectAliases) {
        this.resolveSelectAliases = replaceSelectAliases;
    }

    public String getAliasPrefix() {
        return aliasPrefix;
    }

    public void setAliasPrefix(String aliasPrefix) {
        this.aliasPrefix = aliasPrefix;
    }

    @Override
    public void visit(ArrayExpression expression) {
    }
}
