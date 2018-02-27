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

import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0.0
 */
public class IllegalSubqueryDetector extends VisitorAdapter {

    private final AliasManager aliasManager;
    private boolean inSubquery = false;

    public IllegalSubqueryDetector(AliasManager aliasManager) {
        this.aliasManager = aliasManager;
    }

    @Override
    public void visit(PathExpression expression) {
        AliasInfo aliasInfo = aliasManager.getAliasInfo(expression.toString());

        if (aliasInfo != null && aliasInfo instanceof SelectInfo) {
            ((SelectInfo) aliasInfo).getExpression().accept(this);
        } else if (inSubquery) {
            JoinNode joinNode = (JoinNode) expression.getBaseNode();

            if (joinNode != null && joinNode.getAliasInfo().getAliasOwner() == aliasManager) {
                // we have an external path in the subquery
                while (joinNode != null && joinNode.getParentTreeNode() != null) {
                    if (joinNode.getParentTreeNode().isCollection()) {
                        throw new IllegalStateException("Unsupported external collection access [" + joinNode.getAliasInfo().getAbsolutePath()
                            + "]");
                    }

                    joinNode = joinNode.getParent();
                }
            }
        }
    }

    @Override
    public void visit(SubqueryExpression expression) {
        boolean inSubqueryCpy = inSubquery;
        inSubquery = true;
        // TODO: this is ugly
        AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder = (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery();
        builder.applyVisitor(this);

        if (!inSubqueryCpy) {
            inSubquery = false;
        }
    }
}
