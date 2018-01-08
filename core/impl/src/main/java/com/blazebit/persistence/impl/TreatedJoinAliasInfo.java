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

import javax.persistence.metamodel.EntityType;

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

    public TreatedJoinAliasInfo(JoinNode treatedJoinNode, EntityType<?> treatType) {
        super(
                treatedJoinNode.getAlias(),
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
