/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class OnClauseJoinNodeVisitor implements JoinNodeVisitor {

    private final Expression.Visitor visitor;

    public OnClauseJoinNodeVisitor(Expression.Visitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visit(JoinNode node) {
        if (node.getOnPredicate() != null) {
            node.getOnPredicate().accept(visitor);
        }
    }

}
