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

import com.blazebit.persistence.impl.builder.predicate.JoinOnBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.persistence.CollectionTable;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class JoinManager extends AbstractManager {

    private final static Logger LOG = Logger.getLogger(JoinManager.class.getName());

    // we might have multiple nodes that depend on the same unresolved alias,
    // hence we need a List of NodeInfos.
    // e.g. SELECT a.X, a.Y FROM A a
    // a is unresolved for both X and Y
    private JoinNode rootNode;
    // root entity class
    private final String joinRestrictionKeyword;
    private final AliasManager aliasManager;
    private final Metamodel metamodel; // needed for model-aware joins
    private final JoinManager parent;
    private final JoinOnBuilderEndedListener joinOnBuilderListener;
    private SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    // helper collections for join rendering
    private final Set<JoinNode> renderedJoins = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());
    private final Set<JoinNode> markedJoinNodes = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());

    private static enum JoinClauseBuildMode {

        NORMAL,
        COUNT,
        ID
    };

    JoinManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, JPAInfo jpaInfo, AliasManager aliasManager, Metamodel metamodel, JoinManager parent) {
        super(queryGenerator, parameterManager);
        this.aliasManager = aliasManager;
        this.metamodel = metamodel;
        this.parent = parent;
        this.joinRestrictionKeyword = " " + jpaInfo.getOnClause() + " ";
        this.joinOnBuilderListener = new JoinOnBuilderEndedListener();
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }
    
    void setRoot(Class<?> clazz, String rootAlias){
        if (rootAlias == null) {
            if (aliasManager.getAliasInfo(clazz.getSimpleName().toLowerCase()) == null) {
                rootAlias = clazz.getSimpleName().toLowerCase();
            } else {
                rootAlias = aliasManager.generatePostfixedAlias(clazz.getSimpleName().toLowerCase());
            }
        }
        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, aliasManager);
        rootNode = new JoinNode(null, null, rootAliasInfo, null, clazz);
        rootAliasInfo.setJoinNode(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
    }

    String getRootAlias() {
        return rootNode.getAliasInfo().getAlias();
    }

    boolean hasCollections() {
        return rootNode.hasCollections();
    }

    String getRootId() {
        EntityType<?> entityType = metamodel.entity(rootNode.getPropertyClass());
        return entityType.getId(entityType.getIdType()
                .getJavaType())
                .getName();
    }

    JoinManager getParent() {
        return parent;
    }

    void setSubqueryInitFactory(SubqueryInitiatorFactory subqueryInitFactory) {
        this.subqueryInitFactory = subqueryInitFactory;
    }

    void buildJoins(StringBuilder sb, Set<ClauseType> clauseExclusions, String aliasPrefix) {
        rootNode.registerDependencies();
        renderedJoins.clear();
        applyJoins(sb, rootNode.getAliasInfo(), rootNode.getNodes(), clauseExclusions, aliasPrefix);
    }

    void verifyBuilderEnded() {
        joinOnBuilderListener.verifyBuilderEnded();
    }

    void acceptVisitor(JoinNodeVisitor v) {
        rootNode.accept(v);
    }

    void applyTransformer(ExpressionTransformer transformer) {
        rootNode.accept(new OnClauseJoinNodeVisitor(new PredicateManager.TransformationVisitor(transformer, null)));
    }

    private void renderJoinNode(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node, String aliasPrefix) {
        if (!renderedJoins.contains(node)) {
            switch (node.getType()) {
                case INNER:
                    sb.append(" JOIN ");
                    break;
                case LEFT:
                    sb.append(" LEFT JOIN ");
                    break;
                case RIGHT:
                    sb.append(" RIGHT JOIN ");
                    break;
            }
            if (node.isFetch()) {
                sb.append("FETCH ");
            }
            
            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }
            
            sb.append(joinBase.getAlias()).append('.').append(node.getParentTreeNode().getRelationName()).append(' ');
            
            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }
            
            sb.append(node.getAliasInfo().getAlias());

            if (node.getWithPredicate() != null && !node.getWithPredicate().getChildren().isEmpty()) {
                sb.append(joinRestrictionKeyword);
                queryGenerator.setQueryBuffer(sb);
                node.getWithPredicate().accept(queryGenerator);
            }
            renderedJoins.add(node);
        }
    }

    private void renderReverseDependency(StringBuilder sb, JoinNode dependency, String aliasPrefix) {
        if (dependency.getParent() != null) {
            renderReverseDependency(sb, dependency.getParent(), aliasPrefix);
            if (!dependency.getDependencies().isEmpty()) {
                markedJoinNodes.add(dependency);
                try {
                    for (JoinNode dep : dependency.getDependencies()) {
                        if (markedJoinNodes.contains(dep)) {
                            throw new IllegalStateException("Cyclic join dependency detected at absolute path [" + dep.getAliasInfo().getAbsolutePath() + "] with alias [" + dep.getAliasInfo().getAlias() + "]");
                        }
                        //render reverse dependencies
                        renderReverseDependency(sb, dep, aliasPrefix);
                    }
                } finally {
                    markedJoinNodes.remove(dependency);
                }
            }
            renderJoinNode(sb, dependency.getParent().getAliasInfo(), dependency, aliasPrefix);
        }
    }
    
    private boolean isOptionalRelation(JoinNode node) {
        Class<?> baseNodeType = node.getParent().getPropertyClass();
        ManagedType type = metamodel.managedType(baseNodeType);
        Attribute attr = type.getAttribute(node.getParentTreeNode().getRelationName());
        if (attr == null) {
            throw new IllegalArgumentException("Field with name "
                    + node.getParentTreeNode().getRelationName() + " was not found within class "
                    + baseNodeType.getName());
        }
        
        if (attr instanceof SingularAttribute<?, ?>) {
            return ((SingularAttribute<?, ?>) attr).isOptional();
        }
        
        return true;
    }
    
    private boolean isEmptyCondition(JoinNode node) {
        return node.getWithPredicate() == null || node.getWithPredicate().getChildren().isEmpty();
    }
    
    private boolean isArrayExpressionCondition(JoinNode node) {
        if (node.getWithPredicate() == null || node.getWithPredicate().getChildren().size() != 1) {
            return false;
        }
        
        Predicate predicate = node.getWithPredicate().getChildren().get(0);
        if (!(predicate instanceof EqPredicate)) {
            return false;
        }
        
        EqPredicate eqPredicate = (EqPredicate) predicate;
        Expression left = eqPredicate.getLeft();
        if (!(left instanceof FunctionExpression)) {
            return false;
        }
        
        FunctionExpression keyExpression = (FunctionExpression) left;
        if (!"KEY".equals(keyExpression.getFunctionName())) {
            return false;
        }
        
        Expression keyContentExpression = keyExpression.getExpressions().get(0);
        if (!(keyContentExpression instanceof PathExpression)) {
            return false;
        }
        
        PathExpression keyPath = (PathExpression) keyContentExpression;
        if (!node.equals(keyPath.getBaseNode())) {
            return false;
        }
        
        return true;
    }
    
    // TODO: Maybe do that more efficient in a future version
    private boolean isMandatoryJoin(JoinNode node) {
        if (node.getType() == JoinType.INNER) {
            if (isOptionalRelation(node) || !isEmptyCondition(node)) {
                return true;
            }
        } else if (node.getType() == JoinType.LEFT) {
            if (!isEmptyCondition(node) && !isArrayExpressionCondition(node)) {
                return true;
            }
            
            for (Map.Entry<String, JoinTreeNode> nodeEntry : node.getNodes().entrySet()) {
                JoinTreeNode treeNode = nodeEntry.getValue();

                for (JoinNode childNode : treeNode.getJoinNodes().values()) {
                    if (isMandatoryJoin(childNode)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, Map<String, JoinTreeNode> nodes, Set<ClauseType> clauseExclusions, String aliasPrefix) {
        for (Map.Entry<String, JoinTreeNode> nodeEntry : nodes.entrySet()) {
            JoinTreeNode treeNode = nodeEntry.getValue();

            for (JoinNode node : treeNode.getJoinNodes().values()) {
                if (!isMandatoryJoin(node) && !clauseExclusions.isEmpty() && clauseExclusions.containsAll(node.getClauseDependencies())) {
                    continue;
                }

                if (!node.getDependencies().isEmpty()) {
                    renderReverseDependency(sb, node, aliasPrefix);
                }

                renderJoinNode(sb, joinBase, node, aliasPrefix);

                if (!node.getNodes().isEmpty()) {
                    applyJoins(sb, node.getAliasInfo(), node.getNodes(), clauseExclusions, aliasPrefix);
                }
            }
        }
    }

    private boolean isExternal(PathExpression path) {
        PathElementExpression firstElem = path.getExpressions().get(0);
        String startAlias;
        if (firstElem instanceof ArrayExpression) {
            startAlias = ((ArrayExpression) firstElem).getBase().toString();
        } else {
            startAlias = firstElem.toString();
        }

        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return false;
        }

        if (parent != null && aliasInfo.getAliasOwner() == parent.aliasManager) {
            // the alias exists but originates from the parent query builder

            // an external select alias must not be dereferenced
            if (aliasInfo instanceof SelectInfo) {
                throw new ExternalAliasDereferencingException("Start alias [" + startAlias + "] of path [" + path.toString() + "] is external and must not be dereferenced");
            }

            // the alias is external so we do not have to treat it
            return true;
        } else if (aliasInfo.getAliasOwner() == aliasManager) {
            // the alias originates from the current query builder an is therefore not external
            return false;
        } else {
            throw new IllegalStateException("Alias [" + aliasInfo.getAlias() + "] originates from an unknown query");
        }
    }

    private boolean isJoinableSelectAlias(PathExpression pathExpr, boolean fromSelect, boolean fromSubquery) {
        boolean singlePathElement = pathExpr.getExpressions().size() == 1;
        String startAlias = pathExpr.getExpressions().get(0).toString();

        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return false;
        }

        if (aliasInfo instanceof SelectInfo && !fromSelect && !fromSubquery) {
            // select alias
            if (!singlePathElement) {
                throw new IllegalStateException("Path starting with select alias not allowed");
            }

            // might be joinable
            return true;
        }

        return false;
    }

    <X> JoinOnBuilder<X> joinOn(X result, String path, String alias, JoinType type, boolean defaultJoin) {
        joinOnBuilderListener.joinNode = join(path, alias, type, false, defaultJoin);
        return joinOnBuilderListener.startBuilder(new JoinOnBuilderImpl<X>(result, joinOnBuilderListener, parameterManager, expressionFactory, subqueryInitFactory));
    }

    JoinNode join(String path, String alias, JoinType type, boolean fetch, boolean defaultJoin) {
        Expression expr = expressionFactory.createPathExpression(path);
        PathExpression pathExpression;
        if (expr instanceof PathExpression) {
            pathExpression = (PathExpression) expr;
        } else {
            throw new IllegalArgumentException("Join path [" + path + "] is not a path");
        }

        if (isExternal(pathExpression) || isJoinableSelectAlias(pathExpression, false, false)) {
            throw new IllegalArgumentException("No external path or select alias allowed in join path");
        }

        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);
        JoinNode current = implicitJoin(null, pathExpression, 0, pathElements.size() - 1);

        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        } else {
            current = current == null ? rootNode : current;
            current = createOrUpdateNode(current, elementExpr.toString(), alias, type, false, defaultJoin);
        }

        if (fetch) {
            fetchPath(current);
        }

        return current;
    }

    void implicitJoin(Expression expression, boolean objectLeafAllowed, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias) {
        implicitJoin(expression, objectLeafAllowed, fromClause, fromSubquery, fromSelectAlias, false);
    }

    void implicitJoin(Expression expression, boolean objectLeafAllowed, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean fetch) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;

            if (isJoinableSelectAlias(pathExpression, fromClause == ClauseType.SELECT, fromSubquery)) {
                String alias = pathExpression.getExpressions().get(0).toString();
                Expression expr = ((SelectInfo) aliasManager.getAliasInfo(alias)).getExpression();

                // this check is necessary to prevent infinite recursion in the case of e.g. SELECT name AS name
                if (!fromSelectAlias) {
                    // we have to do this implicit join because we might have to adjust the selectOnly flag in the referenced join nodes
                    implicitJoin(expr, true, fromClause, fromSubquery, true);
                }
                return;
            } else if (isExternal(pathExpression)) {
                // try to set base node and field for the external expression based
                // on existing joins in the super query
                parent.implicitJoin(pathExpression, true, fromClause, true, fromSelectAlias);
                return;
            }

            // First try to implicit join indices of array expressions since we will need their base nodes
            for (PathElementExpression pathElem : pathExpression.getExpressions()) {
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, fromClause, fromSubquery, fromSelectAlias);
                }
            }

            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);
            boolean singleValuedAssociationIdExpression = false;
            JoinNode current = null;

            boolean explicitRootAlias = pathElements.get(0).toString().equals(rootNode.getAliasInfo().getAlias());
            int startIndex = 0;

            if (explicitRootAlias) {
                startIndex = 1;
                current = rootNode;
            }

            if (pathElements.size() > startIndex + 1) {
                int maybeSingularAssociationIndex = pathElements.size() - 2;
                int maybeSingularAssociationIdIndex = pathElements.size() - 1;
                current = implicitJoin(current, pathExpression, startIndex, maybeSingularAssociationIndex);
                singleValuedAssociationIdExpression = isSingleValuedAssociationId(current, pathElements);

                if (singleValuedAssociationIdExpression) {
                } else {
                    current = implicitJoin(current, pathExpression, maybeSingularAssociationIndex, maybeSingularAssociationIdIndex);
                }
            } else {
                current = implicitJoin(current, pathExpression, startIndex, pathElements.size() - 1);
            }

            // current might be null
            current = current == null ? rootNode : current;

            JoinResult result;
            AliasInfo aliasInfo;

            if (singleValuedAssociationIdExpression) {
                String associationName = pathElements.get(pathElements.size() - 2).toString();
                AliasInfo a = aliasManager.getAliasInfoForBottomLevel(associationName);
                JoinTreeNode treeNode;

                if (a != null) {
                    current = ((JoinAliasInfo) a).getJoinNode();
                    result = new JoinResult(current, elementExpr.toString());
                } else {
                    treeNode = current.getNodes().get(associationName);

                    if (treeNode != null && treeNode.getDefaultNode() != null) {
                        result = new JoinResult(treeNode.getDefaultNode(), elementExpr.toString());
                    } else {
                        result = new JoinResult(current, associationName + "." + elementExpr.toString());
                    }
                }
            } else if (elementExpr instanceof ArrayExpression) {
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
                    generateAndApplyWithPredicate(current, arrayExpr);
                } else if ((matchingNode = findNode(current, joinRelationName, arrayExpr)) != null) {
                    // We found a join node for the same join relation with the same array expression predicate
                    current = matchingNode;
                } else {
                    String joinAlias = getJoinAlias(arrayExpr);
                    current = createOrUpdateNode(current, joinRelationName, joinAlias, null, true, false);
                    generateAndApplyWithPredicate(current, arrayExpr);
                }

                result = new JoinResult(current, null);
            } else if (pathElements.size() == 1 && !fromSelectAlias && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectInfo) {
                    // We actually allow usage of select aliases in expressions, but JPA doesn't, so we have to resolve them here
                    Expression selectExpr = ((SelectInfo) aliasInfo).getExpression();

                    if (!(selectExpr instanceof PathExpression)) {
                        throw new RuntimeException("The select expression '" + selectExpr.toString() + "' is not a simple path expression! No idea how to implicit join that.");
                    }
                    // join the expression behind a select alias once when it is encountered the first time
                    if (((PathExpression) selectExpr).getBaseNode() == null) {
                        implicitJoin(selectExpr, objectLeafAllowed, fromClause, fromSubquery, true);
                    }
                    PathExpression selectPathExpr = (PathExpression) selectExpr;
                    result = new JoinResult((JoinNode) selectPathExpr.getBaseNode(), selectPathExpr.getField());
                } else {
                    // Naked join alias usage like in "KEY(joinAlias)"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    result = new JoinResult(current, null);
                }
            } else if (!pathExpression.isUsedInCollectionFunction()) {
                result = implicitJoinSingle(current, elementExpr.toString(), objectLeafAllowed);
            } else {
                result = new JoinResult(current, elementExpr.toString());
            }

            if (fetch) {
                fetchPath(result.baseNode);
            }

            // Don't forget to update the clause dependencies!!
            if (fromClause != null) {
                updateClauseDependencies(result.baseNode, fromClause);
            }

            pathExpression.setBaseNode(result.baseNode);
            pathExpression.setField(result.field);
        } else if (expression instanceof CompositeExpression) {
            for (Expression exp : ((CompositeExpression) expression).getExpressions()) {
                implicitJoin(exp, objectLeafAllowed, fromClause, fromSubquery, fromSelectAlias);
            }
        } else if (expression instanceof FunctionExpression) {
            for (Expression exp : ((FunctionExpression) expression).getExpressions()) {
                implicitJoin(exp, objectLeafAllowed, fromClause, fromSubquery, fromSelectAlias);
            }
        }
    }

    private boolean isSingleValuedAssociationId(JoinNode parent, List<PathElementExpression> pathElements) {
        int maybeSingularAssociationIndex = pathElements.size() - 2;
        int maybeSingularAssociationIdIndex = pathElements.size() - 1;
        ManagedType<?> baseType;
        Attribute<?, ?> maybeSingularAssociation;

        if (parent == null) {
            // This is the case when we have exactly 2 path elements
            AliasInfo a = aliasManager.getAliasInfo(pathElements.get(maybeSingularAssociationIndex).toString());

            if (a == null) {
                // if the path element is no alias we can do some optimizations
                baseType = metamodel.managedType(rootNode.getPropertyClass());
                maybeSingularAssociation = baseType.getAttribute(pathElements.get(maybeSingularAssociationIndex).toString());
            } else if (!(a instanceof JoinAliasInfo)) {
                throw new IllegalArgumentException("Can't dereference select alias in the expression!");
            } else {
                // If there is a JoinAliasInfo for the path element, we have to use the alias
                // So we return false in order to signal that a normal implicit join should be done
                return false;
//                JoinNode maybeSingularAssociationJoinNode = ((JoinAliasInfo) a).getJoinNode();
//                ManagedType<?> baseType = metamodel.managedType(maybeSingularAssociationJoinNode.getParent().getPropertyClass());
//                maybeSingularAssociation = baseType.getAttribute(maybeSingularAssociationJoinNode.getParentTreeNode().getRelationName());
            }

        } else {
            baseType = metamodel.managedType(parent.getPropertyClass());
            maybeSingularAssociation = baseType.getAttribute(pathElements.get(maybeSingularAssociationIndex).toString());
        }

        if (maybeSingularAssociation == null) {
            return false;
        }

        if (maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.MANY_TO_ONE // TODO: to be able to support ONE_TO_ONE we need to know where the FK is
                //                && maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE
                ) {
            return false;
        }

        Class<?> maybeSingularAssociationClass = resolveFieldClass(baseType.getJavaType(), maybeSingularAssociation);
        ManagedType<?> maybeSingularAssociationType = metamodel.managedType(maybeSingularAssociationClass);
        Attribute<?, ?> maybeSingularAssociationId = maybeSingularAssociationType.getAttribute(pathElements.get(maybeSingularAssociationIdIndex).toString());

        if (!(maybeSingularAssociationId instanceof SingularAttribute<?, ?>)) {
            return false;
        }

        return ((SingularAttribute<?, ?>) maybeSingularAssociationId).isId();
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
                sb.append(indexPathExpr.getField());
            }
        } else {
            sb.append('_');
            sb.append(indexExpr.toString().replaceAll("\\.", "_"));
        }

        return sb.toString();
    }

    private EqPredicate getArrayExpressionPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        PathExpression keyPath = new PathExpression(new ArrayList<PathElementExpression>(), true);
        keyPath.getExpressions().add(new PropertyExpression(joinNode.getAliasInfo().getAlias()));
        keyPath.setBaseNode(joinNode);
        FunctionExpression keyExpression = new FunctionExpression("KEY", Arrays.asList((Expression) keyPath));
        return new EqPredicate(keyExpression, arrayExpr.getIndex());
    }

    private void registerDependencies(final JoinNode joinNode, Predicate withPredicate) {
        withPredicate.accept(new VisitorAdapter() {
            @Override
            public void visit(PathExpression pathExpr) {
                // prevent loop dependencies to the same join node
                if (pathExpr.getBaseNode() != joinNode) {
                    joinNode.getDependencies().add((JoinNode) pathExpr.getBaseNode());
                }
            }
        });
    }

    private void generateAndApplyWithPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        EqPredicate valueKeyFilterPredicate = getArrayExpressionPredicate(joinNode, arrayExpr);

        if (joinNode.getWithPredicate() != null) {
            AndPredicate currentPred = joinNode.getWithPredicate();

            // Only add the predicate if it isn't contained yet
            if (!findPredicate(currentPred, valueKeyFilterPredicate)) {
                currentPred.getChildren().add(valueKeyFilterPredicate);
                registerDependencies(joinNode, currentPred);
            }
        } else {
            AndPredicate withAndPredicate = new AndPredicate();
            withAndPredicate.getChildren().add(valueKeyFilterPredicate);
            joinNode.setWithPredicate(withAndPredicate);
            registerDependencies(joinNode, withAndPredicate);
        }
    }

    private JoinNode implicitJoin(JoinNode current, PathExpression pathExpression, int start, int end) {
        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        PathElementExpression elementExpr;

        for (int i = start; i < end; i++) {
            AliasInfo aliasInfo;
            elementExpr = pathElements.get(i);
            if (elementExpr instanceof ArrayExpression) {
                ArrayExpression arrayExpr = (ArrayExpression) elementExpr;
                String joinRelationName = arrayExpr.getBase().toString();

                current = current == null ? rootNode : current;
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
                    generateAndApplyWithPredicate(current, arrayExpr);
                } else {
                    String joinAlias = getJoinAlias(arrayExpr);
                    current = createOrUpdateNode(current, joinRelationName, joinAlias, null, true, false);
                    generateAndApplyWithPredicate(current, arrayExpr);
                }
            } else if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectInfo) {
                    throw new IllegalArgumentException("Can't dereference a select alias");
                } else {
                    // Join alias usage like in "joinAlias.relationName"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                }
            } else {
                current = implicitJoinSingle(current, elementExpr.toString());
            }
        }

        return current;
    }

    private JoinNode implicitJoinSingle(JoinNode baseNode, String attributeName) {
        if (baseNode == null) {
            // When no base is given, check if the attribute name is an alias
            AliasInfo aliasInfo = aliasManager.getAliasInfoForBottomLevel(attributeName);
            if (aliasInfo != null && aliasInfo instanceof JoinAliasInfo) {
                // if it is, we can just return the join node
                return ((JoinAliasInfo) aliasInfo).getJoinNode();
            }
        }

        // If we have no base node, root is assumed
        if (baseNode == null) {
            baseNode = rootNode;
        }

        // check if the path is joinable, assuming it is relative to the root (implicit root prefix)
        return createOrUpdateNode(baseNode, attributeName, null, null, true, true);
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName, boolean objectLeafAllowed) {
        JoinNode newBaseNode;
        String field;
        // The given path may be relative to the root or it might be an alias
        if (objectLeafAllowed) {
            newBaseNode = implicitJoinSingle(baseNode, attributeName);
            // check if the last path element was also joined
            if (newBaseNode != baseNode) {
                field = null;
            } else {
                field = attributeName;
            }
        } else {
            Class baseNodeType = baseNode.getPropertyClass();
            Attribute attr = metamodel.managedType(baseNodeType).getAttribute(attributeName);
            if (attr == null) {
                throw new IllegalArgumentException("Field with name "
                        + attributeName + " was not found within class "
                        + baseNodeType.getName());
            }
            if (isJoinable(attr)) {
                throw new IllegalArgumentException("No object leaf allowed but " + attributeName + " is an object leaf");
            }
            newBaseNode = baseNode;
            field = attributeName;
        }
        return new JoinResult(newBaseNode, field);
    }

    private void updateClauseDependencies(JoinNode baseNode, ClauseType clauseDependency) {
        JoinNode current = baseNode;
        while (current != null) {
            // setSelectOnlyFalse for all JoinNodes that are used in the WITH clause of the current node
            for (JoinNode dependency : current.getDependencies()) {
                updateClauseDependencies(dependency, clauseDependency);
            }

            current.getClauseDependencies().add(clauseDependency);
            current = current.getParent();
        }
    }

    private boolean isJoinable(Attribute attr) {
        return attr.isCollection()
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    private Class<?> resolveFieldClass(Class<?> baseClass, Attribute attr) {
        if (attr.isCollection()) {
            PluralAttribute<?, ?, ?> collectionAttr = (PluralAttribute<?, ?, ?>) attr;
            
            if (collectionAttr.getCollectionType() == PluralAttribute.CollectionType.MAP) {
                if (attr.getJavaMember() instanceof Method) {
                    return ReflectionUtils.getResolvedMethodReturnTypeArguments(baseClass, (Method) attr.getJavaMember())[1];
                } else {
                    return ReflectionUtils.getResolvedFieldTypeArguments(baseClass, (Field) attr.getJavaMember())[1];
                }
            } else {
                if (attr.getJavaMember() instanceof Method) {
                    return ReflectionUtils.getResolvedMethodReturnTypeArguments(baseClass, (Method) attr.getJavaMember())[0];
                } else {
                    return ReflectionUtils.getResolvedFieldTypeArguments(baseClass, (Field) attr.getJavaMember())[0];
                }
            }
        }

        if (attr.getJavaMember() instanceof Method) {
            return ReflectionUtils.getResolvedMethodReturnType(baseClass, (Method) attr.getJavaMember());
        } else {
            return ReflectionUtils.getResolvedFieldType(baseClass, (Field) attr.getJavaMember());
        }
    }

    private JoinType getModelAwareType(JoinNode baseNode, Attribute attr) {
        if (baseNode.getType() == JoinType.LEFT) {
            return JoinType.LEFT;
        }
        
        if ((attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE)
                && ((SingularAttribute) attr).isOptional() == false) {
            return JoinType.INNER;
        } else {
            return JoinType.LEFT;
        }
    }
    
    private JoinNode createOrUpdateNode(JoinNode baseNode, String joinRelationName, String alias, JoinType joinType, boolean implicit, boolean defaultJoin) {
        Class<?> baseNodeType = baseNode.getPropertyClass();
        ManagedType type = metamodel.managedType(baseNodeType);
        Attribute attr = type.getAttribute(joinRelationName);
        if (attr == null) {
            throw new IllegalArgumentException("Field with name "
                    + joinRelationName + " was not found within class "
                    + baseNodeType.getName());
        }
        Class<?> resolvedFieldClass = resolveFieldClass(baseNodeType, attr);

        if (!isJoinable(attr)) {
            LOG.fine(new StringBuilder("Field with name ").append(joinRelationName).append(" of class ").append(baseNodeType.getName()).append(
                    " is parseable and therefore it has not to be fetched explicitly.").toString());
            return baseNode;
        }

        if (implicit) {
            String aliasToUse = alias == null ? joinRelationName : alias;
            alias = aliasManager.generatePostfixedAlias(aliasToUse);
        }
        
        if (joinType == null) {
            joinType = getModelAwareType(baseNode, attr);
        }

        JoinNode newNode = getOrCreate(baseNode, joinRelationName, resolvedFieldClass, alias, joinType, "Ambiguous implicit join", implicit, attr.isCollection(), defaultJoin);

        return newNode;
    }

    private void checkAliasIsAvailable(String alias, String currentJoinPath, String errorMessage) {
        AliasInfo oldAliasInfo = aliasManager.getAliasInfoForBottomLevel(alias);
        if (oldAliasInfo instanceof SelectInfo) {
            throw new IllegalStateException("Alias [" + oldAliasInfo.getAlias() + "] already used as select alias");
        }
        JoinAliasInfo oldJoinAliasInfo = (JoinAliasInfo) oldAliasInfo;
        if (oldJoinAliasInfo != null) {
            if (!oldJoinAliasInfo.getAbsolutePath().equals(currentJoinPath)) {
                throw new IllegalArgumentException(errorMessage);
            } else {
                throw new RuntimeException("Probably a programming error if this happens. An alias[" + alias + "] for the same join path[" + currentJoinPath
                        + "] is available but the join node is not!");
            }
        }
    }

    private JoinNode getOrCreate(JoinNode baseNode, String joinRelationName, Class<?> joinRelationClass, String alias, JoinType type, String errorMessage, boolean implicit, boolean collection, boolean defaultJoin) {
        JoinTreeNode treeNode = baseNode.getOrCreateTreeNode(joinRelationName, collection);
        JoinNode node = treeNode.getJoinNode(alias, defaultJoin);
        String currentJoinPath = baseNode.getAliasInfo().getAbsolutePath() + "." + joinRelationName;
        if (node == null) {
            // a join node for the join relation does not yet exist
            checkAliasIsAvailable(alias, currentJoinPath, errorMessage);

            // the alias might have to be postfixed since it might already exist in parent queries
            if (implicit && aliasManager.getAliasInfo(alias) != null) {
                alias = aliasManager.generatePostfixedAlias(alias);
            }

            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, aliasManager);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = new JoinNode(baseNode, treeNode, newAliasInfo, type, joinRelationClass);
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
                    node.setType(type);

                    aliasManager.registerAliasInfo(nodeAliasInfo);
                } else if (!nodeAliasInfo.isImplicit() && !implicit) {
                    throw new IllegalArgumentException("Alias conflict [" + nodeAliasInfo.getAlias() + "=" + nodeAliasInfo.getAbsolutePath() + ", " + alias + "=" + currentJoinPath
                            + "]");
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
            AndPredicate andPredicate = node.getWithPredicate();

            if (findPredicate(andPredicate, pred)) {
                return node;
            }
        }

        return null;
    }

    private boolean findPredicate(AndPredicate andPredicate, Predicate pred) {
        if (andPredicate != null) {
            for (Predicate p : andPredicate.getChildren()) {
                if (p.equals(pred)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean startsAtRootAlias(String path) {
        AliasInfo rootAliasInfo = rootNode.getAliasInfo();
        return path.startsWith(rootAliasInfo.getAlias()) && path.length() > rootAliasInfo.getAlias().length() && path.charAt(rootAliasInfo.getAlias().length()) == '.';
    }

    /**
     * Base node will NOT be fetched
     *
     * @param baseNode
     * @param path
     */
    private void fetchPath(JoinNode node) {
        JoinNode currentNode = node;
        while (currentNode != null) {
            currentNode.setFetch(true);
            currentNode = currentNode.getParent();
        }
    }

    // TODO: needs equals-hashCode implementation
    private static class JoinResult {

        final JoinNode baseNode;
        final String field;

        public JoinResult(JoinNode baseNode, String field) {
            this.baseNode = baseNode;
            this.field = field;
        }
    }

    private class JoinOnBuilderEndedListener extends PredicateBuilderEndedListenerImpl {

        private JoinNode joinNode;

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            super.onBuilderEnded(builder);
            joinNode.setWithPredicate((AndPredicate) builder.getPredicate());
        }
    }
}
