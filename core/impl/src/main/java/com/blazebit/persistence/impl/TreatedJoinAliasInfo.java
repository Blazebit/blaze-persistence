/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import jakarta.persistence.metamodel.EntityType;

/**
 * This is the join alias info for "special" join nodes that aren't rendered as joins
 * but only serve for providing a "treat-view" on an existing join node.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TreatedJoinAliasInfo extends JoinAliasInfo {

    private final JoinNode treatedJoinNode;
    private final EntityType<?> treatType;

    public TreatedJoinAliasInfo(JoinNode treatedJoinNode, EntityType<?> treatType, String alias) {
        super(
                alias,
                "TREAT(" + treatedJoinNode.getAliasInfo().getAbsolutePath() + " AS " + treatType.getName() + ")",
                treatedJoinNode.getAliasInfo().isImplicit(),
                treatedJoinNode.getAliasInfo().isRootNode(),
                treatedJoinNode.getAliasInfo().getAliasOwner()
        );
        this.treatedJoinNode = treatedJoinNode;
        this.treatType = treatType;
    }

    public JoinNode getTreatedJoinNode() {
        return treatedJoinNode;
    }

    public EntityType<?> getTreatType() {
        return treatType;
    }

    @Override
    public String getAbsolutePath() {
        return "TREAT(" + treatedJoinNode.getAliasInfo().getAbsolutePath() + " AS " + treatType.getName() + ")";
    }

    @Override
    public void render(StringBuilder sb) {
        sb.append("TREAT(").append(treatedJoinNode.getAliasInfo().getAlias()).append(" AS ").append(treatType.getName()).append(')');
    }
}
