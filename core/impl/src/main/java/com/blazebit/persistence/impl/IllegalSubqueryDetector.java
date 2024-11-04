/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
