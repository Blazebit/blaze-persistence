/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
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
import com.blazebit.persistence.parser.expression.SubqueryExpression;
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
import java.util.NavigableMap;
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
    private final EntityType<?> valueType;
    private final Set<String> valuesIdNames;
    private final String valuesLikeClause;
    private final boolean valueClazzAttributeSingular;
    private final boolean valueClazzSimpleValue;
    private final String valuesLikeAttribute;
    private final String valuesCastedParameter;
    private final String[] valuesAttributes;
    private final String qualificationExpression;
    private final JoinAliasInfo aliasInfo;
    private final List<JoinNode> joinNodesForTreatConstraint;
    private final boolean lateral;

    private final NavigableMap<String, JoinTreeNode> nodes = new TreeMap<>(); // Use TreeMap so that joins get applied alphabetically for easier testing
    private final NavigableMap<String, JoinNode> treatedJoinNodes = new TreeMap<>();
    private final Set<JoinNode> entityJoinNodes = new LinkedHashSet<>();

    // contains other join nodes which this node depends on
    private final Set<JoinNode> dependencies = new HashSet<>();

    private CTEInfo inlineCte;
    private CompoundPredicate onPredicate;
    private List<JoinNode> joinNodesNeedingTreatConjunct;
    private String deReferenceFunction;
    
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
        this.valueType = treatedJoinNode.valueType;
        this.valuesIdNames = treatedJoinNode.valuesIdNames;
        this.valuesLikeClause = treatedJoinNode.valuesLikeClause;
        this.valueClazzAttributeSingular = treatedJoinNode.valueClazzAttributeSingular;
        this.valueClazzSimpleValue = treatedJoinNode.valueClazzSimpleValue;
        this.valuesLikeAttribute = treatedJoinNode.valuesLikeAttribute;
        this.valuesCastedParameter = treatedJoinNode.valuesCastedParameter;
        this.valuesAttributes = treatedJoinNode.valuesAttributes;
        this.aliasInfo = treatedJoinAliasInfo;
        this.lateral = treatedJoinNode.lateral;
        List<JoinNode> joinNodesForTreatConstraint = new ArrayList<>(treatedJoinNode.joinNodesForTreatConstraint.size() + 1);
        joinNodesForTreatConstraint.addAll(treatedJoinNode.joinNodesForTreatConstraint);
        joinNodesForTreatConstraint.add(this);
        this.joinNodesForTreatConstraint = Collections.unmodifiableList(joinNodesForTreatConstraint);
    }

    private JoinNode(JoinNode parent, JoinTreeNode parentTreeNode, JoinType joinType, JoinNode correlationParent, String correlationPath, Type<?> nodeType, EntityType<?> treatType, String qualificationExpression, JoinAliasInfo aliasInfo, boolean lateral) {
        this.parent = parent;
        this.parentTreeNode = parentTreeNode;
        this.joinType = joinType;
        this.correlationParent = correlationParent;
        this.correlationPath = correlationPath;
        this.nodeType = nodeType;
        this.treatType = treatType;
        this.lateral = lateral;
        this.valuesTypeName = null;
        this.valueCount = 0;
        this.valueType = null;
        this.valuesIdNames = null;
        this.valuesLikeClause = null;
        this.valueClazzAttributeSingular = false;
        this.valueClazzSimpleValue = false;
        this.valuesLikeAttribute = null;
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

    private JoinNode(Type<?> nodeType, EntityType<?> valueType, String valuesTypeName, int valueCount, Set<String> valuesIdNames, String valuesLikeClause, String valueClazzAttributeQualificationExpression, boolean valueClazzAttributeSingular, boolean valueClazzSimpleValue, String valuesLikeAttribute, String valuesCastedParameter, String[] valuesAttributes, JoinAliasInfo aliasInfo) {
        this.parent = null;
        this.parentTreeNode = null;
        this.joinType = null;
        this.correlationParent = null;
        this.correlationPath = null;
        this.nodeType = nodeType;
        this.treatType = null;
        this.valuesTypeName = valuesTypeName;
        this.valueCount = valueCount;
        this.valueType = valueType;
        this.valuesIdNames = valuesIdNames;
        this.valuesLikeClause = valuesLikeClause;
        this.valueClazzAttributeSingular = valueClazzAttributeSingular;
        this.valueClazzSimpleValue = valueClazzSimpleValue;
        this.valuesLikeAttribute = valuesLikeAttribute;
        this.valuesCastedParameter = valuesCastedParameter;
        this.valuesAttributes = valuesAttributes;
        this.qualificationExpression = valueClazzAttributeQualificationExpression;
        this.aliasInfo = aliasInfo;
        this.joinNodesForTreatConstraint = Collections.emptyList();
        this.lateral = false;
        onUpdate(null);
    }

    public static JoinNode createRootNode(EntityType<?> nodeType, JoinAliasInfo aliasInfo) {
        return new JoinNode(null, null, null, null, null, nodeType, null, null, aliasInfo, false);
    }

    public static JoinNode createSimpleValuesRootNode(MainQuery mainQuery, Class<?> nodeType, int valueCount, JoinAliasInfo aliasInfo) {
        String sqlType = mainQuery.dbmsDialect.getSqlType(Long.class);
        String valuesTypeName = mainQuery.cbf.getNamedTypes().get(Long.class);
        String valuesCastedParameter = mainQuery.dbmsDialect.cast("?", sqlType);
        return new JoinNode(mainQuery.metamodel.type(nodeType), mainQuery.metamodel.entity(ValuesEntity.class), valuesTypeName, valueCount, null, null, null, true, true, "value", valuesCastedParameter, new String[] { "value" }, aliasInfo);
    }

    public static JoinNode createValuesRootNode(Type<?> nodeType, EntityType<?> valueType, String valuesTypeName, int valueCount, Set<String> valuesIdName, String valuesLikeClause, String qualificationExpression, boolean valueClazzAttributeSingular, boolean valueClazzSimpleValue, String valuesLikeAttribute, String valuesCastedParameter, String[] valuesAttributes, JoinAliasInfo aliasInfo) {
        return new JoinNode(nodeType, valueType, valuesTypeName, valueCount, valuesIdName, valuesLikeClause, qualificationExpression, valueClazzAttributeSingular, valueClazzSimpleValue, valuesLikeAttribute, valuesCastedParameter, valuesAttributes, aliasInfo);
    }

    public static JoinNode createCorrelationRootNode(JoinNode correlationParent, String correlationPath, Attribute<?, ?> correlatedAttribute, Type<?> nodeType, EntityType<?> treatType, JoinAliasInfo aliasInfo, boolean lateral) {
        return new JoinNode(lateral ? correlationParent : null, new JoinTreeNode(correlationPath, correlatedAttribute), null, correlationParent, correlationPath, nodeType, treatType, null, aliasInfo, lateral);
    }

    public static JoinNode createEntityJoinNode(JoinNode parent, JoinType joinType, EntityType<?> nodeType, JoinAliasInfo aliasInfo, boolean lateral) {
        return new JoinNode(parent, null, joinType, null, null, nodeType, null, null, aliasInfo, lateral);
    }

    public static JoinNode createAssociationJoinNode(JoinNode parent, JoinTreeNode parentTreeNode, JoinType joinType, Type<?> nodeType, EntityType<?> treatType, String qualificationExpression, JoinAliasInfo aliasInfo) {
        return new JoinNode(parent, parentTreeNode, joinType, null, null, nodeType, treatType, qualificationExpression, aliasInfo, false);
    }

    public JoinNode cloneRootNode(JoinAliasInfo aliasInfo) {
        // NOTE: no cloning of treatedJoinNodes and entityJoinNodes is intentional
        JoinNode newNode;
        if (valueCount > 0) {
            newNode = createValuesRootNode(nodeType, valueType, valuesTypeName, valueCount, valuesIdNames, valuesLikeClause, qualificationExpression, valueClazzAttributeSingular, valueClazzSimpleValue, valuesLikeAttribute, valuesCastedParameter, valuesAttributes, aliasInfo);
        } else if (correlationParent == null) {
            newNode = createRootNode((EntityType<?>) nodeType, aliasInfo);
        } else {
            JoinAliasInfo parentAliasInfo = (JoinAliasInfo) aliasInfo.getAliasOwner().getAliasInfo(correlationParent.getAlias());
            newNode = createCorrelationRootNode(parentAliasInfo.getJoinNode(), correlationPath, parentTreeNode.getAttribute(), nodeType, treatType, aliasInfo, false);
        }

        newNode.getClauseDependencies().addAll(clauseDependencies);

        return newNode;
    }

    public JoinNode cloneJoinNode(JoinNode parent, JoinTreeNode parentTreeNode, JoinAliasInfo aliasInfo) {
        // NOTE: no cloning of onPredicate, treatedJoinNodes and entityJoinNodes is intentional
        JoinNode newNode;
        if (parentTreeNode == null) {
            newNode = createEntityJoinNode(parent, joinType, (EntityType<?>) nodeType, aliasInfo, lateral);
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

    public boolean containsNode(JoinNode n, String joinRelationName) {
        if (parent == n && parentTreeNode != null && joinRelationName.equals(parentTreeNode.getRelationName())) {
            return onPredicate == null;
        }
        return parent != null && parent.containsNode(n, joinRelationName);
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
                public void visit(SubqueryExpression expression) {
                    ((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery()).applyVisitor(this);
                }

                @Override
                public void visit(PathExpression pathExpr) {
                    // prevent loop dependencies to the same join node and dependencies to qualified join nodes
                    JoinNode baseNode = (JoinNode) pathExpr.getBaseNode();
                    // Also, we ensure we only add dependencies to nodes that are of the same query. Dependencies on subquery nodes wouldn't make sense
                    if (baseNode != null && baseNode != JoinNode.this && baseNode.aliasInfo.getAliasOwner() == aliasInfo.getAliasOwner() && (baseNode.getQualificationExpression() == null || baseNode.parent != JoinNode.this)) {
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
        T stopValue = visitor.getStopValue();
        T result = visitor.visit(this);

        if (stopValue.equals(result)) {
            return result;
        }

        for (JoinTreeNode treeNode : nodes.values()) {
            for (JoinNode joinNode : treeNode.getJoinNodes().values()) {
                result = joinNode.accept(visitor);

                if (stopValue.equals(result)) {
                    return result;
                }
            }
        }
        for (JoinNode joinNode : entityJoinNodes) {
            result = joinNode.accept(visitor);

            if (stopValue.equals(result)) {
                return result;
            }
        }
        for (JoinNode joinNode : treatedJoinNodes.values()) {
            result = joinNode.accept(visitor);

            if (stopValue.equals(result)) {
                return result;
            }
        }

        return result;
    }

    public JoinNode getTreatedJoinNode(EntityType<?> type) {
        // Return this when the treat is an upcast
        if (type.getJavaType().isAssignableFrom(getJavaType())) {
            return this;
        }
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

    public boolean updateClauseDependencies(ClauseType clauseDependency, Set<JoinNode> seenNodes) {
        return updateClauseDependencies(clauseDependency, false, false, seenNodes);
    }

    private boolean updateClauseDependencies(ClauseType clauseDependency, boolean forceAdd, boolean forceAddAll, Set<JoinNode> seenNodes) {
        if (seenNodes != null && !seenNodes.add(this)) {
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

        if (clauseDependencies.contains(clauseDependency)) {
            if (seenNodes != null) {
                seenNodes.remove(this);
            }
            return true;
        }

        // By default, we add all clause dependency, but we try to reduce JOIN clause dependencies
        boolean add;

        if (clauseDependency != ClauseType.JOIN || forceAddAll) {
            add = true;
        } else {
            // We don't need a JOIN clause dependencies on unrestricted non-optional or outer joins
            if (joinType == JoinType.INNER) {
                add = forceAddAll = parentTreeNode == null || parentTreeNode.isOptional() || !isEmptyCondition();
            } else {
                // We never need left joins to retain the parent's cardinality
                add = false;
            }
        }

        // update the ON clause dependent nodes to also have a clause dependency
        for (JoinNode dependency : dependencies) {
            dependency.updateClauseDependencies(clauseDependency, add, forceAddAll, seenNodes);
        }

        // If the parent node was a dependency, we are done with cycle checking
        // as it has been checked by the recursive call before
        if (parent != null && !dependencies.contains(parent)) {
            add = parent.updateClauseDependencies(clauseDependency, add, forceAddAll, seenNodes);
        }

        add = add || forceAdd;
        if (add) {
            clauseDependencies.add(clauseDependency);
        }

        if (seenNodes != null) {
            seenNodes.remove(this);
        }
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

    public NavigableMap<String, JoinTreeNode> getNodes() {
        return nodes;
    }

    public NavigableMap<String, JoinNode> getTreatedJoinNodes() {
        return treatedJoinNodes;
    }

    public JoinNode getKeyJoinNode() {
        JoinTreeNode treeNode = getOrCreateTreeNode("KEY(" + getParentTreeNode().getRelationName() + ")", getParentTreeNode().getAttribute());
        return treeNode.getDefaultNode();
    }

    public JoinTreeNode getOrCreateTreeNode(String joinRelationName, Attribute<?, ?> attribute) {
        JoinTreeNode node = nodes.get(joinRelationName);

        if (node == null) {
            node = new JoinTreeNode(joinRelationName, attribute);
            nodes.put(joinRelationName, node);
        }

        return node;
    }

    public JoinNode getDefaultJoin(List<PathElementExpression> pathElements, int start, int end) {
        PathElementExpression pathElementExpression = pathElements.get(start);
        JoinTreeNode node = nodes.get(pathElementExpression.toString());
        if (node != null) {
            return node.getDefaultNode();
        }

        return null;
    }

    public boolean hasDefaultJoin(String joinRelationName) {
        JoinTreeNode node = nodes.get(joinRelationName);
        return node != null && node.getDefaultNode() != null;
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

    public EntityType<?> getInternalEntityType() {
        if (valueType != null) {
            return valueType;
        }
        return getEntityType();
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

    public boolean isEntityJoinNode() {
        return parentTreeNode == null;
    }

    public boolean isRootJoinNode() {
        return parent == null && (parentTreeNode == null || correlationParent != null);
    }

    public boolean isDefaultJoinNode() {
        return aliasInfo.isImplicit();
    }

    public int getValueCount() {
        return valueCount;
    }

    public EntityType<?> getValueType() {
        return valueType;
    }

    public boolean isValueClazzAttributeSingular() {
        return valueClazzAttributeSingular;
    }

    public boolean isValueClazzSimpleValue() {
        return valueClazzSimpleValue;
    }

    public String getValuesLikeAttribute() {
        return valuesLikeAttribute;
    }

    public String getValueClazzAlias(String prefix) {
        StringBuilder sb = new StringBuilder();
        appendValueClazzAlias(sb, prefix);
        return sb.toString();
    }

    public void appendValueClazzAlias(StringBuilder sb, String prefix) {
        if (qualificationExpression == null) {
            sb.append(prefix).append(valuesLikeAttribute.replace('.', '_'));
        } else {
            sb.append(prefix).append(valuesAttributes[0].replace('.', '_')).append('_').append(qualificationExpression.toLowerCase());
        }
    }

    public Set<String> getValuesIdNames() {
        return valuesIdNames;
    }

    public String getValuesLikeClause() {
        return valuesLikeClause;
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

    public boolean isCollection(ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector) {
        return parentTreeNode != null && parentTreeNode.isCollection() && !constantifiedJoinNodeAttributeCollector.isConstantified(this) || parentTreeNode == null && !constantifiedJoinNodeAttributeCollector.isConstantified(this);
    }

    List<JoinNode> getJoinNodesNeedingTreatConjunct() {
        return joinNodesNeedingTreatConjunct;
    }

    void setJoinNodesNeedingTreatConjunct(List<JoinNode> joinNodesNeedingTreatConjunct) {
        this.joinNodesNeedingTreatConjunct = joinNodesNeedingTreatConjunct;
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

    public boolean isLateral() {
        return lateral;
    }

    public void setInlineCte(CTEInfo inlineCte) {
        this.inlineCte = inlineCte;
    }

    public CTEInfo getInlineCte() {
        return inlineCte;
    }

    public boolean isInlineCte() {
        return inlineCte != null;
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
            List<PathElementExpression> pathElementExpressions = new ArrayList<>(1);
            pathElementExpressions.add(new PropertyExpression(parent.getAlias()));
            PathExpression path = new PathExpression(pathElementExpressions);
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

    public void appendDeReference(StringBuilder sb, String property, boolean externalRepresentation) {
        appendDeReference(sb, property, false, externalRepresentation, false);
    }

    public void appendDeReference(StringBuilder sb, String property, boolean renderTreat, boolean externalRepresentation, boolean requiresElementCollectionIdCutoff) {
        boolean wrapperFunction = false;
        if (!externalRepresentation && deReferenceFunction != null) {
            wrapperFunction = true;
            sb.append(deReferenceFunction);
        }
        appendAlias(sb, renderTreat, externalRepresentation);
        // If we have a valuesTypeName, the property can only be "value" which is already handled in appendAlias
        if (property != null && valuesTypeName == null) {
            if (requiresElementCollectionIdCutoff && parentTreeNode != null && parentTreeNode.getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION
                    && property.endsWith(".id")) {
                // See https://hibernate.atlassian.net/browse/HHH-13045 for details
                sb.append('.').append(property, 0, property.length() - ".id".length());
            } else {
                sb.append('.').append(property);
            }
        }
        if (wrapperFunction) {
            sb.append(')');
        }
    }

    public void appendAlias(StringBuilder sb, boolean externalRepresentation) {
        appendAlias(sb, false, externalRepresentation);
    }

    public void appendAlias(StringBuilder sb, boolean renderTreat, boolean externalRepresentation) {
        if (valuesTypeName != null) {
            if (externalRepresentation) {
                sb.append(aliasInfo.getAlias());
            } else {
                // NOTE: property should always be null
                sb.append("TREAT_");
                sb.append(valuesTypeName.toUpperCase()).append('(');
                sb.append(aliasInfo.getAlias());
                sb.append(".value");
                sb.append(')');
            }
        } else if (valuesLikeAttribute != null) {
            if (externalRepresentation) {
                sb.append(aliasInfo.getAlias());
            } else if (valueClazzAttributeSingular) {
                sb.append(aliasInfo.getAlias()).append('.').append(valuesLikeAttribute.replace('.', '_'));
            } else {
                if (qualificationExpression != null) {
                    sb.append(qualificationExpression);
                    sb.append('(');
                    sb.append(aliasInfo.getAlias()).append('_').append(valuesLikeAttribute.replace('.', '_'));
                    sb.append('_').append(qualificationExpression.toLowerCase());
                    sb.append(')');
                } else {
                    sb.append(aliasInfo.getAlias()).append('_').append(valuesLikeAttribute.replace('.', '_'));
                }
            }
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

    public String getDeReferenceFunction() {
        return deReferenceFunction;
    }

    public void setDeReferenceFunction(String deReferenceFunction) {
        this.deReferenceFunction = deReferenceFunction;
    }
    /* Implementation of Root interface */

    @Override
    public String getAlias() {
        return aliasInfo.getAlias();
    }

    public String getAliasExpression() {
        StringBuilder sb = new StringBuilder();
        appendAlias(sb, true, false);
        return sb.toString();
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
