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

import com.blazebit.persistence.impl.predicate.VisitorAdapter;
import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

/**
 *
 * @author ccbem
 */
public class IllegalSubqueryDetector extends VisitorAdapter {

    private final AliasManager aliasManager;
    private boolean inSubquery = false;
    private final BaseQueryBuilder<?, ?> aliasOwner;

    public IllegalSubqueryDetector(AliasManager aliasManager, BaseQueryBuilder<?, ?> aliasOwner) {
        this.aliasManager = aliasManager;
        this.aliasOwner = aliasOwner;
    }

    @Override
    public void visit(PathExpression expression) {
        AliasInfo aliasInfo = aliasManager.getAliasInfo(expression.toString());
        if (aliasInfo != null && aliasInfo instanceof SelectManager.SelectInfo) {
            ((SelectManager.SelectInfo) aliasInfo).getExpression().accept(this);
        } else {
            // check if this path is invalid
            if (inSubquery) {
                JoinNode joinNode = (JoinNode) expression.getBaseNode();
                if (joinNode != null) {
                    if (joinNode.getAliasInfo().getAliasOwner() == aliasOwner) {
                        int pathElemStart = 0;
                        if(joinNode.getAliasInfo().getAbsolutePath().isEmpty()){
                            // skip root node in for loop
                            pathElemStart = 1;
                        }
                        // we have an external path in the subquery
                        if (joinNode.isCollection()) {
                            throw new IllegalStateException("Unsupported external collection access [" + joinNode.getAliasInfo().getAbsolutePath() + "]");
                        }
                        for (int i = pathElemStart; i < expression.getExpressions().size(); i++) {
                            PathElementExpression pathElem = expression.getExpressions().get(i);
                            if (pathElem instanceof ArrayExpression) {
                                joinNode = joinNode.getNodes().get(((ArrayExpression) pathElem).getBase().toString());
                            } else {
                                joinNode = joinNode.getNodes().get(pathElem.toString());
                            }
                            if (joinNode != null && joinNode.isCollection()) {
                                throw new IllegalStateException("Unsupported external collection access [" + joinNode.getAliasInfo().getAbsolutePath() + "]");
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visit(SubqueryExpression expression) {
        boolean inSubqueryCpy = inSubquery;
        inSubquery = true;
        SubqueryBuilderImpl<?> builder = (SubqueryBuilderImpl<?>) expression.getBuilder();
        builder.applyVisitor(this);
        if (!inSubqueryCpy) {
            inSubquery = false;
        }
    }
}
