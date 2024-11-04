/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
class ImplicitGroupByClauseDependencyRegistrationVisitor extends VisitorAdapter {

    private final AliasManager aliasManager;

    public ImplicitGroupByClauseDependencyRegistrationVisitor(AliasManager aliasManager) {
        this.aliasManager = aliasManager;
    }

    @Override
    public void visit(PathExpression expr) {
        JoinNode node = (JoinNode) expr.getBaseNode();
        if (node == null) {
            // This can only be a select alias
            ((SelectInfo) aliasManager.getAliasInfo(expr.toString())).getExpression().accept(this);
        } else {
            node.updateClauseDependencies(ClauseType.GROUP_BY, null);
        }
    }

}