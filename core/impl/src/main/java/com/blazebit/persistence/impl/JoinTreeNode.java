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

import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

import java.util.NavigableMap;
import java.util.TreeMap;

import javax.persistence.metamodel.Attribute;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class JoinTreeNode {

    private final String relationName;
    private final Attribute<?, ?> attribute;
    private JoinNode defaultNode;
    private final boolean collection;
    private final boolean map;
    private final boolean optional;
    /* maps join aliases to join nodes */
    private final NavigableMap<String, JoinNode> joinNodes = new TreeMap<String, JoinNode>();

    public JoinTreeNode(String relationName, Attribute<?, ?> attribute) {
        this.relationName = relationName;
        this.attribute = attribute;
        this.collection = attribute.isCollection();
        this.map = JpaMetamodelUtils.isMap(attribute);
        this.optional = JpaMetamodelUtils.isOptional(attribute);
    }

    public String getRelationName() {
        return relationName;
    }

    public Attribute<?, ?> getAttribute() {
        return attribute;
    }

    public JoinNode getDefaultNode() {
        return defaultNode;
    }

    public NavigableMap<String, JoinNode> getJoinNodes() {
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

    public boolean isMap() {
        return map;
    }

    public boolean isOptional() {
        return optional;
    }
}
