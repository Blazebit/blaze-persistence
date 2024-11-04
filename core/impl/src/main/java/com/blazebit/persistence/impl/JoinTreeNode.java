/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
