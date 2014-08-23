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

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class JoinTreeNode {

    private final String relationName;
    private JoinNode defaultNode;
    private final boolean collection;
    /* maps join aliases to join nodes */
    private final Map<String, JoinNode> joinNodes = new TreeMap<String, JoinNode>();

    public JoinTreeNode(String relationName, boolean collection) {
        this.relationName = relationName;
        this.collection = collection;
    }

    public String getRelationName() {
        return relationName;
    }

    public JoinNode getDefaultNode() {
        return defaultNode;
    }

    public void setDefaultNode(JoinNode defaultNode) {
        this.defaultNode = defaultNode;
    }

    public Map<String, JoinNode> getJoinNodes() {
        return joinNodes;
    }

    public JoinNode getJoinNode(String alias, boolean defaultJoin) {
        if (defaultJoin) {
            return defaultNode;
        } else {
            return joinNodes.get(alias);
        }
    }

    public void addJoinNode(JoinNode node, boolean defaultJoin) {
        if (defaultJoin) {
            if (defaultNode != null) {
                throw new IllegalStateException("Tried to add a default node to the tree node although one already exists!");
            }
            defaultNode = node;
        }

        joinNodes.put(node.getAliasInfo().getAlias(), node);
    }

    public boolean isCollection() {
        return collection;
    }
}
