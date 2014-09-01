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

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.VisitorAdapter;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinNode {

    private JoinAliasInfo aliasInfo;
    private JoinType type = JoinType.LEFT;
    private boolean fetch = false;

    // We need this for count and id queries where we do not need all the joins
    private final EnumSet<ClauseType> clauseDependencies = EnumSet.noneOf(ClauseType.class);
    
    private final JoinNode parent;
    private final JoinTreeNode parentTreeNode;

    private final Class<?> propertyClass;
    private final Map<String, JoinTreeNode> nodes = new TreeMap<String, JoinTreeNode>(); // Use TreeMap so that joins get applied alphabetically for easier testing
    
    // contains other join nodes which this node depends on
    private final Set<JoinNode> dependencies = new HashSet<JoinNode>();

    private AndPredicate withPredicate;

    public JoinNode(JoinNode parent, JoinTreeNode parentTreeNode, JoinAliasInfo aliasInfo, JoinType type, Class<?> propertyClass) {
        this.parent = parent;
        this.parentTreeNode = parentTreeNode;
        this.aliasInfo = aliasInfo;
        this.type = type;
        this.propertyClass = propertyClass;
    }

    public void registerDependencies() {
        if (withPredicate != null) {
            withPredicate.accept(new VisitorAdapter() {
                @Override
                public void visit(PathExpression pathExpr) {
                    // prevent loop dependencies to the same join node
                    if (pathExpr.getBaseNode() != JoinNode.this) {
                        dependencies.add((JoinNode) pathExpr.getBaseNode());
                    }
                }
            });
        }
    }

    public void accept(JoinNodeVisitor visitor) {
        visitor.visit(this);
        for (JoinTreeNode treeNode : nodes.values()) {
            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                joinNode.accept(visitor);
            }
        }
    }

    public EnumSet<ClauseType> getClauseDependencies() {
        return clauseDependencies;
    }

    public JoinTreeNode getParentTreeNode() {
        return parentTreeNode;
    }

    public JoinNode getParent() {
        return parent;
    }

    public JoinAliasInfo getAliasInfo() {
        return aliasInfo;
    }

    public void setAliasInfo(JoinAliasInfo aliasInfo) {
        this.aliasInfo = aliasInfo;
    }

    public JoinType getType() {
        return type;
    }

    public void setType(JoinType type) {
        this.type = type;
    }

    public boolean isFetch() {
        return fetch;
    }

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }

    public Map<String, JoinTreeNode> getNodes() {
        return nodes;
    }

    public JoinTreeNode getOrCreateTreeNode(String joinRelationName, boolean collection) {
        JoinTreeNode node = nodes.get(joinRelationName);

        if (node == null) {
            node = new JoinTreeNode(joinRelationName, collection);
            nodes.put(joinRelationName, node);
        }

        return node;
    }

    public Class<?> getPropertyClass() {
        return propertyClass;
    }

    public AndPredicate getWithPredicate() {
        return withPredicate;
    }

    public void setWithPredicate(AndPredicate withPredicate) {
        this.withPredicate = withPredicate;
    }

    public Set<JoinNode> getDependencies() {
        return dependencies;
    }
}
