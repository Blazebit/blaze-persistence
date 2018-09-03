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

import com.blazebit.lang.StringUtils;
import com.blazebit.lang.ValueRetriever;
import com.blazebit.persistence.From;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.Path;
import com.blazebit.persistence.impl.builder.predicate.JoinOnBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.parser.ListIndexAttribute;
import com.blazebit.persistence.parser.MapEntryAttribute;
import com.blazebit.persistence.parser.MapKeyAttribute;
import com.blazebit.persistence.parser.QualifiedAttribute;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PathReference;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.QualifiedExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.impl.util.Keywords;
import com.blazebit.persistence.parser.util.ExpressionUtils;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0.0
 */
public class JoinManager extends AbstractManager<ExpressionModifier> {

    private static final Logger LOG = Logger.getLogger(JoinManager.class.getName());

    // we might have multiple nodes that depend on the same unresolved alias,
    // hence we need a List of NodeInfos.
    // e.g. SELECT a.X, a.Y FROM A a
    // a is unresolved for both X and Y
    private final List<JoinNode> rootNodes = new ArrayList<JoinNode>(1);
    private final Set<JoinNode> entityFunctionNodes = new LinkedHashSet<>();
    // root entity class
    private final String joinRestrictionKeyword;
    private final MainQuery mainQuery;
    private final AliasManager aliasManager;
    private final EntityMetamodelImpl metamodel; // needed for model-aware joins
    private final JoinManager parent;
    private final JoinOnBuilderEndedListener joinOnBuilderListener;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder;

