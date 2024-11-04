/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.JoinType;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class InnerJoinOnlyAbortableResultJoinNodeVisitor implements AbortableResultJoinNodeVisitor<Boolean> {

    public static final InnerJoinOnlyAbortableResultJoinNodeVisitor INSTANCE = new InnerJoinOnlyAbortableResultJoinNodeVisitor();

    private InnerJoinOnlyAbortableResultJoinNodeVisitor() {
    }

    @Override
    public Boolean getStopValue() {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(JoinNode node) {
        return node.getJoinType() != null && node.getJoinType() != JoinType.INNER;
    }
}
