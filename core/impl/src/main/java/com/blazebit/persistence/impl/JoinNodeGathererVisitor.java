/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class JoinNodeGathererVisitor extends VisitorAdapter {

    private Collection<JoinNode> joinNodes;

    public JoinNodeGathererVisitor() {
    }

    public JoinNodeGathererVisitor(Collection<JoinNode> joinNodes) {
        this.joinNodes = joinNodes;
    }

    public Set<JoinNode> collectNonRootJoinNodes(ResolvedExpression[] identifierExpressions) {
        joinNodes = new ArrayList<>();
        int size = identifierExpressions.length;
        for (int i = 0; i < size; i++) {
            identifierExpressions[i].getExpression().accept(this);
        }

        Set<JoinNode> nonRootJoinNodeClosure = new HashSet<>();
        List<JoinNode> nodes = (List<JoinNode>) joinNodes;
        size = nodes.size();
        for (int i = 0; i < size; i++) {
            JoinNode n = nodes.get(i);
            if (n.getParent() != null) {
                nonRootJoinNodeClosure.add(n);
                nonRootJoinNodeClosure.addAll(n.getDependencies());
            }
        }
        return nonRootJoinNodeClosure;
    }

    @Override
    public void visit(PathExpression expression) {
        joinNodes.add((JoinNode) expression.getBaseNode());
    }
}