    // helper collections for join rendering
    private final Set<JoinNode> collectionJoinNodes = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());
    private final Set<JoinNode> renderedJoins = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());
    private final Set<JoinNode> markedJoinNodes = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());

    // Setting to force entity joins being rendered as cross joins. Needed for recursive CTEs with DB2..
    private boolean emulateJoins;

    JoinManager(MainQuery mainQuery, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, ResolvingQueryGenerator queryGenerator, AliasManager aliasManager, JoinManager parent, ExpressionFactory expressionFactory) {
        super(queryGenerator, mainQuery.parameterManager, null);
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.metamodel = mainQuery.metamodel;
        this.parent = parent;
        this.joinRestrictionKeyword = " " + mainQuery.jpaProvider.getOnClause() + " ";
        this.joinOnBuilderListener = new JoinOnBuilderEndedListener();
        this.subqueryInitFactory = new SubqueryInitiatorFactory(mainQuery, queryBuilder, aliasManager, this);
        this.expressionFactory = expressionFactory;
        this.queryBuilder = queryBuilder;
    }

    Map<JoinNode, JoinNode> applyFrom(JoinManager joinManager) {
        Map<JoinNode, JoinNode> nodeMapping = new IdentityHashMap<>();
        for (JoinNode node : joinManager.rootNodes) {
            JoinNode rootNode = applyFrom(nodeMapping, node);

            if (node.getValueCount() > 0) {
                // TODO: At the moment the value type is without meaning
                ParameterManager.ParameterImpl<?> param = joinManager.parameterManager.getParameter(node.getAlias());
                ValuesParameterBinder binder = ((ParameterManager.ValuesParameterWrapper) param.getParameterValue()).getBinder();

                parameterManager.registerValuesParameter(rootNode.getAlias(), null, binder.getParameterNames(), binder.getPathExpressions(), queryBuilder);
                entityFunctionNodes.add(rootNode);
            }
        }
        return nodeMapping;
    }

    private JoinNode applyFrom(Map<JoinNode, JoinNode> nodeMapping, JoinNode node) {
        String rootAlias = node.getAlias();
        boolean implicit = node.getAliasInfo().isImplicit();

        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, implicit, true, aliasManager);
        JoinNode rootNode;

        if (node.getCorrelationParent() != null) {
            throw new UnsupportedOperationException("Cloning subqueries not yet implemented!");
        } else {
            rootNode = node.cloneRootNode(rootAliasInfo);
        }

        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        nodeMapping.put(node, rootNode);

        for (JoinTreeNode treeNode : node.getNodes().values()) {
            applyFrom(nodeMapping, rootNode, treeNode);
        }

        for (JoinNode entityJoinNode : node.getEntityJoinNodes()) {
            rootNode.addEntityJoin(applyFrom(nodeMapping, rootNode, null, entityJoinNode.getAlias(), entityJoinNode));
        }

        if (!node.getTreatedJoinNodes().isEmpty()) {
            throw new UnsupportedOperationException("Cloning joins with treat joins is not yet implemented!");
        }

        return rootNode;
    }

    private void applyFrom(Map<JoinNode, JoinNode> nodeMapping, JoinNode parent, JoinTreeNode treeNode) {
        JoinTreeNode newTreeNode = parent.getOrCreateTreeNode(treeNode.getRelationName(), treeNode.getAttribute());
        for (Map.Entry<String, JoinNode> nodeEntry : treeNode.getJoinNodes().entrySet()) {
            JoinNode newNode = applyFrom(nodeMapping, parent, newTreeNode, nodeEntry.getKey(), nodeEntry.getValue());
            newTreeNode.addJoinNode(newNode, nodeEntry.getValue() == treeNode.getDefaultNode());
        }
    }

    private JoinNode applyFrom(Map<JoinNode, JoinNode> nodeMapping, JoinNode parent, JoinTreeNode treeNode, String alias, JoinNode oldNode) {
        JoinNode node;
        if (treeNode == null) {
            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, null, false, true, aliasManager);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = oldNode.cloneJoinNode(parent, null, newAliasInfo);
            newAliasInfo.setJoinNode(node);
        } else {
            boolean implicit = oldNode.getAliasInfo().isImplicit();
            String currentJoinPath = parent.getAliasInfo().getAbsolutePath() + "." + treeNode.getRelationName();
            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, false, aliasManager);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = oldNode.cloneJoinNode(parent, treeNode, newAliasInfo);
            newAliasInfo.setJoinNode(node);
        }
        nodeMapping.put(oldNode, node);

        if (oldNode.getOnPredicate() != null) {
            node.setOnPredicate(subqueryInitFactory.reattachSubqueries(oldNode.getOnPredicate().clone(true), ClauseType.JOIN));
        }

        for (JoinTreeNode oldTreeNode : oldNode.getNodes().values()) {
            applyFrom(nodeMapping, node, oldTreeNode);
        }

        for (JoinNode entityJoinNode : oldNode.getEntityJoinNodes()) {
            node.addEntityJoin(applyFrom(nodeMapping, node, null, entityJoinNode.getAlias(), entityJoinNode));
        }

        if (!oldNode.getTreatedJoinNodes().isEmpty()) {
            throw new UnsupportedOperationException("Cloning joins with treat joins is not yet implemented!");
        }

        return node;
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.JOIN;
    }

    Set<JoinNode> getKeyRestrictedLeftJoins() {
        if (!mainQuery.jpaProvider.needsJoinSubqueryRewrite()) {
            return Collections.emptySet();
        }

        Set<JoinNode> keyRestrictedLeftJoins = new HashSet<JoinNode>();
        acceptVisitor(new KeyRestrictedLeftJoinCollectingVisitor(mainQuery.jpaProvider, keyRestrictedLeftJoins));
        return keyRestrictedLeftJoins;
    }

    void removeSelectOnlyNodes(Set<JoinNode> candidateNodes) {
        int size = rootNodes.size();
        for (int i = 0; i < size; i++) {
            JoinNode rootNode = rootNodes.get(i);
            removeSelectOnlyNodes(candidateNodes, rootNode);
        }
    }

    private static void removeSelectOnlyNodes(Set<JoinNode> candidateNodes, JoinNode node) {
        Iterator<JoinTreeNode> iterator = node.getNodes().values().iterator();
        while (iterator.hasNext()) {
            JoinTreeNode joinTreeNode = iterator.next();

            removeSelectOnlyNodes(candidateNodes, joinTreeNode.getJoinNodes().values().iterator());

            if (joinTreeNode.getJoinNodes().isEmpty()) {
                iterator.remove();
            }
        }

        removeSelectOnlyNodes(candidateNodes, node.getEntityJoinNodes().iterator());
    }

    private static void removeSelectOnlyNodes(Set<JoinNode> candidateNodes, Iterator<JoinNode> joinNodeIterator) {
        while (joinNodeIterator.hasNext()) {
            JoinNode subNode = joinNodeIterator.next();
            if (candidateNodes.contains(subNode) && subNode.getClauseDependencies().size() == 1 && subNode.getClauseDependencies().contains(ClauseType.SELECT)) {
                joinNodeIterator.remove();
            } else {
                removeSelectOnlyNodes(candidateNodes, subNode);
            }
        }
    }

    public void collectCorrelatedRootExpressions(Collection<Expression> expressions) {
        int size = rootNodes.size();
        for (int i = 0; i < size; i++) {
            JoinNode rootNode = rootNodes.get(i);
            if (rootNode.getCorrelationParent() != null) {
                ExtendedManagedType<?> extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, rootNode.getCorrelationParent().getManagedType());
                for (SingularAttribute<?, ?> idAttribute : extendedManagedType.getIdAttributes()) {
                    List<PathElementExpression> pathElementExpressions = new ArrayList<>(2);
                    pathElementExpressions.add(new PropertyExpression(rootNode.getCorrelationParent().getAlias()));
                    pathElementExpressions.add(new PropertyExpression(idAttribute.getName()));
                    expressions.add(new PathExpression(pathElementExpressions, new SimplePathReference(rootNode.getCorrelationParent(), idAttribute.getName(), idAttribute.getType()), false, false));
                }
            }
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static class KeyRestrictedLeftJoinCollectingVisitor extends VisitorAdapter implements JoinNodeVisitor {

        final JpaProvider jpaProvider;
        final Set<JoinNode> keyRestrictedLeftJoins;

        public KeyRestrictedLeftJoinCollectingVisitor(JpaProvider jpaProvider, Set<JoinNode> keyRestrictedLeftJoins) {
            this.jpaProvider = jpaProvider;
            this.keyRestrictedLeftJoins = keyRestrictedLeftJoins;
        }

        @Override
        public void visit(JoinNode node) {
            if (node.getJoinType() == JoinType.LEFT && node.getOnPredicate() != null) {
                node.getOnPredicate().accept(this);
            }
        }

        @Override
        public void visit(MapKeyExpression expression) {
            super.visit(expression);
            visitKeyOrIndexExpression(expression.getPath());
        }

        @Override
        public void visit(ListIndexExpression expression) {
            super.visit(expression);
            visitKeyOrIndexExpression(expression.getPath());
        }

        private void visitKeyOrIndexExpression(PathExpression pathExpression) {
            JoinNode node = (JoinNode) pathExpression.getBaseNode();
            Attribute<?, ?> attribute = node.getParentTreeNode().getAttribute();
            // Exclude element collections as they are not problematic
            if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                // There are weird mappings possible, we have to check if the attribute is a join table
                if (jpaProvider.getJoinTable(node.getParent().getEntityType(), attribute.getName()) != null) {
                    keyRestrictedLeftJoins.add(node);
                }
            }
        }
    }

    String addRootValues(Class<?> clazz, Class<?> valueClazz, String rootAlias, int valueCount, String typeName, String castedParameter, boolean identifiableReference) {
        if (rootAlias == null) {
            throw new IllegalArgumentException("Illegal empty alias for the VALUES clause: " + clazz.getName());
        }
        // TODO: we should pad the value count to avoid filling query caches
        ManagedType<?> managedType = mainQuery.metamodel.getManagedType(clazz);
        String idAttributeName = null;
        Set<Attribute<?, ?>> attributeSet;

        if (identifiableReference) {
            SingularAttribute<?, ?> idAttribute = JpaMetamodelUtils.getSingleIdAttribute((EntityType<?>) managedType);
            idAttributeName = idAttribute.getName();
            attributeSet = (Set<Attribute<?, ?>>) (Set<?>) Collections.singleton(idAttribute);
        } else {
            Set<Attribute<?, ?>> originalAttributeSet = (Set<Attribute<?, ?>>) (Set) managedType.getAttributes();
            attributeSet = new TreeSet<>(JpaMetamodelUtils.ATTRIBUTE_NAME_COMPARATOR);
            for (Attribute<?, ?> attr : originalAttributeSet) {
                // Filter out collection attributes
                if (!attr.isCollection()) {
                    attributeSet.add(attr);
                }
            }
        }

        String[][] parameterNames = new String[valueCount][attributeSet.size()];
        ValueRetriever<Object, Object>[] pathExpressions = new ValueRetriever[attributeSet.size()];

        String[] attributes = initializeValuesParameter(clazz, identifiableReference, rootAlias, attributeSet, parameterNames, pathExpressions);
        parameterManager.registerValuesParameter(rootAlias, valueClazz, parameterNames, pathExpressions, queryBuilder);

        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = JoinNode.createValuesRootNode(managedType, typeName, valueCount, idAttributeName, castedParameter, attributes, rootAliasInfo);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        entityFunctionNodes.add(rootNode);
        return rootAlias;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    static class SimpleValueRetriever implements ValueRetriever<Object, Object> {
        @Override
        public Object getValue(Object target) {
            return target;
        }
    }

    private String[] initializeValuesParameter(Class<?> clazz, boolean identifiableReference, String prefix, Set<Attribute<?, ?>> attributeSet, String[][] parameterNames, ValueRetriever<?, ?>[] pathExpressions) {
        int valueCount = parameterNames.length;
        String[] attributes = new String[attributeSet.size()];

        if (clazz == ValuesEntity.class) {
            pathExpressions[0] = new SimpleValueRetriever();
            String attributeName = attributeSet.iterator().next().getName();
            attributes[0] = attributeName;
            for (int j = 0; j < valueCount; j++) {
                parameterNames[j][0] = prefix + '_' + attributeName + '_' + j;
            }
        } else if (identifiableReference) {
            Attribute<?, ?> attribute = attributeSet.iterator().next();
            String attributeName = attribute.getName();
            attributes[0] = attributeName;
            pathExpressions[0] = com.blazebit.reflection.ExpressionUtils.getExpression(clazz, attributeName);
            for (int j = 0; j < valueCount; j++) {
                parameterNames[j][0] = prefix + '_' + attributeName + '_' + j;
            }
        } else {
            Iterator<Attribute<?, ?>> iter = attributeSet.iterator();
            for (int i = 0; i < attributeSet.size(); i++) {
                Attribute<?, ?> attribute = iter.next();
                String attributeName = attribute.getName();
                attributes[i] = attributeName;
                pathExpressions[i] = com.blazebit.reflection.ExpressionUtils.getExpression(clazz, attributeName);
                for (int j = 0; j < valueCount; j++) {
                    parameterNames[j][i] = prefix + '_' + attributeName + '_' + j;
                }
            }
        }

        return attributes;
    }

    String addRoot(EntityType<?> entityType, String rootAlias) {
        if (rootAlias == null) {
            // TODO: not sure if other JPA providers support case sensitive queries like hibernate
            String entityTypeName = entityType.getName();
            // Handle the Envers generated entity name which is FQN_AUD
            int dotIdx;
            if ((dotIdx = entityTypeName.lastIndexOf('.')) != -1) {
                entityTypeName = entityTypeName.substring(dotIdx + 1);
            }
            StringBuilder sb = new StringBuilder(entityTypeName);
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            String alias = sb.toString();

            if (aliasManager.getAliasInfo(alias) == null && !Keywords.JPQL.contains(alias.toUpperCase())) {
                rootAlias = alias;
            } else {
                rootAlias = aliasManager.generateRootAlias(alias);
            }
        }
        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = JoinNode.createRootNode(entityType, rootAliasInfo);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        return rootAlias;
    }

    String addRoot(String correlationPath, String rootAlias) {
        Expression expr = expressionFactory.createJoinPathExpression(correlationPath);

        PathElementExpression elementExpr;
        EntityType<?> treatEntityType = null;
        JoinResult result;
        JoinNode correlationParent = null;
        if (expr instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expr;

            if (isJoinableSelectAlias(pathExpression, false, false)) {
                throw new IllegalArgumentException("No select alias allowed in join path");
            }

            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            elementExpr = pathElements.get(pathElements.size() - 1);
            if (pathElements.size() > 1) {
                result = implicitJoin(null, pathExpression, null, null, 0, pathElements.size() - 1, true);
                correlationParent = result.baseNode;
            } else {
                result = new JoinResult(null, null, null);
            }
        } else if (expr instanceof TreatExpression) {
            TreatExpression treatExpression = (TreatExpression) expr;
            Expression expression = treatExpression.getExpression();

            if (expression instanceof PathExpression) {
                PathExpression pathExpression = (PathExpression) expression;
                List<PathElementExpression> pathElements = pathExpression.getExpressions();
                elementExpr = pathElements.get(pathElements.size() - 1);
                result = implicitJoin(null, pathExpression, null, null, 0, pathElements.size() - 1, true);
                correlationParent = result.baseNode;
                treatEntityType = metamodel.entity(treatExpression.getType());
            } else {
                throw new IllegalArgumentException("Unexpected expression type[" + expression.getClass().getSimpleName() + "] in treat expression: " + treatExpression);
            }
        } else if (expr instanceof FunctionExpression && ExpressionUtils.isOuterFunction((FunctionExpression) expr)) {
            FunctionExpression outerFunctionExpr = (FunctionExpression) expr;
            PathExpression pathExpression = (PathExpression) outerFunctionExpr.getExpressions().get(0);
            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            elementExpr = pathElements.get(pathElements.size() - 1);
            result = implicitJoin(parent.getRootNodeOrFail("Can't use OUTER when parent query has multiple roots!"), pathExpression, null, null, 0, pathElements.size() - 1, true);
            correlationParent = result.baseNode;
        } else {
            throw new IllegalArgumentException("Correlation join path [" + correlationPath + "] is not a valid join path");
        }

        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        }

        if (correlationParent == null) {
            correlationParent = getRootNodeOrFail("Could not join correlation path [", correlationPath, "] because it did not use an absolute path but multiple root nodes are available!");
        }

        if (correlationParent.getAliasInfo().getAliasOwner() == aliasManager) {
            throw new IllegalArgumentException("The correlation path '" + correlationPath + "' does not seem to be part of a parent query!");
        }

        String correlatedAttribute;
        Expression correlatedAttributeExpr;

        if (result.hasField()) {
            correlatedAttribute = result.joinFields(elementExpr.toString());
            correlatedAttributeExpr = expressionFactory.createSimpleExpression(correlatedAttribute, false);
        } else {
            correlatedAttribute = elementExpr.toString();
            correlatedAttributeExpr = elementExpr;
        }

        AttributeHolder joinResult = JpaUtils.getAttributeForJoining(metamodel, correlationParent.getNodeType(), correlatedAttributeExpr, null);
        Type<?> type = joinResult.getAttributeType();

        if (rootAlias == null) {
            StringBuilder sb = new StringBuilder(JpaMetamodelUtils.getSimpleTypeName(type));
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            String alias = sb.toString();

            if (aliasManager.getAliasInfo(alias) == null) {
                rootAlias = alias;
            } else {
                rootAlias = aliasManager.generateRootAlias(alias);
            }
        }

        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = JoinNode.createCorrelationRootNode(correlationParent, correlatedAttribute, type, treatEntityType, rootAliasInfo);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        return rootAlias;
    }

    void removeRoot() {
        // We only use this to remove implicit root nodes
        JoinNode rootNode = rootNodes.remove(0);
        aliasManager.unregisterAliasInfoForBottomLevel(rootNode.getAliasInfo());
    }

    JoinNode getRootNodeOrFail(String string) {
        return getRootNodeOrFail(string, "", "");
    }

    JoinNode getRootNodeOrFail(String prefix, Object middle, String suffix) {
        if (rootNodes.size() > 1) {
            throw new IllegalArgumentException(prefix + middle + suffix);
        }

        return rootNodes.get(0);
    }

    JoinNode getRootNode(Expression expression) {
        String alias;
        if (expression instanceof PropertyExpression) {
            alias = expression.toString();
        } else {
            return null;
        }

        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            JoinNode node = nodes.get(i);
            if (alias.equals(node.getAliasInfo().getAlias())) {
                return node;
            }
        }

        return null;
    }

    public List<JoinNode> getRoots() {
        return rootNodes;
    }

    boolean hasCollections() {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            if (nodes.get(i).hasCollections()) {
                return true;
            }
        }

        return false;
    }

    // Since DB2 doesn't like joins in the recursive part of CTEs, we must be able to determine emulatable joins
    boolean hasNonEmulatableJoins() {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            JoinNode n = nodes.get(i);
            if (!n.getNodes().isEmpty()) {
                return true;
            }
            // Only inner joins can be emulated
            for (JoinNode joinNode : n.getEntityJoinNodes()) {
                if (joinNode.getJoinType() != JoinType.INNER) {
                    return true;
                }
            }

            if  (!n.getTreatedJoinNodes().isEmpty()) {
                for (JoinNode treatedNode : n.getTreatedJoinNodes().values()) {
                    if (!treatedNode.getNodes().isEmpty() || !treatedNode.getEntityJoinNodes().isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    boolean hasEntityFunctions() {
        return entityFunctionNodes.size() > 0;
    }

    public Set<JoinNode> getCollectionJoins() {
        if (rootNodes.isEmpty()) {
            return Collections.EMPTY_SET;
        } else {
            Set<JoinNode> collectionJoins = rootNodes.get(0).getCollectionJoins();
            for (int i = 1; i < rootNodes.size(); i++) {
                collectionJoins.addAll(rootNodes.get(i).getCollectionJoins());
            }
            return collectionJoins;
        }
    }

    Set<JoinNode> getEntityFunctionNodes() {
        return entityFunctionNodes;
    }

    public JoinManager getParent() {
        return parent;
    }

    public AliasManager getAliasManager() {
        return aliasManager;
    }

    public SubqueryInitiatorFactory getSubqueryInitFactory() {
        return subqueryInitFactory;
    }

    void reorderSimpleValuesClauses() {
        List<JoinNode> newRootNodes = new ArrayList<>();
        List<JoinNode> noJoinValuesNodes = new ArrayList<>();
        for (JoinNode rootNode : rootNodes) {
            if (isNoJoinValuesNode(rootNode)) {
                noJoinValuesNodes.add(rootNode);
            } else {
                newRootNodes.add(rootNode);
            }
        }

        newRootNodes.addAll(noJoinValuesNodes);
        rootNodes.clear();
        rootNodes.addAll(newRootNodes);
    }

    private static boolean isNoJoinValuesNode(JoinNode rootNode) {
        return rootNode.getValueCount() > 0 && rootNode.getNodes().isEmpty() && rootNode.getTreatedJoinNodes().isEmpty() && rootNode.getEntityJoinNodes().isEmpty();
    }

    Set<JoinNode> buildClause(StringBuilder sb, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean externalRepresenation, boolean ignoreCardinality,
                              List<String> whereConjuncts, List<String> syntheticSubqueryValuesWhereClauseConjuncts, Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities, Set<JoinNode> nodesToFetch, Set<JoinNode> alwaysIncludedNodes) {
        final boolean renderFetches = !clauseExclusions.contains(ClauseType.SELECT);
        StringBuilder tempSb = null;
        collectionJoinNodes.clear();
        renderedJoins.clear();
        sb.append(" FROM ");

        StringBuilder noJoinValuesNodesSb = new StringBuilder();
        // TODO: we might have dependencies to other from clause elements which should also be accounted for
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(", ");
            }

            JoinNode rootNode = nodes.get(i);
            JoinNode correlationParent = rootNode.getCorrelationParent();

            if (externalRepresenation && rootNode.getValueCount() > 0) {
                ManagedType<?> type = rootNode.getManagedType();
                if (type.getJavaType() == ValuesEntity.class) {
                    sb.append(rootNode.getValuesTypeName());
                } else {
                    if (type instanceof EntityType<?>) {
                        sb.append(((EntityType) type).getName());
                        if (rootNode.getValuesIdName() != null) {
                            sb.append('.').append(rootNode.getValuesIdName());
                        }
                    } else {
                        sb.append(rootNode.getValuesTypeName());
                    }
                }
                sb.append("(");
                sb.append(rootNode.getValueCount());
                sb.append(" VALUES)");
            } else if (externalRepresenation && explicitVersionEntities.get(rootNode.getJavaType()) != null) {
                DbmsModificationState state = explicitVersionEntities.get(rootNode.getJavaType()).get(rootNode.getAlias());
                EntityType<?> type = rootNode.getEntityType();
                if (state == DbmsModificationState.NEW) {
                    sb.append("NEW(");
                } else {
                    sb.append("OLD(");
                }
                sb.append(type.getName());
                sb.append(')');
            } else {
                if (correlationParent != null) {
                    renderCorrelationJoinPath(sb, correlationParent.getAliasInfo(), rootNode, whereConjuncts);
                } else {
                    EntityType<?> type = rootNode.getEntityType();
                    sb.append(type.getName());
                }
            }

            sb.append(' ');

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            sb.append(rootNode.getAliasInfo().getAlias());
            renderedJoins.add(rootNode);

            // TODO: not sure if needed since applyImplicitJoins will already invoke that
            rootNode.registerDependencies();

            JoinNode valuesNode = null;
            if (rootNode.getValueCount() > 0) {
                valuesNode = rootNode;
                // We add a synthetic where clause conjuncts for subqueries that are removed later to support the values clause in subqueries
                if (syntheticSubqueryValuesWhereClauseConjuncts != null) {
                    if (syntheticSubqueryValuesWhereClauseConjuncts.isEmpty()) {
                        syntheticSubqueryValuesWhereClauseConjuncts.add("1=1");
                    }
                    String exampleAttributeName = "value";
                    if (rootNode.getType() instanceof IdentifiableType<?>) {
                        exampleAttributeName = JpaMetamodelUtils.getSingleIdAttribute(rootNode.getEntityType()).getName();
                    }
                    syntheticSubqueryValuesWhereClauseConjuncts.add(rootNode.getAlias() + "." + exampleAttributeName + " IS NULL");
                }
            }
            if (!rootNode.getNodes().isEmpty()) {
                valuesNode = applyJoins(sb, rootNode.getAliasInfo(), rootNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, ignoreCardinality, nodesToFetch, whereConjuncts, valuesNode, alwaysIncludedNodes);
            }
            for (JoinNode treatedNode : rootNode.getTreatedJoinNodes().values()) {
                if (!treatedNode.getNodes().isEmpty()) {
                    valuesNode = applyJoins(sb, treatedNode.getAliasInfo(), treatedNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, ignoreCardinality, nodesToFetch, whereConjuncts, valuesNode, alwaysIncludedNodes);
                }
            }
            if (!rootNode.getEntityJoinNodes().isEmpty()) {
                // TODO: Fix this with #216
                boolean isCollection = true;
                if (mainQuery.jpaProvider.supportsEntityJoin() && !emulateJoins) {
                    valuesNode = applyJoins(sb, rootNode.getAliasInfo(), new ArrayList<>(rootNode.getEntityJoinNodes()), isCollection, clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, ignoreCardinality, nodesToFetch, whereConjuncts, valuesNode, alwaysIncludedNodes);
                } else {
                    Set<JoinNode> entityNodes = rootNode.getEntityJoinNodes();
                    for (JoinNode entityNode : entityNodes) {
                        if (entityNode.getJoinType() != JoinType.INNER) {
                            throw new IllegalArgumentException("Can't emulate outer join for entity join node: " + entityNode);
                        }

                        // Collect the join nodes referring to collections
                        if (collectCollectionJoinNodes && isCollection) {
                            // TODO: Maybe we can improve this and treat array access joins like non-collection join nodes
                            collectionJoinNodes.add(entityNode);
                        }

                        sb.append(", ");

                        EntityType<?> type = entityNode.getEntityType();
                        sb.append(type.getName());

                        sb.append(' ');

                        if (aliasPrefix != null) {
                            sb.append(aliasPrefix);
                        }

                        sb.append(entityNode.getAliasInfo().getAlias());

                        // TODO: not sure if needed since applyImplicitJoins will already invoke that
                        entityNode.registerDependencies();

                        if (entityNode.getOnPredicate() != null && !entityNode.getOnPredicate().getChildren().isEmpty()) {
                            if (tempSb == null) {
                                tempSb = new StringBuilder();
                            } else {
                                tempSb.setLength(0);
                            }

                            if (valuesNode != null) {
                                renderValuesClausePredicate(tempSb, valuesNode, valuesNode.getAlias());
                                whereConjuncts.add(tempSb.toString());
                                tempSb.setLength(0);
                                valuesNode = null;
                            }
                            queryGenerator.setClauseType(ClauseType.JOIN);
                            queryGenerator.setQueryBuffer(tempSb);
                            SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);
                            queryGenerator.generate(entityNode.getOnPredicate());
                            queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
                            queryGenerator.setClauseType(null);
                            whereConjuncts.add(tempSb.toString());
                        }

                        renderedJoins.add(entityNode);
                        if (!entityNode.getNodes().isEmpty()) {
                            valuesNode = applyJoins(sb, entityNode.getAliasInfo(), entityNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, ignoreCardinality, nodesToFetch, whereConjuncts, valuesNode, alwaysIncludedNodes);
                        }
                        for (JoinNode treatedNode : entityNode.getTreatedJoinNodes().values()) {
                            valuesNode = applyJoins(sb, treatedNode.getAliasInfo(), treatedNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, ignoreCardinality, nodesToFetch, whereConjuncts, valuesNode, alwaysIncludedNodes);
                        }
                    }
                }
            }

            if (valuesNode != null) {
                if (noJoinValuesNodesSb.length() != 0) {
                    noJoinValuesNodesSb.append(" AND ");
                }
                renderValuesClausePredicate(noJoinValuesNodesSb, valuesNode, valuesNode.getAlias());
            }
        }

        if (noJoinValuesNodesSb.length() != 0) {
            whereConjuncts.add(0, noJoinValuesNodesSb.toString());
        }

        return collectionJoinNodes;
    }

    void renderValuesClausePredicate(StringBuilder sb, JoinNode rootNode, String alias) {
        // The rendering strategy is to render the VALUES clause predicate into JPQL with the values parameters
        // in the correct order. The whole SQL part of that will be replaced later by the correct SQL
        int valueCount = rootNode.getValueCount();
        if (valueCount > 0) {
            String typeName = rootNode.getValuesTypeName() == null ? null : rootNode.getValuesTypeName().toUpperCase();
            String[] attributes = rootNode.getValuesAttributes();
            String prefix = rootNode.getAlias();

            for (int i = 0; i < valueCount; i++) {
                for (int j = 0; j < attributes.length; j++) {
                    if (typeName != null) {
                        sb.append("TREAT_");
                        sb.append(typeName);
                        sb.append('(');
                        sb.append(alias);
                        sb.append('.');
                        sb.append(attributes[j]);
                        sb.append(')');
                    } else {
                        sb.append(alias);
                        sb.append('.');
                        sb.append(attributes[j]);
                    }

                    sb.append(" = ");

                    sb.append(':');
                    sb.append(prefix);
                    sb.append('_');
                    sb.append(attributes[j]);
                    sb.append('_').append(i);
                    sb.append(" OR ");
                }
            }

            sb.setLength(sb.length() - " OR ".length());
        }
    }

    void verifyBuilderEnded() {
        joinOnBuilderListener.verifyBuilderEnded();
    }

    void acceptVisitor(JoinNodeVisitor v) {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            nodes.get(i).accept(v);
        }
    }

    void setEmulateJoins(boolean emulateJoins) {
        this.emulateJoins = emulateJoins;
    }

    public boolean acceptVisitor(Expression.ResultVisitor<Boolean> aggregateDetector, boolean stopValue) {
        Boolean stop = Boolean.valueOf(stopValue);

        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            if (stop.equals(nodes.get(i).accept(new AbortableOnClauseJoinNodeVisitor(aggregateDetector, stopValue)))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void apply(ExpressionModifierVisitor<? super ExpressionModifier> visitor) {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            nodes.get(i).accept(visitor);
        }
    }

    private JoinNode renderJoinNode(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node, String aliasPrefix, boolean renderFetches, Set<JoinNode> nodesToFetch, List<String> whereConjuncts, JoinNode valuesNode) {
        if (!renderedJoins.contains(node)) {
            // We determine the nodes that should be fetched by analyzing the fetch owners during implicit joining
            final boolean fetch = nodesToFetch.contains(node) && renderFetches;
            // Don't render key joins unless fetching is specified on it
            if (node.isQualifiedJoin() && !fetch) {
                renderedJoins.add(node);
                return valuesNode;
            }
            // We only render treat joins, but not treated join nodes. These treats are just "optional casts" that don't affect joining
            if (node.isTreatedJoinNode()) {
                renderedJoins.add(node);
                return valuesNode;
            }
            switch (node.getJoinType()) {
                case INNER:
                    sb.append(" JOIN ");
                    break;
                case LEFT:
                    sb.append(" LEFT JOIN ");
                    break;
                case RIGHT:
                    sb.append(" RIGHT JOIN ");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown join type: " + node.getJoinType());
            }

            if (fetch) {
                sb.append("FETCH ");
            }

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            String onCondition = renderJoinPath(sb, joinBase, node, whereConjuncts);
            sb.append(' ');

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            sb.append(node.getAliasInfo().getAlias());
            renderedJoins.add(node);
            boolean onClause = valuesNode != null || node.getOnPredicate() != null && !node.getOnPredicate().getChildren().isEmpty() || onCondition != null;

            if (onClause) {
                sb.append(joinRestrictionKeyword);

                // Always render the ON condition in parenthesis to workaround an EclipseLink bug in entity join parsing
                sb.append('(');
            }

            // This condition will be removed in the final SQL, so no worries about it
            // It is just there to have parameters at the right position in the final SQL
            if (valuesNode != null) {
                renderValuesClausePredicate(sb, valuesNode, valuesNode.getAlias());
                sb.append(" AND ");
                valuesNode = null;
            }

            if (node.getOnPredicate() != null && !node.getOnPredicate().getChildren().isEmpty()) {
                if (onCondition != null) {
                    sb.append(onCondition).append(" AND ");
                }

                queryGenerator.setClauseType(ClauseType.JOIN);
                queryGenerator.setQueryBuffer(sb);
                SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);
                queryGenerator.setRenderedJoinNodes(renderedJoins);
                queryGenerator.generate(node.getOnPredicate());
                queryGenerator.setRenderedJoinNodes(null);
                queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
                queryGenerator.setClauseType(null);
            } else if (onCondition != null) {
                sb.append(onCondition);
            }

            if (onClause) {
                sb.append(')');
            }
        }
        return valuesNode;
    }

    private void renderCorrelationJoinPath(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node, List<String> whereConjuncts) {
        final boolean renderTreat = mainQuery.jpaProvider.supportsTreatJoin() &&
                (!mainQuery.jpaProvider.supportsSubtypeRelationResolving() || node.getJoinType() == JoinType.INNER);
        if (!mainQuery.jpaProvider.supportsJoinElementCollectionsOnCorrelatedInverseAssociations() && node.hasElementCollectionJoins() || node.getTreatType() != null && !renderTreat && !mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
            ExtendedManagedType extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, node.getCorrelationParent().getManagedType());
            ExtendedAttribute attribute = extendedManagedType.getAttribute(node.getCorrelationPath());
            if (attribute.getMappedBy() != null) {
                sb.append(node.getEntityType().getName());
                StringBuilder whereSb = new StringBuilder();
                node.appendAlias(whereSb, false);
                whereSb.append('.').append(attribute.getMappedBy());
                boolean singleValuedAssociationId = mainQuery.jpaProvider.supportsSingleValuedAssociationIdExpressions() && extendedManagedType.getIdAttributes().size() == 1;
                if (singleValuedAssociationId) {
                    whereSb.append('.').append(extendedManagedType.getIdAttribute().getName());
                }
                whereSb.append(" = ");
                node.getCorrelationParent().appendAlias(whereSb, false);
                if (singleValuedAssociationId) {
                    whereSb.append('.').append(extendedManagedType.getIdAttribute().getName());
                }
                whereConjuncts.add(whereSb.toString());
                return;
            }
        }
        if (node.getTreatType() != null) {
            if (renderTreat) {
                sb.append("TREAT(");
                renderAlias(sb, joinBase.getJoinNode(), mainQuery.jpaProvider.supportsRootTreat());
                sb.append('.');
                sb.append(node.getCorrelationPath());
                sb.append(" AS ");
                sb.append(node.getTreatType().getName());
                sb.append(')');
            } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                sb.append(joinBase.getAlias()).append('.').append(node.getCorrelationPath());
            } else {
                throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
            }
        } else {
            JoinNode baseNode = joinBase.getJoinNode();
            if (baseNode.getTreatType() != null) {
                if (mainQuery.jpaProvider.supportsRootTreatJoin()) {
                    baseNode.appendAlias(sb, true);
                } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                    baseNode.appendAlias(sb, false);
                } else {
                    throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
                }
            } else {
                baseNode.appendAlias(sb, false);
            }

            sb.append('.').append(node.getCorrelationPath());
        }
    }

    private String renderJoinPath(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node, List<String> whereConjuncts) {
        if (node.getTreatType() != null) {
            // We render the treat join only if it makes sense. If we have e.g. a left join and the provider supports
            // implicit relation resolving then there is no point in rendering the treat join. On the contrary, that might lead to wrong results
            final boolean renderTreat = mainQuery.jpaProvider.supportsTreatJoin() &&
                    (!mainQuery.jpaProvider.supportsSubtypeRelationResolving() || node.getJoinType() == JoinType.INNER);
            final String onCondition;
            final JoinNode baseNode = joinBase.getJoinNode();
            final String treatType = node.getTreatType().getName();
            final String relationName = node.getParentTreeNode().getRelationName();
            JpaProvider.ConstraintType constraintType = mainQuery.jpaProvider.requiresTreatFilter(baseNode.getEntityType(), relationName, node.getJoinType());
            if (constraintType != JpaProvider.ConstraintType.NONE) {
                String constraint = "TYPE(" + node.getAlias() + ") = " + treatType;
                if (constraintType == JpaProvider.ConstraintType.WHERE) {
                    whereConjuncts.add(constraint);
                    onCondition = null;
                } else {
                    onCondition = constraint;
                }
            } else {
                onCondition = null;
            }
            if (renderTreat) {
                sb.append("TREAT(");
                renderAlias(sb, baseNode, mainQuery.jpaProvider.supportsRootTreatTreatJoin());
                sb.append('.');
                sb.append(relationName);
                sb.append(" AS ");
                sb.append(treatType);
                sb.append(')');
            } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                sb.append(joinBase.getAlias()).append('.').append(node.getParentTreeNode().getRelationName());
            } else {
                throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
            }
            return onCondition;
        } else if (node.getCorrelationPath() == null && node.getAliasInfo().isRootNode()) {
            sb.append(node.getEntityType().getName());
        } else if (node.isQualifiedJoin()) {
            sb.append(node.getQualificationExpression());
            sb.append('(');
            sb.append(joinBase.getJoinNode().getAlias());
            sb.append(')');
        } else {
            renderAlias(sb, joinBase.getJoinNode(), mainQuery.jpaProvider.supportsRootTreatJoin());
            sb.append('.').append(node.getParentTreeNode().getRelationName());
        }

        return null;
    }

    private void renderAlias(StringBuilder sb, JoinNode baseNode, boolean supportsTreat) {
        if (baseNode.getTreatType() != null) {
            if (supportsTreat) {
                baseNode.appendAlias(sb, true);
            } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                baseNode.appendAlias(sb, false);
            } else {
                throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
            }
        } else {
            baseNode.appendAlias(sb, false);
        }
    }

    private JoinNode renderReverseDependency(StringBuilder sb, JoinNode dependency, String aliasPrefix, boolean renderFetches, Set<JoinNode> nodesToFetch, List<String> whereConjuncts, JoinNode valuesNode) {
        if (dependency.getParent() != null) {
            if (dependency.getParent() != valuesNode) {
                valuesNode = renderReverseDependency(sb, dependency.getParent(), aliasPrefix, renderFetches, nodesToFetch, whereConjuncts, valuesNode);
            }
            if (!dependency.getDependencies().isEmpty()) {
                markedJoinNodes.add(dependency);
                try {
                    for (JoinNode dep : dependency.getDependencies()) {
                        if (markedJoinNodes.contains(dep)) {
                            StringBuilder errorSb = new StringBuilder();
                            errorSb.append("Cyclic join dependency between nodes: [");
                            for (JoinNode seenNode : markedJoinNodes) {
                                errorSb.append(seenNode.getAliasInfo().getAlias());
                                if (seenNode.getAliasInfo().isImplicit()) {
                                    errorSb.append('(').append(seenNode.getAliasInfo().getAbsolutePath()).append(')');
                                }
                                errorSb.append(", ");
                            }
                            errorSb.setLength(errorSb.length() - 2);
                            errorSb.append(']');

                            throw new IllegalStateException(errorSb.toString());
                        }
                        // render reverse dependencies
                        valuesNode = renderReverseDependency(sb, dep, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts, valuesNode);
                    }
                } finally {
                    markedJoinNodes.remove(dependency);
                }
            }
            valuesNode = renderJoinNode(sb, dependency.getParent().getAliasInfo(), dependency, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts, valuesNode);
        }

        return valuesNode;
    }

    private JoinNode applyJoins(StringBuilder sb, JoinAliasInfo joinBase, Map<String, JoinTreeNode> nodes, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean renderFetches, boolean ignoreCardinality, Set<JoinNode> nodesToFetch, List<String> whereConjuncts, JoinNode valuesNode, Set<JoinNode> alwaysIncludedNodes) {
        for (Map.Entry<String, JoinTreeNode> nodeEntry : nodes.entrySet()) {
            JoinTreeNode treeNode = nodeEntry.getValue();
            List<JoinNode> stack = new ArrayList<JoinNode>();
            stack.addAll(treeNode.getJoinNodes().descendingMap().values());

            valuesNode = applyJoins(sb, joinBase, stack, treeNode.isCollection(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, ignoreCardinality, nodesToFetch, whereConjuncts, valuesNode, alwaysIncludedNodes);
        }
        return valuesNode;
    }

    private JoinNode applyJoins(StringBuilder sb, JoinAliasInfo joinBase, List<JoinNode> stack, boolean isCollection, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean renderFetches, boolean ignoreCardinality, Set<JoinNode> nodesToFetch, List<String> whereConjuncts, JoinNode valuesNode, Set<JoinNode> alwaysIncludedNodes) {
        while (!stack.isEmpty()) {
            JoinNode node = stack.remove(stack.size() - 1);
            // If the clauses in which a join node occurs are all excluded or the join node is not mandatory for the cardinality, we skip it
            if (!clauseExclusions.isEmpty() && clauseExclusions.containsAll(node.getClauseDependencies()) && (ignoreCardinality || !node.isCardinalityMandatory()) && !alwaysIncludedNodes.contains(node)) {
                continue;
            }

            stack.addAll(node.getEntityJoinNodes());
            stack.addAll(node.getTreatedJoinNodes().values());

            // We have to render any dependencies this join node has before actually rendering itself
            if (!node.getDependencies().isEmpty()) {
                valuesNode = renderReverseDependency(sb, node, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts, valuesNode);
            }

            // Collect the join nodes referring to collections
            if (collectCollectionJoinNodes && isCollection && !node.hasArrayExpressionPredicate()) {
                // TODO: Maybe we can improve this and treat array access joins like non-collection join nodes
                collectionJoinNodes.add(node);
            }

            // Finally render this join node
            valuesNode = renderJoinNode(sb, joinBase, node, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts, valuesNode);

            // Render child nodes recursively
            if (!node.getNodes().isEmpty()) {
                valuesNode = applyJoins(sb, node.getAliasInfo(), node.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, ignoreCardinality, nodesToFetch, whereConjuncts, valuesNode, alwaysIncludedNodes);
            }
        }
        return valuesNode;
    }

    private boolean isExternal(PathExpression path) {
        PathElementExpression firstElem = path.getExpressions().get(0);
        return isExternal(path, firstElem);
    }

    private boolean isExternal(TreatExpression treatExpression) {
        Expression expression = treatExpression.getExpression();

        if (expression instanceof PathExpression) {
            PathExpression path = (PathExpression) expression;
            PathElementExpression firstElem = path.getExpressions().get(0);
            return isExternal(path, firstElem);
        } else if (expression instanceof FunctionExpression) {
            // Can only be key or value function
            PathExpression path = (PathExpression) ((FunctionExpression) expression).getExpressions().get(0);
            PathElementExpression firstElem = path.getExpressions().get(0);
            return isExternal(path, firstElem);
        } else {
            throw new IllegalArgumentException("Unexpected expression type[" + expression.getClass().getSimpleName() + "] in treat expression: " + treatExpression);
        }
    }

    private boolean isExternal(PathExpression path, PathElementExpression firstElem) {
        String startAlias;
        if (firstElem instanceof ArrayExpression) {
            startAlias = ((ArrayExpression) firstElem).getBase().toString();
        } else if (firstElem instanceof TreatExpression) {
            Expression treatedExpression = ((TreatExpression) firstElem).getExpression();

            if (treatedExpression instanceof PathExpression) {
                treatedExpression = ((PathExpression) treatedExpression).getExpressions().get(0);
            }

            if (treatedExpression instanceof ArrayExpression) {
                startAlias = ((ArrayExpression) treatedExpression).getBase().toString();
            } else if (treatedExpression instanceof TreatExpression) {
                startAlias = ((TreatExpression) treatedExpression).getExpression().toString();
            } else {
                startAlias = treatedExpression.toString();
            }
        } else {
            startAlias = firstElem.toString();
        }

        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return false;
        }

        if (parent != null && aliasInfo.getAliasOwner() != aliasManager) {
            // the alias exists but originates from the parent query builder

            // an external select alias must not be de-referenced
            if (path.getExpressions().size() > 1) {
                // But if check if the expression really is just an alias reference or the
                if (aliasInfo instanceof SelectInfo) {
                    throw new ExternalAliasDereferencingException("Start alias [" + startAlias + "] of path [" + path.toString()
                            + "] is external and must not be dereferenced");
                }
            }

            // the alias is external so we do not have to treat it
            return true;
        } else if (aliasInfo.getAliasOwner() == aliasManager) {
            // the alias originates from the current query builder and is therefore not external
            return false;
        } else {
            throw new IllegalStateException("Alias [" + aliasInfo.getAlias() + "] originates from an unknown query");
        }
    }

    public boolean isJoinableSelectAlias(PathExpression pathExpr, boolean fromSelect, boolean fromSubquery) {
        return getJoinableSelectAlias(pathExpr, fromSelect, fromSubquery) != null;
    }

    public Expression getJoinableSelectAlias(PathExpression pathExpr, boolean fromSelect, boolean fromSubquery) {
        // We can skip this check if the first element is not a simple property
        if (!(pathExpr.getExpressions().get(0) instanceof PropertyExpression)) {
            return null;
        }

        boolean singlePathElement = pathExpr.getExpressions().size() == 1;
        String startAlias = pathExpr.getExpressions().get(0).toString();

        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return null;
        }

        if (aliasInfo instanceof SelectInfo && !fromSelect && !fromSubquery) {
            // select alias
            if (!singlePathElement) {
                throw new IllegalStateException("Path starting with select alias not allowed");
            }

            // might be joinable
            Expression expression = ((SelectInfo) aliasInfo).getExpression();
            // If the expression the alias refers to and the expression are the same, we are resolving an ambiguous alias expression
            if (expression == pathExpr) {
                return null;
            }

            return expression;
        }

        return null;
    }

    <X> JoinOnBuilder<X> joinOn(X result, String base, Class<?> clazz, String alias, JoinType type) {
        return joinOn(result, base, metamodel.entity(clazz), alias, type);
    }

    <X> JoinOnBuilder<X> joinOn(X result, String base, EntityType<?> entityType, String alias, JoinType type) {
        PathExpression basePath = expressionFactory.createPathExpression(base);

        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty alias!");
        }
        if (type != JoinType.INNER && !mainQuery.jpaProvider.supportsEntityJoin()) {
            throw new IllegalArgumentException("The JPA provider does not support entity joins and an emulation for non-inner entity joins is not implemented!");
        }

        List<PathElementExpression> propertyExpressions = basePath.getExpressions();
        JoinNode baseNode;
        if (propertyExpressions.size() > 1) {
            AliasInfo aliasInfo = aliasManager.getAliasInfo(propertyExpressions.get(0).toString());

            if (aliasInfo == null || !(aliasInfo instanceof JoinAliasInfo)) {
                throw new IllegalArgumentException("The base '" + base + "' is not a valid join alias!");
            }

            baseNode = ((JoinAliasInfo) aliasInfo).getJoinNode();
            for (int i = 1; i < propertyExpressions.size(); i++) {
                String relationName = propertyExpressions.get(i).toString();
                JoinTreeNode treeNode = baseNode.getNodes().get(relationName);
                if (treeNode == null) {
                    break;
                }
                baseNode = treeNode.getDefaultNode();
                if (baseNode == null) {
                    break;
                }
            }
            if (baseNode == null) {
                throw new IllegalArgumentException("The base '" + base + "' is not a valid join alias!");
            }
        } else {
            AliasInfo aliasInfo = aliasManager.getAliasInfo(base);

            if (aliasInfo == null || !(aliasInfo instanceof JoinAliasInfo)) {
                throw new IllegalArgumentException("The base '" + base + "' is not a valid join alias!");
            }
            baseNode = ((JoinAliasInfo) aliasInfo).getJoinNode();
        }

        JoinAliasInfo joinAliasInfo = new JoinAliasInfo(alias, null, false, true, aliasManager);
        JoinNode entityJoinNode = JoinNode.createEntityJoinNode(baseNode, type, entityType, joinAliasInfo);
        joinAliasInfo.setJoinNode(entityJoinNode);
        baseNode.addEntityJoin(entityJoinNode);
        aliasManager.registerAliasInfo(joinAliasInfo);

        joinOnBuilderListener.joinNode = entityJoinNode;
        return joinOnBuilderListener.startBuilder(new JoinOnBuilderImpl<X>(result, joinOnBuilderListener, parameterManager, expressionFactory, subqueryInitFactory));
    }

    <X> JoinOnBuilder<X> joinOn(X result, String path, String alias, JoinType type, boolean defaultJoin) {
        joinOnBuilderListener.joinNode = join(path, alias, type, false, defaultJoin);
        return joinOnBuilderListener.startBuilder(new JoinOnBuilderImpl<X>(result, joinOnBuilderListener, parameterManager, expressionFactory, subqueryInitFactory));
    }

    JoinNode join(String path, String alias, JoinType type, boolean fetch, boolean defaultJoin) {
        Expression expr = expressionFactory.createJoinPathExpression(path);
        PathElementExpression elementExpr;
        String treatType = null;
        JoinResult result;
        JoinNode current;
        if (expr instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expr;

            if (isExternal(pathExpression) || isJoinableSelectAlias(pathExpression, false, false)) {
                throw new IllegalArgumentException("No external path or select alias allowed in join path");
            }

            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            elementExpr = pathElements.get(pathElements.size() - 1);
            result = implicitJoin(null, pathExpression, null, null, 0, pathElements.size() - 1, false);
            current = result.baseNode;
        } else if (expr instanceof TreatExpression) {
            TreatExpression treatExpression = (TreatExpression) expr;

            if (isExternal(treatExpression)) {
                throw new IllegalArgumentException("No external path or select alias allowed in join path");
            }

            Expression expression = treatExpression.getExpression();

            if (expression instanceof PathExpression) {
                PathExpression pathExpression = (PathExpression) expression;
                List<PathElementExpression> pathElements = pathExpression.getExpressions();
                elementExpr = pathElements.get(pathElements.size() - 1);
                result = implicitJoin(null, pathExpression, null, null, 0, pathElements.size() - 1, false);
                current = result.baseNode;
                treatType = treatExpression.getType();
            } else {
                throw new IllegalArgumentException("Unexpected expression type[" + expression.getClass().getSimpleName() + "] in treat expression: " + treatExpression);
            }
        } else {
            throw new IllegalArgumentException("Join path [" + path + "] is not a path");
        }

        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        } else if (elementExpr instanceof MapKeyExpression) {
            MapKeyExpression mapKeyExpression = (MapKeyExpression) elementExpr;
            boolean fromSubquery = false;
            boolean fromSelectAlias = false;
            boolean joinRequired = true;
            current = joinMapKey(mapKeyExpression, alias, null, null, fromSubquery, fromSelectAlias, joinRequired, fetch, false, defaultJoin);
            result = new JoinResult(current, null, current.getNodeType());
        } else {
            List<String> joinRelationAttributes = result.addToList(new ArrayList<String>());
            joinRelationAttributes.add(elementExpr.toString());
            current = current == null ? getRootNodeOrFail("Could not join path [", path, "] because it did not use an absolute path but multiple root nodes are available!") : current;
            result = createOrUpdateNode(current, joinRelationAttributes, treatType, alias, type, false, defaultJoin);
        }

        if (fetch) {
            fetchPath(result.baseNode);
        }

        return result.baseNode;
    }

    public void implicitJoin(Expression expression, boolean objectLeafAllowed, String targetType, ClauseType fromClause, String selectAlias, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean idRemovable) {
        implicitJoin(expression, objectLeafAllowed, targetType, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, idRemovable, false);
    }

    @SuppressWarnings("checkstyle:methodlength")
    public void implicitJoin(Expression expression, boolean objectLeafAllowed, String targetTypeName, ClauseType fromClause, String selectAlias, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean idRemovable, boolean fetch) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;

            Expression aliasedExpression;
            // If joinable select alias, it is guaranteed to have only a single element
            if ((aliasedExpression = getJoinableSelectAlias(pathExpression, fromClause == ClauseType.SELECT, fromSubquery)) != null) {
                // this check is necessary to prevent infinite recursion in the case of e.g. SELECT name AS name
                if (!fromSelectAlias) {
                    // we have to do this implicit join because we might have to adjust the selectOnly flag in the referenced join nodes
                    implicitJoin(aliasedExpression, true, null, fromClause, selectAlias, fromSubquery, true, joinRequired, false);
                }
                return;
            } else if (isExternal(pathExpression)) {
                // try to set base node and field for the external expression based
                // on existing joins in the super query
                parent.implicitJoin(pathExpression, true, targetTypeName, fromClause, selectAlias, true, fromSelectAlias, joinRequired, false);
                return;
            }

            // First try to implicit join indices of array expressions since we will need their base nodes
            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            int pathElementSize = pathElements.size();
            for (int i = 0; i < pathElementSize; i++) {
                PathElementExpression pathElem = pathElements.get(i);
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, false);
                }
            }

            PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);
            boolean singleValuedAssociationIdExpression = false;
            JoinNode current = null;
            List<String> resultFields = new ArrayList<String>();
            JoinResult currentResult;

            JoinNode possibleRoot;
            int startIndex = 0;

            // Skip root speculation if this is just a single element path
            if (pathElements.size() > 1 && (possibleRoot = getRootNode(pathElements.get(0))) != null) {
                startIndex = 1;
                current = possibleRoot;
            }

            if (pathElements.size() > startIndex + 1) {
                int maybeSingularAssociationIndex = pathElements.size() - 2;
                int maybeSingularAssociationIdIndex = pathElements.size() - 1;
                currentResult = implicitJoin(current, pathExpression, fromClause, selectAlias, startIndex, maybeSingularAssociationIndex, false);
                current = currentResult.baseNode;
                resultFields = currentResult.addToList(resultFields);

                singleValuedAssociationIdExpression = isSingleValuedAssociationId(currentResult, pathElements, idRemovable);

                if (singleValuedAssociationIdExpression) {
                    if (!mainQuery.jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
                        if (idRemovable) {
                            // remove the id part only if we come from a predicate
                            elementExpr = null;
                            if (current == null) {
                                // This is the case when we use a join alias like "alias.id"
                                // We need to resolve the base since it might not be the root node
                                AliasInfo a = aliasManager.getAliasInfo(pathElements.get(maybeSingularAssociationIndex).toString());
                                // We know this can only be a join node alias
                                current = ((JoinAliasInfo) a).getJoinNode();
                                resultFields = Collections.emptyList();
                            }
                        } else {
                            // Need a normal join
                            currentResult = implicitJoin(current, pathExpression, fromClause, selectAlias, maybeSingularAssociationIndex, pathElements.size() - 1, false);
                            current = currentResult.baseNode;
                            resultFields = currentResult.addToList(resultFields);
                            singleValuedAssociationIdExpression = false;
                        }
                    }
                } else {
                    if (currentResult.hasField()) {
                        // currentResult.typeName?
                        // Redo the joins for embeddables by moving the start index back
                        currentResult = implicitJoin(current, pathExpression, fromClause, selectAlias, maybeSingularAssociationIndex - currentResult.fields.size(), maybeSingularAssociationIdIndex, false);
                        if (currentResult.fields != resultFields) {
                            resultFields.clear();
                        }
                    } else {
                        currentResult = implicitJoin(current, pathExpression, fromClause, selectAlias, maybeSingularAssociationIndex, maybeSingularAssociationIdIndex, false);
                    }

                    current = currentResult.baseNode;
                    resultFields = currentResult.addToList(resultFields);
                }
            } else {
                // Single element expression like "alias", "relation", "property" or "alias.relation"
                currentResult = implicitJoin(current, pathExpression, fromClause, selectAlias, startIndex, pathElements.size() - 1, false);
                current = currentResult.baseNode;
                resultFields = currentResult.addToList(resultFields);

                if (idRemovable) {
                    if (current != null) {
                        // If there is a "base node" i.e. a current, the expression has 2 elements
                        if (isId(current.getNodeType(), elementExpr)) {
                            // We remove the "id" part
                            elementExpr = null;
                            // Treat it like a single valued association id expression
                            singleValuedAssociationIdExpression = true;
                        }
                    } else {
                        // There is no base node, this is a expression with 1 element
                        // Either relative or a direct alias
                        String elementExpressionString;
                        if (elementExpr instanceof ArrayExpression) {
                            elementExpressionString = ((ArrayExpression) elementExpr).getBase().toString();
                        } else {
                            elementExpressionString = elementExpr.toString();
                        }
                        AliasInfo a = aliasManager.getAliasInfo(elementExpressionString);
                        if (a == null) {
                            // If the element expression is an alias, there is nothing to replace
                            current = getRootNodeOrFail("Could not join path [", expression, "] because it did not use an absolute path but multiple root nodes are available!");
                            if (isId(current.getNodeType(), elementExpr)) {
                                // We replace the "id" part with the alias
                                elementExpr = new PropertyExpression(current.getAlias());
                            }
                        }
                    }
                }
            }

            JoinResult result;
            AliasInfo aliasInfo;

            // The case of a simple join alias usage
            if (pathElements.size() == 1 && !fromSelectAlias
                    && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null
                    && (selectAlias == null || !selectAlias.equals(elementExpr.toString()))) {
                // No need to assert the resultFields here since they can't appear anyways if we enter this branch
                if (aliasInfo instanceof SelectInfo) {
                    if (targetTypeName != null) {
                        throw new IllegalArgumentException("The select alias '" + aliasInfo.getAlias()
                                + "' can not be used for a treat expression!.");
                    }

                    // We actually allow usage of select aliases in expressions, but JPA doesn't, so we have to resolve them here
                    Expression selectExpr = ((SelectInfo) aliasInfo).getExpression();

                    if (!(selectExpr instanceof PathExpression)) {
                        throw new RuntimeException("The select expression '" + selectExpr.toString()
                                + "' is not a simple path expression! No idea how to implicit join that.");
                    }
                    // join the expression behind a select alias once when it is encountered the first time
                    if (((PathExpression) selectExpr).getBaseNode() == null) {
                        implicitJoin(selectExpr, objectLeafAllowed, null, fromClause, selectAlias, fromSubquery, true, joinRequired, false);
                    }
                    PathExpression selectPathExpr = (PathExpression) selectExpr;
                    PathReference reference = selectPathExpr.getPathReference();
                    result = new JoinResult((JoinNode) selectPathExpr.getBaseNode(), Arrays.asList(selectPathExpr.getField()), reference.getType());
                } else {
                    JoinNode pathJoinNode = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    if (targetTypeName != null) {
                        // Treated root path
                        ManagedType<?> targetType = metamodel.managedType(targetTypeName);
                        result = new JoinResult(pathJoinNode, null, targetType);
                    } else {
                        // Naked join alias usage like in "KEY(joinAlias)"
                        result = new JoinResult(pathJoinNode, null, pathJoinNode.getNodeType());
                    }
                }
            } else if (pathElements.size() == 1 && elementExpr instanceof QualifiedExpression) {
                QualifiedExpression qualifiedExpression = (QualifiedExpression) elementExpr;
                JoinNode baseNode;
                if (elementExpr instanceof MapKeyExpression) {
                    baseNode = joinMapKey((MapKeyExpression) elementExpr, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, true, fetch, true, true);
                } else if (elementExpr instanceof ListIndexExpression) {
                    baseNode = joinListIndex((ListIndexExpression) elementExpr, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, true, fetch, true, true);
                } else if (elementExpr instanceof MapEntryExpression) {
                    baseNode = joinMapEntry((MapEntryExpression) elementExpr, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, true, fetch, true, true);
                } else if (elementExpr instanceof MapValueExpression) {
                    implicitJoin(qualifiedExpression.getPath(), objectLeafAllowed, targetTypeName, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
                    baseNode = (JoinNode) qualifiedExpression.getPath().getBaseNode();
                } else {
                    throw new IllegalArgumentException("Unknown qualified expression type: " + elementExpr);
                }

                result = new JoinResult(baseNode, null, baseNode.getNodeType());
            } else {
                // current might be null
                if (current == null) {
                    current = getRootNodeOrFail("Could not join path [", expression, "] because it did not use an absolute path but multiple root nodes are available!");
                }

                if (singleValuedAssociationIdExpression) {
                    String associationName = pathElements.get(pathElements.size() - 2).toString();
                    AliasInfo singleValuedAssociationRootAliasInfo = null;
                    JoinTreeNode treeNode;

                    if (currentResult.hasField()) {
                        associationName = currentResult.joinFields(associationName);
                    } else if (pathElements.size() == 2) {
                        // If this path is composed of only two elements, the association name could represent an alias
                        singleValuedAssociationRootAliasInfo = aliasManager.getAliasInfoForBottomLevel(associationName);
                    }

                    if (singleValuedAssociationRootAliasInfo != null) {
                        JoinNode singleValuedAssociationRoot = ((JoinAliasInfo) singleValuedAssociationRootAliasInfo).getJoinNode();
                        if (elementExpr != null) {
                            AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                    metamodel,
                                    singleValuedAssociationRoot.getNodeType(),
                                    elementExpr,
                                    singleValuedAssociationRoot.getAlias()
                            );
                            Type<?> type = attributeHolder.getAttributeType();
                            result = new JoinResult(singleValuedAssociationRoot, Arrays.asList(elementExpr.toString()), type);
                        } else {
                            result = new JoinResult(singleValuedAssociationRoot, null, singleValuedAssociationRoot.getNodeType());
                        }
                    } else {
                        boolean reusExistingNode = false;
                        treeNode = current.getNodes().get(associationName);

                        if (reusExistingNode && treeNode != null && treeNode.getDefaultNode() != null) {
                            if (elementExpr != null) {
                                AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                        metamodel,
                                        treeNode.getDefaultNode().getNodeType(),
                                        elementExpr,
                                        treeNode.getDefaultNode().getAlias()
                                );
                                Type<?> type = attributeHolder.getAttributeType();
                                result = new JoinResult(treeNode.getDefaultNode(), Arrays.asList(elementExpr.toString()), type);
                            } else {
                                result = new JoinResult(treeNode.getDefaultNode(), null, treeNode.getDefaultNode().getNodeType());
                            }
                        } else {
                            if (elementExpr != null) {
                                String elementString = elementExpr.toString();
                                Expression resultExpr = expressionFactory.createSimpleExpression(associationName + '.' + elementString, false);
                                AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                        metamodel,
                                        current.getNodeType(),
                                        resultExpr,
                                        current.getAlias()
                                );
                                Type<?> type = attributeHolder.getAttributeType();
                                result = new JoinResult(current, Arrays.asList(associationName, elementString), type);
                            } else {
                                Expression resultExpr = expressionFactory.createSimpleExpression(associationName, false);
                                AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                        metamodel,
                                        current.getNodeType(),
                                        resultExpr,
                                        current.getAlias()
                                );
                                Type<?> type = attributeHolder.getAttributeType();
                                result = new JoinResult(current, Arrays.asList(associationName), type);
                            }
                        }
                    }
                } else if (elementExpr instanceof ArrayExpression) {
                    // Element collection case
                    ArrayExpression arrayExpr = (ArrayExpression) elementExpr;
                    String joinRelationName = arrayExpr.getBase().toString();

                    // Find a node by a predicate match
                    JoinNode matchingNode;

                    if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(joinRelationName)) != null) {
                        // The first node is allowed to be a join alias
                        if (aliasInfo instanceof SelectInfo) {
                            throw new IllegalArgumentException("Illegal reference to the select alias '" + joinRelationName + "'");
                        }
                        current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                        generateAndApplyOnPredicate(current, arrayExpr);
                    } else if ((matchingNode = findNode(current, joinRelationName, arrayExpr)) != null) {
                        // We found a join node for the same join relation with the same array expression predicate
                        current = matchingNode;
                    } else {
                        String joinAlias = getJoinAlias(arrayExpr);
                        resultFields.add(joinRelationName);
                        currentResult = createOrUpdateNode(current, resultFields, null, joinAlias, null, true, false);
                        current = currentResult.baseNode;
                        // TODO: Not sure if necessary
                        if (currentResult.hasField()) {
                            throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part [" + currentResult.joinFields()
                                    + "]");
                        }
                        generateAndApplyOnPredicate(current, arrayExpr);
                    }

                    result = new JoinResult(current, null, current.getNodeType());
                } else if (!pathExpression.isUsedInCollectionFunction()) {
                    if (resultFields.isEmpty()) {
                        result = implicitJoinSingle(current, elementExpr.toString(), objectLeafAllowed, joinRequired);
                    } else {
                        resultFields.add(elementExpr.toString());

                        String attributeName = StringUtils.join(".", resultFields);
                        // Validates and gets the path type
                        getPathType(current.getNodeType(), attributeName, pathExpression);

                        result = implicitJoinSingle(current, attributeName, objectLeafAllowed, joinRequired);
                    }
                } else {
                    if (resultFields.isEmpty()) {
                        String attributeName = elementExpr.toString();
                        Type<?> type = getPathType(current.getNodeType(), attributeName, pathExpression);
                        result = new JoinResult(current, Arrays.asList(attributeName), type);
                    } else {
                        resultFields.add(elementExpr.toString());

                        String attributeName = StringUtils.join(".", resultFields);
                        Type<?> type = getPathType(current.getNodeType(), attributeName, pathExpression);

                        result = new JoinResult(current, resultFields, type);
                    }
                }
            }

            if (fetch) {
                fetchPath(result.baseNode);
            }

            // Don't forget to update the clause dependencies, but only for normal attribute accesses, that way paginated queries can prevent joins in certain cases
            if (fromClause != null) {
                try {
                    result.baseNode.updateClauseDependencies(fromClause, false, new LinkedHashSet<JoinNode>());
                } catch (IllegalStateException ex) {
                    throw new IllegalArgumentException("Implicit join in expression '" + expression + "' introduces cyclic join dependency!", ex);
                }
            }

            if (result.isLazy()) {
                pathExpression.setPathReference(new LazyPathReference(result.baseNode, result.joinFields(), result.type));
            } else {
                pathExpression.setPathReference(new SimplePathReference(result.baseNode, result.joinFields(), result.type));
            }
        } else if (expression instanceof FunctionExpression) {
            List<Expression> expressions = ((FunctionExpression) expression).getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                implicitJoin(expressions.get(i), objectLeafAllowed, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, false);
            }
        } else if (expression instanceof MapKeyExpression) {
            MapKeyExpression mapKeyExpression = (MapKeyExpression) expression;
            joinMapKey(mapKeyExpression, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, fetch, true, true);
        } else if (expression instanceof QualifiedExpression) {
            implicitJoin(((QualifiedExpression) expression).getPath(), objectLeafAllowed, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, false);
        } else if (expression instanceof ArrayExpression || expression instanceof GeneralCaseExpression || expression instanceof TreatExpression) {
            // TODO: Having a treat expression actually makes sense here for fetchOnly
            // NOTE: I haven't found a use case for this yet, so I'd like to throw an exception instead of silently not supporting this
            throw new IllegalArgumentException("Unsupported expression for implicit joining found: " + expression);
        } else {
            // Other expressions don't need handling
        }
    }

    private JoinNode getFetchOwner(JoinNode node) {
        while (node.isFetch()) {
            node = node.getParent();
        }
        return node;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class LazyPathReference implements PathReference, Path {
        private final JoinNode baseNode;
        private final String field;
        private final Type<?> type;

        public LazyPathReference(JoinNode baseNode, String field, Type<?> type) {
            this.baseNode = baseNode;
            this.field = field;
            this.type = type;
        }

        @Override
        public JoinNode getBaseNode() {
            JoinTreeNode subNode = baseNode.getNodes().get(field);
            if (subNode != null && subNode.getDefaultNode() != null) {
                return subNode.getDefaultNode();
            }

            return baseNode;
        }

        @Override
        public String getField() {
            JoinTreeNode subNode = baseNode.getNodes().get(field);
            if (subNode != null && subNode.getDefaultNode() != null) {
                return null;
            }

            return field;
        }

        @Override
        public Type<?> getType() {
            return type;
        }

        @Override
        public From getFrom() {
            return getBaseNode();
        }

        @Override
        public String getPath() {
            StringBuilder sb = new StringBuilder();
            getBaseNode().appendDeReference(sb, getField());
            return sb.toString();
        }

        @Override
        public Class<?> getJavaType() {
            return type.getJavaType();
        }

        @Override
        public String toString() {
            return getPath();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((baseNode == null) ? 0 : baseNode.hashCode());
            result = prime * result + ((field == null) ? 0 : field.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof PathReference)) {
                return false;
            }
            PathReference other = (PathReference) obj;
            if (baseNode == null) {
                if (other.getBaseNode() != null) {
                    return false;
                }
            } else if (!baseNode.equals(other.getBaseNode())) {
                return false;
            }
            if (field == null) {
                if (other.getField() != null) {
                    return false;
                }
            } else if (!field.equals(other.getField())) {
                return false;
            }
            return true;
        }
    }

    private Type<?> getPathType(Type<?> baseType, String expression, PathExpression pathExpression) {
        try {
            return JpaUtils.getAttributeForJoining(metamodel, baseType, expressionFactory.createPathExpression(expression), null).getAttributeType();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                    + expression + "]", ex);
        }
    }

    private boolean isSingleValuedAssociationId(JoinResult joinResult, List<PathElementExpression> pathElements, boolean idRemovable) {
        JoinNode parent = joinResult.baseNode;
        int maybeSingularAssociationIndex = pathElements.size() - 2;
        int maybeSingularAssociationIdIndex = pathElements.size() - 1;
        Type<?> baseType;
        AttributeHolder maybeSingularAssociationJoinResult;
        PathElementExpression maybeSingularAssociationNameExpression = pathElements.get(maybeSingularAssociationIndex);
        String maybeSingularAssociationName = getSimpleName(maybeSingularAssociationNameExpression);

        if (parent == null) {
            // This is the case when we have exactly 2 path elements
            if (maybeSingularAssociationNameExpression instanceof TreatExpression) {
                // When we dereference a treat expression, we simply say this can never be a single valued association id
                return false;
            } else {
                AliasInfo a = aliasManager.getAliasInfo(maybeSingularAssociationName);

                if (a == null) {
                    // if the path element is no alias we can do some optimizations
                    parent = getRootNodeOrFail("Ambiguous join path [", maybeSingularAssociationName, "] because of multiple root nodes!");
                    baseType = parent.getManagedType();
                    maybeSingularAssociationJoinResult = JpaUtils.getAttributeForJoining(metamodel, baseType, maybeSingularAssociationNameExpression, parent.getAlias());
                } else if (!(a instanceof JoinAliasInfo)) {
                    throw new IllegalArgumentException("Can't dereference select alias in the expression!");
                } else {
                    // If there is a JoinAliasInfo for the path element, we have to use the alias
                    // We can only "consider" this path a single valued association id when we are about to "remove" the id part
                    if (idRemovable) {
                        JoinNode joinNode = ((JoinAliasInfo) a).getJoinNode();
                        PathElementExpression maybeSingularAssociationIdExpression = pathElements.get(maybeSingularAssociationIdIndex);
                        if (isId(joinNode.getNodeType(), maybeSingularAssociationIdExpression)) {
                            return true;
                        }
                        parent = joinNode.getParent();
                        if (joinNode.getParentTreeNode() == null) {
                            return false;
                        }
                        maybeSingularAssociationName = joinNode.getParentTreeNode().getRelationName();
                        ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, parent.getJavaType());
                        return managedType.getAttributes().containsKey(maybeSingularAssociationName + "." + maybeSingularAssociationIdExpression);
                    } else {
                        // Otherwise we return false in order to signal that a normal implicit join should be done
                        return false;
                    }
                }
            }
        } else {
            if (joinResult.hasField()) {
                Expression fieldExpression = expressionFactory.createPathExpression(joinResult.joinFields());
                AttributeHolder result = JpaUtils.getAttributeForJoining(metamodel, parent.getNodeType(), fieldExpression, parent.getAlias());
                baseType = result.getAttributeType();
            } else {
                baseType = parent.getNodeType();
            }

            maybeSingularAssociationJoinResult = JpaUtils.getAttributeForJoining(metamodel, baseType, maybeSingularAssociationNameExpression, null);
        }

        Attribute<?, ?> maybeSingularAssociation = maybeSingularAssociationJoinResult.getAttribute();
        if (maybeSingularAssociation == null) {
            // A naked root treat like TREAT(alias AS Subtype) has no attribute
            return false;
        }

        if (maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.MANY_TO_ONE
                && maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE
        ) {
            // Attributes that are not ManyToOne or OneToOne can't possibly be single value association sources
            return false;
        }

        if (maybeSingularAssociation instanceof MapKeyAttribute<?, ?>) {
            // Skip the foreign join column check for map keys
            // They aren't allowed as join sources in the JPA providers yet so we can only render them directly
        } else if (baseType instanceof EmbeddableType<?>) {
            // Get the base type. This is important if the path is "deeper" i.e. when having embeddables
            String attributePath = joinResult.joinFields(maybeSingularAssociationName);
            JoinNode node = parent;
            baseType = node.getNodeType();
            while (baseType instanceof EmbeddableType<?>) {
                if (node.getParentTreeNode() == null) {
                    break;
                }
                attributePath = node.getParentTreeNode().getRelationName() + "." + attributePath;
                node = node.getParent();
                baseType = node.getNodeType();
            }

            if (mainQuery.jpaProvider.isForeignJoinColumn((EntityType<?>) baseType, attributePath)) {
                return false;
            }
        } else if (mainQuery.jpaProvider.isForeignJoinColumn((EntityType<?>) baseType, maybeSingularAssociation.getName())) {
            return false;
        }

        PathElementExpression maybeSingularAssociationIdExpression = pathElements.get(maybeSingularAssociationIdIndex);
        ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, JpaMetamodelUtils.getTypeName(parent.getManagedType()));
        String field = maybeSingularAssociationName + "." + maybeSingularAssociationIdExpression;
        return managedType.getAttributes().containsKey(joinResult.joinFields(field));
    }

    private boolean isId(Type<?> type, Expression idExpression) {
        AttributeHolder maybeSingularAssociationIdJoinResult = JpaUtils.getAttributeForJoining(metamodel, type, idExpression, null);

        Attribute<?, ?> maybeSingularAssociationId = maybeSingularAssociationIdJoinResult.getAttribute();
        if (!(maybeSingularAssociationId instanceof SingularAttribute<?, ?>)) {
            return false;
        }

        if (!((SingularAttribute<?, ?>) maybeSingularAssociationId).isId()) {
            return false;
        }

        return true;
    }

    private String getSimpleName(PathElementExpression element) {
        if (element == null) {
            return null;
        } else if (element instanceof ArrayExpression) {
            return ((ArrayExpression) element).getBase().getProperty();
        } else {
            return element.toString();
        }
    }

    private String getJoinAlias(ArrayExpression expr) {
        StringBuilder sb = new StringBuilder(expr.getBase().toString());
        Expression indexExpr = expr.getIndex();

        if (indexExpr instanceof ParameterExpression) {
            ParameterExpression indexParamExpr = (ParameterExpression) indexExpr;
            sb.append('_');
            sb.append(indexParamExpr.getName());
        } else if (indexExpr instanceof PathExpression) {
            PathExpression indexPathExpr = (PathExpression) indexExpr;
            sb.append('_');
            sb.append(((JoinNode) indexPathExpr.getBaseNode()).getAliasInfo().getAlias());

            if (indexPathExpr.getField() != null) {
                sb.append('_');
                sb.append(indexPathExpr.getField().replaceAll("\\.", "_"));
            }
        } else if (indexExpr instanceof NumericLiteral) {
            sb.append('_');
            sb.append(((NumericLiteral) indexExpr).getValue());
        } else if (indexExpr instanceof StringLiteral) {
            sb.append('_');
            sb.append(((StringLiteral) indexExpr).getValue());
        } else {
            throw new IllegalStateException("Invalid array index expression " + indexExpr.toString());
        }

        return sb.toString();
    }

    private EqPredicate getArrayExpressionPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        PathExpression keyPath = new PathExpression(new ArrayList<PathElementExpression>(), true);
        keyPath.getExpressions().add(new PropertyExpression(joinNode.getAliasInfo().getAlias()));
        keyPath.setPathReference(new SimplePathReference(joinNode, null, joinNode.getNodeType()));
        Attribute<?, ?> arrayBaseAttribute = joinNode.getParentTreeNode().getAttribute();
        Expression keyExpression;
        if (arrayBaseAttribute instanceof ListAttribute<?, ?>) {
            keyExpression = new ListIndexExpression(keyPath);
        } else {
            keyExpression = new MapKeyExpression(keyPath);
        }
        return new EqPredicate(keyExpression, arrayExpr.getIndex());
    }

    private void registerDependencies(final JoinNode joinNode, CompoundPredicate onExpression) {
        onExpression.accept(new VisitorAdapter() {

            @Override
            public void visit(PathExpression pathExpr) {
                // prevent loop dependencies to the same join node
                if (pathExpr.getBaseNode() != joinNode) {
                    joinNode.getDependencies().add((JoinNode) pathExpr.getBaseNode());
                }
            }

        });
        joinNode.updateClauseDependencies(ClauseType.JOIN, false, new LinkedHashSet<JoinNode>());
    }

    private void generateAndApplyOnPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        EqPredicate valueKeyFilterPredicate = getArrayExpressionPredicate(joinNode, arrayExpr);

        if (joinNode.getOnPredicate() != null) {
            CompoundPredicate currentPred = joinNode.getOnPredicate();

            // Only add the predicate if it isn't contained yet
            if (!findPredicate(currentPred, valueKeyFilterPredicate)) {
                currentPred.getChildren().add(valueKeyFilterPredicate);
                registerDependencies(joinNode, currentPred);
            }
        } else {
            CompoundPredicate onAndPredicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND);
            onAndPredicate.getChildren().add(valueKeyFilterPredicate);
            joinNode.setOnPredicate(onAndPredicate);
            registerDependencies(joinNode, onAndPredicate);
        }
    }

    private JoinResult implicitJoin(JoinNode current, PathExpression pathExpression, ClauseType fromClause, String selectAlias, int start, int end, boolean allowParentAliases) {
        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        List<String> resultFields = new ArrayList<String>();
        PathElementExpression elementExpr;

        for (int i = start; i < end; i++) {
            AliasInfo aliasInfo;
            elementExpr = pathElements.get(i);

            if (elementExpr instanceof ArrayExpression) {
                ArrayExpression arrayExpr = (ArrayExpression) elementExpr;
                String joinRelationName;
                List<String> joinRelationAttributes;

                if (!resultFields.isEmpty()) {
                    resultFields.add(arrayExpr.getBase().toString());
                    joinRelationAttributes = resultFields;
                    resultFields = new ArrayList<>();
                    joinRelationName = StringUtils.join(".", joinRelationAttributes);
                } else {
                    joinRelationName = arrayExpr.getBase().toString();
                    joinRelationAttributes = Arrays.asList(joinRelationName);
                }

                current = current == null ? getRootNodeOrFail("Ambiguous join path [", joinRelationName, "] because of multiple root nodes!") : current;
                // Find a node by a predicate match
                JoinNode matchingNode = findNode(current, joinRelationName, arrayExpr);

                if (matchingNode != null) {
                    current = matchingNode;
                } else if (i == 0 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(joinRelationName)) != null) {
                    // The first node is allowed to be a join alias
                    if (aliasInfo instanceof SelectInfo) {
                        throw new IllegalArgumentException("Illegal reference to the select alias '" + joinRelationName + "'");
                    }
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    generateAndApplyOnPredicate(current, arrayExpr);
                } else {
                    String joinAlias = getJoinAlias(arrayExpr);
                    final JoinResult result = createOrUpdateNode(current, joinRelationAttributes, null, joinAlias, null, true, false);
                    current = result.baseNode;
                    resultFields = result.addToList(resultFields);
                    generateAndApplyOnPredicate(current, arrayExpr);
                }
            } else if (elementExpr instanceof TreatExpression) {
                if (i != 0 || current != null) {
                    throw new IllegalArgumentException("A treat expression should be the first element in a path!");
                }
                TreatExpression treatExpression = (TreatExpression) elementExpr;
                boolean fromSubquery = false;
                boolean fromSelectAlias = false;
                boolean joinRequired = false;
                boolean fetch = false;

                if (treatExpression.getExpression() instanceof PathExpression) {
                    PathExpression treatedPathExpression = (PathExpression) treatExpression.getExpression();
                    implicitJoin(treatedPathExpression, true, treatExpression.getType(), fromClause, selectAlias, fromSubquery, fromSelectAlias, true, false, fetch);
                    JoinNode treatedJoinNode = (JoinNode) treatedPathExpression.getBaseNode();
                    EntityType<?> treatType = metamodel.getEntity(treatExpression.getType());
                    current = treatedJoinNode.getTreatedJoinNode(treatType);
                } else {
                    throw new UnsupportedOperationException("Unsupported treated expression type: " + treatExpression.getExpression().getClass());
                }
            } else if (elementExpr instanceof MapKeyExpression) {
                MapKeyExpression mapKeyExpression = (MapKeyExpression) elementExpr;
                boolean fromSubquery = false;
                boolean fromSelectAlias = false;
                boolean joinRequired = true;
                boolean fetch = false;
                current = joinMapKey(mapKeyExpression, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, fetch, true, true);
            } else if (elementExpr instanceof MapValueExpression) {
                MapValueExpression mapValueExpression = (MapValueExpression) elementExpr;
                boolean fromSubquery = false;
                boolean fromSelectAlias = false;
                boolean joinRequired = true;
                boolean fetch = false;

                implicitJoin(mapValueExpression.getPath(), true, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, fetch);
                current = (JoinNode) mapValueExpression.getPath().getBaseNode();
            } else if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectInfo) {
                    throw new IllegalArgumentException("Can't dereference a select alias");
                } else {
                    // Join alias usage like in "joinAlias.relationName"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                }
            } else {
                if (resultFields.isEmpty()) {
                    final JoinResult result = implicitJoinSingle(current, elementExpr.toString(), allowParentAliases);
                    if (current != result.baseNode) {
                        current = result.baseNode;
                    }
                    resultFields = result.addToList(resultFields);
                } else {
                    resultFields.add(elementExpr.toString());
                    JoinResult currentResult = createOrUpdateNode(current, resultFields, null, null, null, true, true);
                    current = currentResult.baseNode;
                    if (!currentResult.hasField()) {
                        resultFields.clear();
                    }
                }

            }
        }

        if (resultFields.isEmpty()) {
            return new JoinResult(current, null, current == null ? null : current.getNodeType());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(resultFields.get(0));
            for (int i = 1; i < resultFields.size(); i++) {
                sb.append('.');
                sb.append(resultFields.get(i));
            }
            Expression expression = expressionFactory.createSimpleExpression(sb.toString(), false);
            Type<?> type = JpaUtils.getAttributeForJoining(metamodel, current.getNodeType(), expression, current.getAlias()).getAttributeType();
            return new JoinResult(current, resultFields, type);
        }
    }

    private JoinNode joinMapKey(MapKeyExpression mapKeyExpression, String alias, ClauseType fromClause, String selectAlias, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch, boolean implicit, boolean defaultJoin) {
        implicitJoin(mapKeyExpression.getPath(), true, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
        JoinNode current = (JoinNode) mapKeyExpression.getPath().getBaseNode();
        String joinRelationName = "KEY(" + current.getParentTreeNode().getRelationName() + ")";
        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) current.getParentTreeNode().getAttribute();
        Attribute<?, ?> keyAttribute = new MapKeyAttribute<>(mapAttribute);
        String aliasToUse = alias == null ? current.getParentTreeNode().getRelationName().replaceAll("\\.", "_") + "_key" : alias;
        Type<?> joinRelationType = metamodel.type(mapAttribute.getKeyJavaType());
        current = getOrCreate(current, joinRelationName, joinRelationType, null, aliasToUse, JoinType.LEFT, "Ambiguous implicit join", implicit, true, keyAttribute);
        return current;
    }

    private JoinNode joinMapEntry(MapEntryExpression mapEntryExpression, String alias, ClauseType fromClause, String selectAlias, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch, boolean implicit, boolean defaultJoin) {
        implicitJoin(mapEntryExpression.getPath(), true, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
        JoinNode current = (JoinNode) mapEntryExpression.getPath().getBaseNode();
        String joinRelationName = "ENTRY(" + current.getParentTreeNode().getRelationName() + ")";
        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) current.getParentTreeNode().getAttribute();
        Attribute<?, ?> entryAttribute = new MapEntryAttribute<>(mapAttribute);
        String aliasToUse = alias == null ? current.getParentTreeNode().getRelationName().replaceAll("\\.", "_") + "_entry" : alias;
        Type<?> joinRelationType = metamodel.type(Map.Entry.class);
        current = getOrCreate(current, joinRelationName, joinRelationType, null, aliasToUse, JoinType.LEFT, "Ambiguous implicit join", implicit, true, entryAttribute);
        return current;
    }

    private JoinNode joinListIndex(ListIndexExpression listIndexExpression, String alias, ClauseType fromClause, String selectAlias, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch, boolean implicit, boolean defaultJoin) {
        implicitJoin(listIndexExpression.getPath(), true, null, fromClause, selectAlias, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
        JoinNode current = (JoinNode) listIndexExpression.getPath().getBaseNode();
        String joinRelationName = "INDEX(" + current.getParentTreeNode().getRelationName() + ")";
        ListAttribute<?, ?> listAttribute = (ListAttribute<?, ?>) current.getParentTreeNode().getAttribute();
        Attribute<?, ?> indexAttribute = new ListIndexAttribute<>(listAttribute);
        String aliasToUse = alias == null ? current.getParentTreeNode().getRelationName().replaceAll("\\.", "_") + "_index" : alias;
        Type<?> joinRelationType = metamodel.type(Integer.class);
        current = getOrCreate(current, joinRelationName, joinRelationType, null, aliasToUse, JoinType.LEFT, "Ambiguous implicit join", implicit, true, indexAttribute);
        return current;
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName, boolean allowParentAliases) {
        if (baseNode == null) {
            // When no base is given, check if the attribute name is an alias
            AliasInfo aliasInfo = allowParentAliases ?
                    aliasManager.getAliasInfo(attributeName) :
                    aliasManager.getAliasInfoForBottomLevel(attributeName);
            if (aliasInfo != null && aliasInfo instanceof JoinAliasInfo) {
                JoinNode node = ((JoinAliasInfo) aliasInfo).getJoinNode();
                // if it is, we can just return the join node
                return new JoinResult(node, null, node.getNodeType());
            }
        }

        // If we have no base node, root is assumed
        if (baseNode == null) {
            baseNode = getRootNodeOrFail("Ambiguous join path [", attributeName, "] because of multiple root nodes!");
        }

        // check if the path is joinable, assuming it is relative to the root (implicit root prefix)
        return createOrUpdateNode(baseNode, Arrays.asList(attributeName), null, null, null, true, true);
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName, boolean objectLeafAllowed, boolean joinRequired) {
        JoinNode newBaseNode;
        String field;
        Type<?> type;
        boolean lazy = false;
        Type<?> baseNodeType = baseNode.getNodeType();
        // The given path may be relative to the root or it might be an alias
        if (objectLeafAllowed) {
            AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(metamodel, baseNodeType, expressionFactory.createJoinPathExpression(attributeName), baseNode.getAlias());
            Attribute<?, ?> attr = attributeHolder.getAttribute();
            if (attr == null) {
                throw new IllegalArgumentException("Field with name '" + attributeName + "' was not found within managed type " + JpaMetamodelUtils.getTypeName(baseNodeType));
            }

            if (joinRequired || attr.isCollection()) {
                final JoinResult newBaseNodeResult = implicitJoinSingle(baseNode, attributeName, false);
                newBaseNode = newBaseNodeResult.baseNode;
                // check if the last path element was also joined
                if (newBaseNode != baseNode) {
                    field = null;
                    type = newBaseNode.getNodeType();
                } else {
                    field = attributeName;
                    type = attributeHolder.getAttributeType();
                }
            } else {
                newBaseNode = baseNode;
                field = attributeName;
                type = attributeHolder.getAttributeType();
                lazy = true;
            }
        } else {
            AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(metamodel, baseNodeType, expressionFactory.createJoinPathExpression(attributeName), baseNode.getAlias());
            Attribute<?, ?> attr = attributeHolder.getAttribute();
            if (attr == null) {
                throw new IllegalArgumentException("Field with name " + attributeName + " was not found within class " + JpaMetamodelUtils.getTypeName(baseNodeType));
            }
            if (JpaMetamodelUtils.isJoinable(attr)) {
                if (JpaMetamodelUtils.isCompositeNode(attr)) {
                    throw new IllegalArgumentException("No object leaf allowed but " + attributeName + " is an object leaf");
                } else {
                    final JoinResult newBaseNodeResult = implicitJoinSingle(baseNode, attributeName, false);
                    newBaseNode = newBaseNodeResult.baseNode;
                    field = null;
                    type = newBaseNode.getNodeType();
                }
            } else {
                newBaseNode = baseNode;
                field = attributeName;
                type = attributeHolder.getAttributeType();
            }
        }
        return new JoinResult(newBaseNode, field == null ? null : Arrays.asList(field), type, lazy);
    }

    private JoinType getModelAwareType(JoinNode baseNode, Attribute<?, ?> attr) {
        if (baseNode.getJoinType() == JoinType.LEFT) {
            return JoinType.LEFT;
        }

        if ((attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE)
                && ((SingularAttribute<?, ?>) attr).isOptional() == false) {
            return JoinType.INNER;
        } else {
            return JoinType.LEFT;
        }
    }

    private JoinResult createOrUpdateNode(JoinNode baseNode, List<String> joinRelationAttributes, String treatType, String alias, JoinType joinType, boolean implicit, boolean defaultJoin) {
        Type<?> baseNodeType = baseNode.getNodeType();
        String joinRelationName = StringUtils.join(".", joinRelationAttributes);
        AttributeHolder attrJoinResult = JpaUtils.getAttributeForJoining(metamodel, baseNodeType, expressionFactory.createJoinPathExpression(joinRelationName), baseNode.getAlias());
        Attribute<?, ?> attr = attrJoinResult.getAttribute();
        if (attr == null) {
            throw new IllegalArgumentException("Field with name " + joinRelationName + " was not found within class " + JpaMetamodelUtils.getTypeName(baseNodeType));
        }

        if (!JpaMetamodelUtils.isJoinable(attr)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(new StringBuilder("Field with name ").append(joinRelationName)
                        .append(" of class ")
                        .append(JpaMetamodelUtils.getTypeName(baseNodeType))
                        .append(" is parseable and therefore it has not to be fetched explicitly.")
                        .toString());
            }
            return new JoinResult(baseNode, joinRelationAttributes, attrJoinResult.getAttributeType());
        }

        if (implicit) {
            String aliasToUse = alias == null ? attr.getName() : alias;
            alias = baseNode.getAliasInfo().getAliasOwner().generateJoinAlias(aliasToUse);
        }

        if (joinType == null) {
            joinType = getModelAwareType(baseNode, attr);
        }

        Type<?> joinRelationType = attrJoinResult.getAttributeType();
        JoinNode newNode = getOrCreate(baseNode, joinRelationName, joinRelationType, treatType, alias, joinType, "Ambiguous implicit join", implicit, defaultJoin, attr);

        return new JoinResult(newNode, null, newNode.getNodeType());
    }

    private void checkAliasIsAvailable(AliasManager aliasManager, String alias, String currentJoinPath, String errorMessage) {
        AliasInfo oldAliasInfo = aliasManager.getAliasInfoForBottomLevel(alias);
        if (oldAliasInfo instanceof SelectInfo) {
            throw new IllegalStateException("Alias [" + oldAliasInfo.getAlias() + "] already used as select alias");
        }
        JoinAliasInfo oldJoinAliasInfo = (JoinAliasInfo) oldAliasInfo;
        if (oldJoinAliasInfo != null) {
            if (!oldJoinAliasInfo.getAbsolutePath().equals(currentJoinPath)) {
                throw new IllegalArgumentException(errorMessage);
            } else {
                throw new RuntimeException("Probably a programming error if this happens. An alias[" + alias + "] for the same join path["
                        + currentJoinPath + "] is available but the join node is not!");
            }
        }
    }

    private JoinNode getOrCreate(JoinNode baseNode, String joinRelationName, Type<?> joinRelationType, String treatType, String alias, JoinType type, String errorMessage, boolean implicit, boolean defaultJoin, Attribute<?, ?> attribute) {
        JoinTreeNode treeNode = baseNode.getOrCreateTreeNode(joinRelationName, attribute);
        JoinNode node = treeNode.getJoinNode(alias, defaultJoin);
        String qualificationExpression = null;
        String qualifiedJoinPath;

        if (attribute instanceof QualifiedAttribute) {
            QualifiedAttribute qualifiedAttribute = (QualifiedAttribute) attribute;
            qualificationExpression = qualifiedAttribute.getQualificationExpression();
            qualifiedJoinPath = joinRelationName.substring(0, qualificationExpression.length() + 1) +
                    baseNode.getAliasInfo().getAbsolutePath()  + "." + joinRelationName.substring(qualificationExpression.length() + 1);
        } else {
            qualifiedJoinPath = baseNode.getAliasInfo().getAbsolutePath() + "." + joinRelationName;
        }

        EntityType<?> treatJoinType;
        String currentJoinPath;

        if (treatType != null) {
            // Verify it's a valid type
            treatJoinType = metamodel.getEntity(treatType);
            currentJoinPath = "TREAT(" + qualifiedJoinPath + " AS " + treatJoinType.getName() + ")";
        } else {
            treatJoinType = null;
            currentJoinPath = qualifiedJoinPath;
        }

        if (node == null) {
            // a join node for the join relation does not yet exist
            AliasManager aliasManager = baseNode.getAliasInfo().getAliasOwner();
            checkAliasIsAvailable(aliasManager, alias, currentJoinPath, errorMessage);

            // the alias might have to be postfixed since it might already exist in parent queries
            if (implicit && aliasManager.getAliasInfo(alias) != null) {
                alias = aliasManager.generateJoinAlias(alias);
            }

            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, false, aliasManager);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = JoinNode.createAssociationJoinNode(baseNode, treeNode, type, joinRelationType, treatJoinType, qualificationExpression, newAliasInfo);
            newAliasInfo.setJoinNode(node);
            treeNode.addJoinNode(node, defaultJoin);
        } else {
            JoinAliasInfo nodeAliasInfo = node.getAliasInfo();

            if (!alias.equals(nodeAliasInfo.getAlias())) {
                // Aliases for the same join paths don't match
                if (nodeAliasInfo.isImplicit() && !implicit) {
                    // Overwrite implicit aliases
                    aliasManager.unregisterAliasInfoForBottomLevel(nodeAliasInfo);
                    // we must alter the nodeAliasInfo instance since this instance is also set on the join node

                    // TODO: we must update the key for the JoinNode in the respective JoinTreeNode
                    nodeAliasInfo.setAlias(alias);
                    nodeAliasInfo.setImplicit(false);
                    // We can only change the join type if the existing node is implicit and the update on the node is not implicit
                    node.setJoinType(type);

                    aliasManager.registerAliasInfo(nodeAliasInfo);
                } else if (!nodeAliasInfo.isImplicit() && !implicit) {
                    throw new IllegalArgumentException("Alias conflict [" + nodeAliasInfo.getAlias() + "=" + nodeAliasInfo.getAbsolutePath() + ", "
                            + alias + "=" + currentJoinPath + "]");
                }
            }

            if (treatJoinType != null) {
                if (node.getTreatType() == null) {
                    node = node.getTreatedJoinNode(treatJoinType);
                } else if (!treatJoinType.equals(node.getTreatType())) {
                    throw new IllegalArgumentException("A join node [" + nodeAliasInfo.getAlias() + "=" + nodeAliasInfo.getAbsolutePath() + "] "
                            + "for treat type [" + treatType + "] conflicts with the existing treat type [" + node.getTreatType() + "]");
                }
            }
        }
        return node;
    }

    private JoinNode findNode(JoinNode baseNode, String joinRelationName, ArrayExpression arrayExpression) {
        JoinTreeNode treeNode = baseNode.getNodes().get(joinRelationName);

        if (treeNode == null) {
            return null;
        }

        for (JoinNode node : treeNode.getJoinNodes().values()) {
            Predicate pred = getArrayExpressionPredicate(node, arrayExpression);
            CompoundPredicate compoundPredicate = node.getOnPredicate();

            if (findPredicate(compoundPredicate, pred)) {
                return node;
            }
        }

        return null;
    }

    private boolean findPredicate(CompoundPredicate compoundPredicate, Predicate pred) {
        if (compoundPredicate != null) {
            List<Predicate> children = compoundPredicate.getChildren();
            int size = children.size();
            for (int i = 0; i < size; i++) {
                if (pred.equals(children.get(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Fetch the given node only.
     *
     * @param node
     */
    private void fetchPath(JoinNode node) {
        node.setFetch(true);
        // fetches implicitly need to be selected
        node.getClauseDependencies().add(ClauseType.SELECT);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    // TODO: needs equals-hashCode implementation
    private static class JoinResult {

        final JoinNode baseNode;
        final List<String> fields;
        final Type<?> type;
        final boolean lazy;

        public JoinResult(JoinNode baseNode, List<String> fields, Type<?> type) {
            this.baseNode = baseNode;
            this.fields = fields;
            this.type = type;
            this.lazy = false;
        }

        public JoinResult(JoinNode baseNode, List<String> fields, Type<?> type, boolean lazy) {
            this.baseNode = baseNode;
            this.fields = fields;
            this.type = type;
            this.lazy = lazy;
        }

        private boolean hasField() {
            return fields != null && !fields.isEmpty();
        }

        private String joinFields(String field) {
            if (fields == null || fields.isEmpty()) {
                return field;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(fields.get(0));
            for (int i = 1; i < fields.size(); i++) {
                sb.append('.');
                sb.append(fields.get(i));
            }

            if (field != null) {
                sb.append('.');
                sb.append(field);
            }

            return sb.toString();
        }

        private String joinFields() {
            return joinFields(null);
        }

        private List<String> addToList(List<String> resultFields) {
            if (hasField()) {
                if (resultFields != fields) {
                    resultFields.addAll(fields);
                }
            }

            return resultFields;
        }

        private boolean isLazy() {
            return lazy;
        }

    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private class JoinOnBuilderEndedListener extends PredicateBuilderEndedListenerImpl {

        private JoinNode joinNode;

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            super.onBuilderEnded(builder);
            Predicate predicate = builder.getPredicate();
            predicate.accept(new VisitorAdapter() {

                private boolean isKeyFunction;

                @Override
                public void visit(ListIndexExpression expression) {
                    boolean old = isKeyFunction;
                    this.isKeyFunction = true;
                    super.visit(expression);
                    this.isKeyFunction = old;
                }

                @Override
                public void visit(MapKeyExpression expression) {
                    boolean old = isKeyFunction;
                    this.isKeyFunction = true;
                    super.visit(expression);
                    this.isKeyFunction = old;
                }

                @Override
                public void visit(PathExpression expression) {
                    expression.setCollectionKeyPath(isKeyFunction);
                    super.visit(expression);
                }

            });
            joinNode.setOnPredicate((CompoundPredicate) predicate);
            joinNode.updateClauseDependencies(ClauseType.JOIN, false, new LinkedHashSet<JoinNode>());
        }
    }
}
