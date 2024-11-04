/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.5
 */
public class AbortableOnClauseJoinNodeVisitor implements AbortableResultJoinNodeVisitor<Boolean> {

    private final Expression.ResultVisitor<Boolean> visitor;
    private final Boolean stopValue;

    public AbortableOnClauseJoinNodeVisitor(Expression.ResultVisitor<Boolean> visitor, Boolean stopValue) {
        this.visitor = visitor;
        this.stopValue = stopValue;
    }

    @Override
    public Boolean visit(JoinNode node) {
        if (node.getOnPredicate() != null) {
            return node.getOnPredicate().accept(visitor);
        }

        return null;
    }

    public Boolean getStopValue() {
        return stopValue;
    }

}
