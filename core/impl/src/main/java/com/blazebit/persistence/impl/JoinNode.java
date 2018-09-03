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

import com.blazebit.persistence.From;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.parser.expression.BaseNode;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinNode implements From, ExpressionModifier, BaseNode {

    private JoinType joinType = JoinType.LEFT;
    private boolean fetch = false;

    // We need this for count and id queries where we do not need all the joins
    private final EnumSet<ClauseType> clauseDependencies = EnumSet.noneOf(ClauseType.class);

    private final JoinNode parent;
    private final JoinTreeNode parentTreeNode;

    private final JoinNode correlationParent;
    private final String correlationPath;
    private final Type<?> nodeType;
    private final EntityType<?> treatType;
    private final String valuesTypeName;
    private final int valueCount;
    private final String valuesIdName;
    private final String valuesCastedParameter;
    private final String[] valuesAttributes;
    private final String qualificationExpression;
    private final JoinAliasInfo aliasInfo;
    private final List<JoinNode> joinNodesForTreatConstraint;

    private final Map<String, JoinTreeNode> nodes = new TreeMap<>(); // Use TreeMap so that joins get applied alphabetically for easier testing
    private final Map<String, JoinNode> treatedJoinNodes = new TreeMap<>();
    private final Set<JoinNode> entityJoinNodes = new LinkedHashSet<>();

    // contains other join nodes which this node depends on
    private final Set<JoinNode> dependencies = new HashSet<>();

    private CompoundPredicate onPredicate;
    
    // Cache
    private boolean dirty = true;
    private boolean cardinalityMandatory;

    private JoinNode(TreatedJoinAliasInfo treatedJoinAliasInfo) {
        JoinNode treatedJoinNode = treatedJoinAliasInfo.getTreatedJoinNode();
        this.parent = treatedJoinNode.parent;
        this.parentTreeNode = treatedJoinNode.parentTreeNode;
        this.joinType = treatedJoinNode.joinType;
        this.correlationParent = treatedJoinNode.correlationParent;
        this.correlationPath = treatedJoinNode.correlationPath;
        this.nodeType = treatedJoinNode.nodeType;
        this.treatType = treatedJoinAliasInfo.getTreatType();
        this.qualificationExpression = treatedJoinNode.qualificationExpression;
        this.valuesTypeName = treatedJoinNode.valuesTypeName;
        this.valueCount = treatedJoinNode.valueCount;
        this.valuesIdName = treatedJoinNode.valuesIdName;
        this.valuesCastedParameter = treatedJoinNode.valuesCastedParameter;
        this.valuesAttributes = treatedJoinNode.valuesAttributes;
        this.aliasInfo = treatedJoinAliasInfo;
        List<JoinNode> joinNodesForTreatConstraint = new ArrayList<>(treatedJoinNode.joinNodesForTreatConstraint.size() + 1);
        joinNodesForTreatConstraint.addAll(treatedJoinNode.joinNodesForTreatConstraint);
        joinNodesForTreatConstraint.add(this);
        this.joinNodesForTreatConstraint = Collections.unmodifiableList(joinNodesForTreatConstraint);
    }

    private JoinNode(JoinNode parent, JoinTreeNode parentTreeNode, JoinType joinType, JoinNode correlationParent, String correlationPath, Type<?> nodeType, EntityType<?> treatType, String qualificationExpression, JoinAliasInfo aliasInfo) {
        this.parent = parent;
        this.parentTreeNode = parentTreeNode;
        this.joinType = joinType;
        this.correlationParent = correlationParent;
        this.correlationPath = correlationPath;
        this.nodeType = nodeType;
        this.treatType = treatType;
        this.valuesTypeName = null;
        this.valueCount = 0;
        this.valuesIdName = null;
        this.valuesCastedParameter = null;
        this.valuesAttributes = null;
        this.qualificationExpression = qualificationExpression;
        this.aliasInfo = aliasInfo;
        if (treatType != null) {
            if (parent != null) {
                List<JoinNode> joinNodesForTreatConstraint = new ArrayList<>(parent.joinNodesForTreatConstraint.size() + 1);
                joinNodesForTreatConstraint.addAll(parent.joinNodesForTreatConstraint);
                joinNodesForTreatConstraint.add(this);
                this.joinNodesForTreatConstraint = Collections.unmodifiableList(joinNodesForTreatConstraint);
            } else {
                this.joinNodesForTreatConstraint = Collections.singletonList(this);
            }
        } else {
            if (parent != null) {
                this.joinNodesForTreatConstraint = parent.joinNodesForTreatConstraint;
            } else {
                this.joinNodesForTreatConstraint = Collections.emptyList();
            }
        }
        onUpdate(null);
    }

    private JoinNode(ManagedType<?> nodeType, String valuesTypeName, int valueCount, String valuesIdName, String valuesCastedParameter, String[] valuesAttributes, JoinAliasInfo aliasInfo) {
        this.parent = null;
        this.parentTreeNode = null;
        this.joinType = null;
        this.correlationParent = null;
        this.correlationPath = null;
        this.nodeType = nodeType;
        this.treatType = null;
        this.valuesTypeName = valuesTypeName;
        this.valueCount = valueCount;
        this.valuesIdName = valuesIdName;
        this.valuesCastedParameter = valuesCastedParameter;
        this.valuesAttributes = valuesAttributes;
        this.qualificationExpression = null;
        this.aliasInfo = aliasInfo;
        this.joinNodesForTreatConstraint = Collections.emptyList();
        onUpdate(null);
    }

    public static JoinNode createRootNode(EntityType<?> nodeType, JoinAliasInfo aliasInfo) {
        return new JoinNode(null, null, null, null, null, nodeType, null, null, aliasInfo);
    }

    public static JoinNode createValuesRootNode(ManagedType<?> nodeType, String valuesTypeName, int valueCount, String valuesIdName, String valuesCastedParameter, String[] valuesAttributes, JoinAliasInfo aliasInfo) {
        return new JoinNode(nodeType, valuesTypeName, valueCount, valuesIdName, valuesCastedParameter, valuesAttributes, aliasInfo);
    }

    public static JoinNode createCorrelationRootNode(JoinNode correlationParent, String correlationPath, Type<?> nodeType, EntityType<?> treatType, JoinAliasInfo aliasInfo) {
        return new JoinNode(null, null, null, correlationParent, correlationPath, nodeType, treatType, null, aliasInfo);
    }

    public static JoinNode createEntityJoinNode(JoinNode parent, JoinType joinType, EntityType<?> nodeType, JoinAliasInfo aliasInfo) {
        return new JoinNode(parent, null, joinType, null, null, nodeType, null, null, aliasInfo);
    }

    public static JoinNode createAssociationJoinNode(JoinNode parent, JoinTreeNode parentTreeNode, JoinType joinType, Type<?> nodeType, EntityType<?> treatType, String qualificationExpression, JoinAliasInfo aliasInfo) {
        return new JoinNode(parent, parentTreeNode, joinType, null, null, nodeType, treatType, qualificationExpression, aliasInfo);
    }

    public JoinNode cloneRootNode(JoinAliasInfo aliasInfo) {
        // NOTE: no cloning of treatedJoinNodes and entityJoinNodes is intentional
        JoinNode newNode;
        if (valueCount > 0) {
            newNode = createValuesRootNode((ManagedType<?>) nodeType, valuesTypeName, valueCount, valuesIdName, valuesCastedParameter, valuesAttributes, aliasInfo);
        } else if (joinType == null) {
            newNode = createRootNode((EntityType<?>) nodeType, aliasInfo);
        } else {
            throw new UnsupportedOperationException("Cloning subqueries not yet implemented!");
        }

        newNode.getClauseDependencies().addAll(clauseDependencies);

        return newNode;
    }

    public JoinNode cloneJoinNode(JoinNode parent, JoinTreeNode parentTreeNode, JoinAliasInfo aliasInfo) {
        // NOTE: no cloning of onPredicate, treatedJoinNodes and entityJoinNodes is intentional
        JoinNode newNode;
        if (parentTreeNode == null) {
            newNode = createEntityJoinNode(parent, joinType, (EntityType<?>) nodeType, aliasInfo);
        } else {
            newNode = createAssociationJoinNode(parent, parentTreeNode, joinType, nodeType, treatType, qualificationExpression, aliasInfo);
        }

        newNode.fetch = fetch;
        newNode.getClauseDependencies().addAll(clauseDependencies);

        return newNode;
    }
    
    private void onUpdate(StateChange stateChange) {
        // Once mandatory, only a type change can cause a change of the cardinality mandatory
        if (cardinalityMandatory && stateChange != StateChange.JOIN_TYPE) {
            return;
        }
        
        dirty = true;
        if (parent != null) {
            parent.onUpdate(StateChange.CHILD);
        }
    }

    public boolean isCardinalityMandatory() {
        if (dirty) {
            updateCardinalityMandatory();
            dirty = false;
        }
        
        return cardinalityMandatory;
    }
    
    private void updateCardinalityMandatory() {
        boolean computedMandatory = false;
        if (joinType == JoinType.INNER) {
            // If the relation is optional/nullable or the join has a condition
            // the join is mandatory, because omitting it might change the semantics of the result set
            // NOTE: entity join nodes(the ones which don't have a parentTreeNode) are considered mandatory for now
            if (parentTreeNode == null || parentTreeNode.isOptional() || !isEmptyCondition()) {
                computedMandatory = true;
            }
        } else if (joinType == JoinType.LEFT) {
            // If the join has a condition which is not an array expression condition
            // we definitively need the join
            // NOTE: an array expression condition with a left join will always produce 1 row
            // so the join is not yet absolutely mandatory 
            if (!isEmptyCondition() && !isArrayExpressionCondition()) {
                computedMandatory = true;
            }

            // Check if any of the child nodes is mandatory for the cardinality
            OUTER: for (Map.Entry<String, JoinTreeNode> nodeEntry : nodes.entrySet()) {
                JoinTreeNode treeNode = nodeEntry.getValue();

                for (JoinNode childNode : treeNode.getJoinNodes().values()) {
                    if (childNode.isCardinalityMandatory()) {
                        computedMandatory = true;
                        break OUTER;
                    }
                }
            }
        }
        
        if (computedMandatory != cardinalityMandatory) {
            cardinalityMandatory = computedMandatory;
        }
    }
    
    private boolean isEmptyCondition() {
        return onPredicate == null || onPredicate.getChildren().isEmpty();
    }
    
    private boolean isArrayExpressionCondition() {
        if (onPredicate == null || onPredicate.getChildren().size() != 1) {
            return false;
        }

        Predicate predicate = onPredicate.getChildren().get(0);
        if (!(predicate instanceof EqPredicate)) {
            return false;
        }

        EqPredicate eqPredicate = (EqPredicate) predicate;
        Expression left = eqPredicate.getLeft();
        if (left instanceof MapKeyExpression) {
            return this.equals(((MapKeyExpression) left).getPath().getBaseNode());
        }
        if (left instanceof ListIndexExpression) {
            return this.equals(((ListIndexExpression) left).getPath().getBaseNode());
        }

        return false;
    }

    public void registerDependencies() {
        if (onPredicate != null) {
            onPredicate.accept(new VisitorAdapter() {

                @Override
                public void visit(PathExpression pathExpr) {
                    // prevent loop dependencies to the same join node and dependencies to qualified join nodes
                    JoinNode baseNode = (JoinNode) pathExpr.getBaseNode();
                    if (baseNode != null && baseNode != JoinNode.this && (baseNode.getQualificationExpression() == null || baseNode.parent != JoinNode.this)) {
                        dependencies.add(baseNode);
                    }
                }
            });
        }
    }

    @Override
    public void set(Expression expression) {
        if (!(expression instanceof CompoundPredicate)) {
            throw new IllegalArgumentException("Expected compound predicate but was given: " + expression);
        }
        onPredicate = (CompoundPredicate) expression;
    }

    @Override
    public Expression get() {
        return onPredicate;
    }

    public void accept(ExpressionModifierVisitor<? super ExpressionModifier> visitor) {
        if (onPredicate != null) {
            visitor.visit(this, ClauseType.JOIN);
        }
        for (JoinTreeNode treeNode : nodes.values()) {
            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                joinNode.accept(visitor);
            }
        }
        for (JoinNode joinNode : entityJoinNodes) {
            joinNode.accept(visitor);
        }
        for (JoinNode joinNode : treatedJoinNodes.values()) {
            joinNode.accept(visitor);
        }
    }

    public void accept(JoinNodeVisitor visitor) {
        visitor.visit(this);
        for (JoinTreeNode treeNode : nodes.values()) {
            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                joinNode.accept(visitor);
            }
        }
        for (JoinNode joinNode : entityJoinNodes) {
            joinNode.accept(visitor);
        }
        for (JoinNode joinNode : treatedJoinNodes.values()) {
            joinNode.accept(visitor);
        }
    }

    public <T> T accept(AbortableResultJoinNodeVisitor<T> visitor) {
        T result = visitor.visit(this);

        if (visitor.getStopValue().equals(result)) {
            return result;
        }

        for (JoinTreeNode treeNode : nodes.values()) {
            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                result = joinNode.accept(visitor);

                if (visitor.getStopValue().equals(result)) {
                    return result;
                }
            }
        }
        for (JoinNode joinNode : entityJoinNodes) {
            result = joinNode.accept(visitor);

            if (visitor.getStopValue().equals(result)) {
                return result;
            }
        }
        for (JoinNode joinNode : treatedJoinNodes.values()) {
            result = joinNode.accept(visitor);

            if (visitor.getStopValue().equals(result)) {
                return result;
            }
        }

        return result;
    }

    public JoinNode getTreatedJoinNode(EntityType<?> type) {
        String typeName = type.getJavaType().getName();
        JoinNode treatedNode = treatedJoinNodes.get(typeName);
        if (treatedNode != null) {
            return treatedNode;
        }

        TreatedJoinAliasInfo treatedJoinAliasInfo = new TreatedJoinAliasInfo(this, type);
        treatedNode = new JoinNode(treatedJoinAliasInfo);
        treatedJoinAliasInfo.setJoinNode(treatedNode);
        treatedJoinNodes.put(typeName, treatedNode);
        return treatedNode;
    }

    public EnumSet<ClauseType> getClauseDependencies() {
        return clauseDependencies;
    }

    public void updateClauseDependencies(ClauseType clauseDependency) {
        // update the ON clause dependent nodes to also have a clause dependency
        for (JoinNode dependency : dependencies) {
            dependency.updateClauseDependencies(clauseDependency);
        }

        clauseDependencies.add(clauseDependency);

        // If the parent node was a dependency, we are done with cycle checking
        // as it has been checked by the recursive call before
        if (parent != null && !dependencies.contains(parent)) {
            parent.updateClauseDependencies(clauseDependency);
        }
    }

    public boolean updateClauseDependencies(ClauseType clauseDependency, boolean forceAdd, Set<JoinNode> seenNodes) {
        if (!seenNodes.add(this)) {
            StringBuilder errorSb = new StringBuilder();
            errorSb.append("Cyclic join dependency between nodes: ");
            for (JoinNode seenNode : seenNodes) {
                errorSb.append(seenNode.getAliasInfo().getAlias());
                if (seenNode.getAliasInfo().isImplicit()) {
                    errorSb.append('(').append(seenNode.getAliasInfo().getAbsolutePath()).append(')');
                }
                errorSb.append(" -> ");
            }
            errorSb.setLength(errorSb.length() - 4);

            throw new IllegalStateException(errorSb.toString());
        }

        // By default, we add all clause dependency, but we try to reduce JOIN clause dependencies
        boolean add;

        if (clauseDependency != ClauseType.JOIN) {
            add = true;
        } else {
            // We don't need a JOIN clause dependencies on unrestricted non-optional or outer joins
            if (joinType == JoinType.INNER) {
                add = parentTreeNode == null || parentTreeNode.isOptional() || !isEmptyCondition();
            } else {
                // We never need left joins to retain the parent's cardinality
                add = false;
            }
        }

        // update the ON clause dependent nodes to also have a clause dependency
        for (JoinNode dependency : dependencies) {
            dependency.updateClauseDependencies(clauseDependency, add, seenNodes);
        }

        // If the parent node was a dependency, we are done with cycle checking
        // as it has been checked by the recursive call before
        if (parent != null && !dependencies.contains(parent)) {
            add = parent.updateClauseDependencies(clauseDependency, add, seenNodes);
        }

        add = add || forceAdd;
        if (add) {
            clauseDependencies.add(clauseDependency);
        }

        seenNodes.remove(this);
        return add;
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

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
        onUpdate(StateChange.JOIN_TYPE);
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

    public Map<String, JoinNode> getTreatedJoinNodes() {
        return treatedJoinNodes;
    }

    public JoinTreeNode getOrCreateTreeNode(String joinRelationName, Attribute<?, ?> attribute) {
        JoinTreeNode node = nodes.get(joinRelationName);

        if (node == null) {
            node = new JoinTreeNode(joinRelationName, attribute);
            nodes.put(joinRelationName, node);
        }

        return node;
    }

    public Set<JoinNode> getEntityJoinNodes() {
        return entityJoinNodes;
    }

    public void addEntityJoin(JoinNode entityJoinNode) {
        entityJoinNodes.add(entityJoinNode);
    }

    public Type<?> getNodeType() {
        if (treatType != null) {
            return treatType;
        }
        return nodeType;
    }

    public EntityType<?> getEntityType() {
        if (treatType != null) {
            return treatType;
        }
        if (nodeType instanceof EntityType<?>) {
            return (EntityType<?>) nodeType;
        }

        throw new IllegalArgumentException("Expected type of join node to be an entity but isn't: " + JpaMetamodelUtils.getTypeName(nodeType));
    }

    public ManagedType<?> getManagedType() {
        if (treatType != null) {
            return treatType;
        }
        if (nodeType instanceof ManagedType<?>) {
            return (ManagedType<?>) nodeType;
        }

        throw new IllegalArgumentException("Expected type of join node to be a managed type but isn't: " + JpaMetamodelUtils.getTypeName(nodeType));
    }

    public Type<?> getBaseType() {
        return nodeType;
    }

    public EntityType<?> getTreatType() {
        return treatType;
    }

    public boolean isTreatJoinNode() {
        return treatType != null && !(aliasInfo instanceof TreatedJoinAliasInfo);
    }

    public boolean isTreatedJoinNode() {
        return treatType != null && aliasInfo instanceof TreatedJoinAliasInfo;
    }

    public int getValueCount() {
        return valueCount;
    }

    public String getValuesIdName() {
        return valuesIdName;
    }

    String getValuesTypeName() {
        return valuesTypeName;
    }

    public String getValuesCastedParameter() {
        return valuesCastedParameter;
    }

    public String[] getValuesAttributes() {
        return valuesAttributes;
    }

    public JoinNode getCorrelationParent() {
        return correlationParent;
    }

    public String getCorrelationPath() {
        return correlationPath;
    }

    public CompoundPredicate getOnPredicate() {
        return onPredicate;
    }

    public void setOnPredicate(CompoundPredicate onPredicate) {
        this.onPredicate = onPredicate;
        onUpdate(StateChange.ON_PREDICATE);
    }

    public Set<JoinNode> getDependencies() {
        return dependencies;
    }

    public boolean hasCollections() {
        if (!entityJoinNodes.isEmpty()) {
            return true;
        }

        List<JoinTreeNode> stack = new ArrayList<JoinTreeNode>();
        stack.addAll(nodes.values());

        for (JoinNode node : entityJoinNodes) {
            stack.addAll(node.getNodes().values());
        }

        for (JoinNode node : treatedJoinNodes.values()) {
            stack.addAll(node.getNodes().values());
        }

        while (!stack.isEmpty()) {
            JoinTreeNode treeNode = stack.remove(stack.size() - 1);

            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                if (treeNode.isCollection() && !joinNode.hasArrayExpressionPredicate()) {
                    return true;
                }
                stack.addAll(joinNode.nodes.values());
            }
        }

        return false;
    }

    public boolean hasArrayExpressionPredicate() {
        CompoundPredicate compoundPredicate = getOnPredicate();
        if (compoundPredicate == null || compoundPredicate.getChildren().isEmpty()) {
            return false;
        }
        Predicate predicate = compoundPredicate.getChildren().get(0);
        if (!(predicate instanceof EqPredicate)) {
            return false;
        }
        EqPredicate eqPredicate = (EqPredicate) predicate;
        JoinNode n;
        if (eqPredicate.getLeft() instanceof ListIndexExpression) {
            ListIndexExpression left = (ListIndexExpression) eqPredicate.getLeft();
            n = (JoinNode) left.getPath().getBaseNode();
        } else if (eqPredicate.getLeft() instanceof MapKeyExpression) {
            MapKeyExpression left = (MapKeyExpression) eqPredicate.getLeft();
            n = (JoinNode) left.getPath().getBaseNode();
        } else {
            n = null;
        }

        return n == this;
    }
    
    Set<JoinNode> getCollectionJoins() {
        Set<JoinNode> collectionJoins = new HashSet<JoinNode>();
        List<JoinTreeNode> stack = new ArrayList<JoinTreeNode>();
        stack.addAll(nodes.values());

        // TODO: Fix this with #216
        // For now we say entity joins are also collection joins because that affects size to count transformations
        for (JoinNode node : entityJoinNodes) {
            stack.addAll(node.getNodes().values());
        }

        for (JoinNode node : treatedJoinNodes.values()) {
            stack.addAll(node.getNodes().values());
        }

        collectionJoins.addAll(entityJoinNodes);

        while (!stack.isEmpty()) {
            JoinTreeNode treeNode = stack.remove(stack.size() - 1);

            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                if (treeNode.isCollection() && !joinNode.hasArrayExpressionPredicate()) {
                    collectionJoins.add(joinNode);
                }
                stack.addAll(joinNode.nodes.values());
            }
        }


        return collectionJoins;
    }

    List<JoinNode> getJoinNodesForTreatConstraint() {
        return joinNodesForTreatConstraint;
    }

    public String getQualificationExpression() {
        return qualificationExpression;
    }

    public boolean isQualifiedJoin() {
        return qualificationExpression != null;
    }

    public boolean hasElementCollectionJoins() {
        for (JoinTreeNode treeNode : nodes.values()) {
            if (treeNode.getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                return true;
            }
        }

        return false;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static enum StateChange {
        JOIN_TYPE,
        ON_PREDICATE,
        CHILD;
    }

    @Override
    public Expression createExpression(String field) {
        List<PathElementExpression> pathElements = new ArrayList<>();
        if (qualificationExpression != null) {
            PathExpression path = new PathExpression(Collections.<PathElementExpression>singletonList(new PropertyExpression(parent.getAlias())));
            if ("KEY".equalsIgnoreCase(qualificationExpression)) {
                pathElements.add(new MapKeyExpression(path));
            } else if ("INDEX".equalsIgnoreCase(qualificationExpression)) {
                pathElements.add(new ListIndexExpression(path));
            } else if ("ENTRY".equalsIgnoreCase(qualificationExpression)) {
                pathElements.add(new MapEntryExpression(path));
            }
        } else {
            pathElements.add(new PropertyExpression(aliasInfo.getAlias()));
        }

        if (field != null) {
            for (String fieldPart : field.split("\\.")) {
                pathElements.add(new PropertyExpression(fieldPart));
            }
        }

        if (valuesTypeName != null) {
            return new FunctionExpression("FUNCTION", Arrays.asList(
                    new StringLiteral("TREAT_" + valuesTypeName.toUpperCase()), new PathExpression(pathElements)
            ));
        } else {
            return new PathExpression(pathElements);
        }
    }

    public void appendDeReference(StringBuilder sb, String property) {
        appendDeReference(sb, property, false);
    }

    public void appendDeReference(StringBuilder sb, String property, boolean renderTreat) {
        appendAlias(sb, renderTreat);
        // If we have a valuesTypeName, the property can only be "value" which is already handled in appendAlias
        if (property != null && valuesTypeName == null) {
            sb.append('.').append(property);
        }
    }

    public void appendAlias(StringBuilder sb) {
        appendAlias(sb, false);
    }

    public void appendAlias(StringBuilder sb, boolean renderTreat) {
        if (valuesTypeName != null) {
            // NOTE: property should always be null
            sb.append("TREAT_");
            sb.append(valuesTypeName.toUpperCase()).append('(');
            sb.append(aliasInfo.getAlias());
            sb.append(".value");
            sb.append(')');
        } else {
            if (qualificationExpression != null) {
                boolean hasTreat = renderTreat && treatType != null;
                if (hasTreat) {
                    sb.append("TREAT(");
                }

                sb.append(qualificationExpression);
                sb.append('(');

                if (renderTreat) {
                    parent.getAliasInfo().render(sb);
                } else {
                    sb.append(parent.getAlias());
                }

                sb.append(')');

                if (hasTreat) {
                    sb.append(" AS ");
                    sb.append(treatType.getName());
                    sb.append(')');
                }
            } else {
                if (renderTreat) {
                    aliasInfo.render(sb);
                } else {
                    sb.append(aliasInfo.getAlias());
                }
            }
        }
    }

    /* Implementation of Root interface */

    @Override
    public String getAlias() {
        return aliasInfo.getAlias();
    }

    @Override
    public Type<?> getType() {
        return getNodeType();
    }

    @Override
    public Class<?> getJavaType() {
        if (treatType != null) {
            return treatType.getJavaType();
        }
        return nodeType.getJavaType();
    }

    public int getJoinDepth() {
        int i = 0;
        JoinNode joinNode = this;
        while ((joinNode = joinNode.getParent()) != null) {
            i++;
        }
        return i;
    }
}
