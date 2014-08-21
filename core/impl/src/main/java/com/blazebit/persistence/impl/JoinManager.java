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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.Predicate.Visitor;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final JoinNode rootNode;
    // Maps alias to join path with the root as base
    private final JoinAliasInfo rootAliasInfo;
    // root entity class
    private final Class<?> clazz;
    private final String joinRestrictionKeyword;
    private final AliasManager aliasManager;
    private final BaseQueryBuilder<?, ?> aliasOwner;
    private final Metamodel metamodel; // needed for model-aware joins
    private final JoinManager parent;
    private final JoinOnBuilderEndedListener joinOnBuilderListener;
    private SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    private static enum JoinClauseBuildMode {

        NORMAL,
        COUNT,
        ID
    };

    JoinManager(String rootAlias, Class<?> clazz, QueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, JPAInfo jpaInfo, AliasManager aliasManager, BaseQueryBuilder<?, ?> aliasOwner, Metamodel metamodel, JoinManager parent) {
        super(queryGenerator, parameterManager);
        if (rootAlias == null) {
            rootAlias = aliasManager.generatePostfixedAlias(clazz.getSimpleName().toLowerCase());
        }
        this.rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, aliasOwner);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(this.rootAliasInfo);

        this.rootNode = new JoinNode(null, rootAliasInfo, null, clazz, false);
        this.rootAliasInfo.setJoinNode(rootNode);
        this.clazz = clazz;
        this.aliasManager = aliasManager;
        this.aliasOwner = aliasOwner;
        this.metamodel = metamodel;
        this.parent = parent;
        this.joinRestrictionKeyword = " " + jpaInfo.getOnClause() + " ";
        this.joinOnBuilderListener = new JoinOnBuilderEndedListener();
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    String getRootAlias() {
        return rootAliasInfo.getAlias();
    }

    JoinManager getParent() {
        return parent;
    }

    void setSubqueryInitFactory(SubqueryInitiatorFactory subqueryInitFactory) {
        this.subqueryInitFactory = subqueryInitFactory;
    }

    void buildJoins(StringBuilder sb, boolean includeSelect) {
        applyJoins(sb, rootAliasInfo, rootNode.getNodes(), includeSelect);
    }

    void verifyBuilderEnded() {
        joinOnBuilderListener.verifyBuilderEnded();
    }

    void acceptVisitor(Visitor v) {
        rootNode.accept(v);
    }

    void applyTransformer(ExpressionTransformer transformer) {
        rootNode.accept(new PredicateManager.TransformationVisitor(transformer));
    }

    StringBuilder generateWhereClauseConjuncts(boolean includeSelect) {
        StringBuilder sb = new StringBuilder();
        generateWhereClauseConjuncts(sb, rootNode, null, includeSelect);
        return sb;
    }

    private void generateWhereClauseConjuncts(StringBuilder sb, JoinNode node, String relation, boolean includeSelect) {
        if (usesKeyInWithPredicate(node, includeSelect)) {
            // Safe because root has no with predicate
            ManagedType<?> t = metamodel.managedType(node.getParent().getPropertyClass());
            Attribute<?, ?> attr = t.getAttribute(relation);

            if (attr.isCollection() && ((AnnotatedElement) attr.getJavaMember()).getAnnotation(CollectionTable.class) != null) {
                if (sb.length() > 0) {
                    sb.append(" AND ");
                }

                Type<?> elementType = ((PluralAttribute<?, ?, ?>) attr).getElementType();

                // Unfortunately we have to branch here because embeddable IS NOT NULL results in a runtime error
                if (elementType instanceof EntityType) {
                    sb.append(node.getAliasInfo().getAlias());
                    sb.append(" IS NOT NULL");
                } else if (elementType instanceof EmbeddableType) {
                    SortedSet<Attribute<?, ?>> attributes = new TreeSet<Attribute<?, ?>>(new Comparator<Attribute<?, ?>>() {

                        @Override
                        public int compare(Attribute<?, ?> o1, Attribute<?, ?> o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    attributes.addAll(((EmbeddableType<?>) elementType).getSingularAttributes());
                    boolean first = true;
                    for (Attribute<?, ?> elementAttribute : attributes) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(" AND ");
                        }

                        sb.append(node.getAliasInfo().getAlias());
                        sb.append('.');
                        sb.append(elementAttribute.getName());
                        sb.append(" IS NOT NULL");
                    }
                }
            }
        }
        for (Map.Entry<String, JoinTreeNode> treeNodeEntry : node.getNodes().entrySet()) {
            String subRelation = treeNodeEntry.getKey();
            JoinTreeNode treeNode = treeNodeEntry.getValue();
            for (JoinNode n : treeNode.getJoinNodes().values()) {
                generateWhereClauseConjuncts(sb, n, subRelation, includeSelect);
            }
        }
    }

    private boolean usesKeyInWithPredicate(JoinNode node, boolean includeSelect) {
        if (!includeSelect && node.isSelectOnly()) {
            return false;
        }
        if (node.getWithPredicate() == null || node.getWithPredicate().getChildren().isEmpty()) {
            return false;
        }

        String keyExpressionString = "KEY(" + node.getAliasInfo().getAlias() + ")";

        for (Predicate p : node.getWithPredicate().getChildren()) {
            if (p instanceof EqPredicate) {
                EqPredicate eq = (EqPredicate) p;
                if (keyExpressionString.equals(eq.getLeft().toString())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, Map<String, JoinTreeNode> nodes, boolean includeSelect) {
        for (Map.Entry<String, JoinTreeNode> nodeEntry : nodes.entrySet()) {
            String relation = nodeEntry.getKey();
            JoinTreeNode treeNode = nodeEntry.getValue();

            for (JoinNode node : treeNode.getJoinNodes().values()) {
                if (includeSelect == false && node.isSelectOnly() == true) {
                    continue;
                }

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
                sb.append(joinBase.getAlias()).append('.').append(relation).append(' ').append(node.getAliasInfo().getAlias());

                if (node.getWithPredicate() != null && !node.getWithPredicate().getChildren().isEmpty()) {
                    sb.append(joinRestrictionKeyword);
                    queryGenerator.setQueryBuffer(sb);
                    node.getWithPredicate().accept(queryGenerator);
                }
                if (!node.getNodes().isEmpty()) {
                    applyJoins(sb, node.getAliasInfo(), node.getNodes(), includeSelect);
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

        if (parent != null && aliasInfo.getAliasOwner() == parent.aliasOwner) {
            // the alias exists but originates from the parent query builder

            // an external select alias must not be dereferenced
            if (aliasInfo instanceof SelectManager.SelectInfo) {
                throw new ExternalAliasDereferencingException("Start alias [" + startAlias + "] of path [" + path.toString() + "] is external and must not be dereferenced");
            }

            // the alias is external so we do not have to treat it
            return true;
        } else if (aliasInfo.getAliasOwner() == aliasOwner) {
            // the alias originates from the current query builder an is therefore not external
            return false;
        } else {
            throw new IllegalStateException("Alias [" + aliasInfo.getAlias() + "] originates from an unknown query");
        }
    }

    private boolean isSkipableSelectAlias(String path, boolean fromSelect, boolean fromSubquery) {
        int firstDotIndex = path.indexOf('.');
        boolean singlePathElement;
        String startAlias;
        if (firstDotIndex != -1) {
            singlePathElement = false;
            startAlias = path.substring(0, firstDotIndex);
        } else {
            singlePathElement = true;
            startAlias = path;
        }
        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return false;
        }

        if (aliasInfo instanceof SelectManager.SelectInfo && !fromSelect && !fromSubquery) {
            // select alias
            if (!singlePathElement) {
                throw new IllegalStateException("Path starting with select alias not allowed");
            }

            // do not join select aliases
            return true;
        }
        return false;
    }

    <X> JoinOnBuilder<X> joinOn(X result, String path, String alias, JoinType type) {
        joinOnBuilderListener.joinNode = join(path, alias, type, false, false);
        return joinOnBuilderListener.startBuilder(new JoinOnBuilderImpl<X>(result, joinOnBuilderListener, parameterManager, expressionFactory, subqueryInitFactory));
    }

    JoinNode join(String path, String alias, JoinType type, boolean fetch, boolean defaultJoin) {
        Expression expr = expressionFactory.createSimpleExpression(path);
        PathExpression pathExpression;
        if (expr instanceof PathExpression) {
            pathExpression = (PathExpression) expr;
        } else {
            throw new IllegalArgumentException("Join path [" + path + "] is not a path");
        }

        if (isExternal(pathExpression) || isSkipableSelectAlias(path, false, false)) {
            throw new IllegalArgumentException("No external path or select alias allowed in join path");
        }

        JoinNode current;

        if (startsAtRootAlias(path)) {
            current = implicitJoin(rootNode, pathExpression, false, 1);
        } else {
            current = implicitJoin(null, pathExpression, false, 0);
        }

        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);

        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        } else {
            current = current == null ? rootNode : current;
            current = createOrUpdateNode(current, elementExpr.toString(), alias, type, false, true, defaultJoin);
        }

        if (fetch) {
            fetchPath(current);
        }

        return current;
    }

    void implicitJoin(Expression expression, boolean objectLeafAllowed, boolean fromSelect, boolean fromSubquery, boolean fromSelectAlias){
        implicitJoin(expression, objectLeafAllowed, fromSelect, fromSubquery, fromSelectAlias, false);
    }
    
    void implicitJoin(Expression expression, boolean objectLeafAllowed, boolean fromSelect, boolean fromSubquery, boolean fromSelectAlias, boolean fetch) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;
            String path = pathExpression.getPath();

            // TODO: no clue about the purpose of this section
            if (isSkipableSelectAlias(path, fromSelect, fromSubquery)) {
                Expression expr = ((SelectManager.SelectInfo) aliasManager.getAliasInfo(path)).getExpression();

                // this check is necessary to prevent infinite recursion in the case of e.g. SELECT name AS name
                if (!fromSelectAlias) {
                    // we have to do this implicit join because we might have to adjust the selectOnly flag in the referenced join nodes
                    implicitJoin(expr, true, fromSelect, fromSubquery, true);
                }
                return;
            } else if (isExternal(pathExpression)) {

                // try to set base node and field for the external expression based
                // on existing joins in the super query
                //TODO: the usage of fromSelect might not be correct here    
                parent.implicitJoin(pathExpression, true, fromSelect, true, fromSelectAlias);
                return;
            }

            // First try to implicit join indices of array expressions since we will need their base nodes
            for (PathElementExpression pathElem : pathExpression.getExpressions()) {
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, fromSelect, fromSubquery, fromSelectAlias);
                }
            }

            JoinNode current = implicitJoin(null, pathExpression, fromSelect, 0);
            // current might be null if pathExpression.size() == 1
            current = current == null ? rootNode : current;

            JoinResult result;
            AliasInfo aliasInfo;
            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);

            if (elementExpr instanceof ArrayExpression) {
                ArrayExpression arrayExpr = (ArrayExpression) elementExpr;
                String joinRelationName = arrayExpr.getBase().toString();

                // Find a node by a predicate match
                JoinNode matchingNode;

                if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(joinRelationName)) != null) {
                    // The first node is allowed to be a join alias
                    if (aliasInfo instanceof SelectManager.SelectInfo) {
                        throw new IllegalArgumentException("Illegal reference to the select alias '" + joinRelationName + "'");
                    }
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    applyWithPredicate(current, arrayExpr);

                    // Don't forget to update the fromSelect!!
                    if (!fromSelect) {
                        setSelectOnlyFalse(current);
                        current.setSelectOnly(false);
                    }
                } else if ((matchingNode = findNode(current, joinRelationName, arrayExpr)) != null) {
                    // We found a join node for the same join relation with the same array expression predicate
                    current = matchingNode;

                    // Don't forget to update the fromSelect!!
                    if (!fromSelect) {
                        setSelectOnlyFalse(current);
                        current.setSelectOnly(false);
                    }
                } else {
                    String joinAlias = getJoinAlias(arrayExpr);
                    current = createOrUpdateNode(current, joinRelationName, joinAlias, null, true, fromSelect, false);
                    applyWithPredicate(current, arrayExpr);
                }

                result = new JoinResult(current, null);
            } else if (pathElements.size() == 1 && !fromSelectAlias && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectManager.SelectInfo) {
                    // We actually allow usage of select aliases in expressions, but JPA doesn't, so we have to resolve them here
                    Expression selectExpr = ((SelectManager.SelectInfo) aliasInfo).getExpression();

                    if (!(selectExpr instanceof PathExpression)) {
                        throw new RuntimeException("The select expression '" + selectExpr.toString() + "' is not a simple path expression! No idea how to implicit join that.");
                    }
                    //TODO: I think this is redundant
                    implicitJoin(selectExpr, objectLeafAllowed, fromSelect, fromSubquery, true);
                    PathExpression selectPathExpr = (PathExpression) selectExpr;
                    result = new JoinResult((JoinNode) selectPathExpr.getBaseNode(), selectPathExpr.getField());
                } else {
                    // Naked join alias usage like in "KEY(joinAlias)"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    result = new JoinResult(current, null);
                }
            } else if(!pathExpression.isUsedInCollectionFunction()) {
                result = implicitJoinSingle(current, elementExpr.toString(), objectLeafAllowed, fromSelect);
            } else{
                result = new JoinResult(current, elementExpr.toString());
            }

            if (pathExpression.isUsedInCollectionFunction() && result.field == null) {
                // when having collection functions, we need field, using just a join alias is wrong
                if (elementExpr instanceof ArrayExpression) {
                    throw new IllegalArgumentException("Array expression in collection function?? Are you crazy?");
                } else {
                    result = new JoinResult(result.baseNode.getParent(), elementExpr.toString());
                }
            }

            if (fetch) {
                fetchPath(result.baseNode);
            }

            pathExpression.setBaseNode(result.baseNode);
            pathExpression.setField(result.field);
        } else if (expression instanceof CompositeExpression) {
            for (Expression exp : ((CompositeExpression) expression).getExpressions()) {
                implicitJoin(exp, objectLeafAllowed, fromSelect, fromSubquery, fromSelectAlias);
            }
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
                sb.append(indexPathExpr.getField());
            }
        } else {
            sb.append('_');
            sb.append(indexExpr.toString().replaceAll("\\.", "_"));
        }

        return sb.toString();
    }

    private Predicate getArrayExpressionPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        CompositeExpression keyExpression = new CompositeExpression(new ArrayList<Expression>());
        keyExpression.getExpressions().add(new FooExpression("KEY("));

        PathExpression keyPath = new PathExpression(new ArrayList<PathElementExpression>(), true);
        keyPath.getExpressions().add(new PropertyExpression(joinNode.getAliasInfo().getAlias()));
        keyPath.setBaseNode(joinNode);
        keyExpression.getExpressions().add(keyPath);
        keyExpression.getExpressions().add(new FooExpression(")"));
        EqPredicate valueKeyFilterPredicate = new EqPredicate(keyExpression, arrayExpr.getIndex());
        return valueKeyFilterPredicate;
    }

    private void applyWithPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        CompositeExpression keyExpression = new CompositeExpression(new ArrayList<Expression>());
        keyExpression.getExpressions().add(new FooExpression("KEY("));

        PathExpression keyPath = new PathExpression(new ArrayList<PathElementExpression>(), true);
        keyPath.getExpressions().add(new PropertyExpression(joinNode.getAliasInfo().getAlias()));
        keyPath.setBaseNode(joinNode);
        keyExpression.getExpressions().add(keyPath);
        keyExpression.getExpressions().add(new FooExpression(")"));
        EqPredicate valueKeyFilterPredicate = new EqPredicate(keyExpression, arrayExpr.getIndex());

        if (joinNode.getWithPredicate() != null) {
            AndPredicate currentPred = joinNode.getWithPredicate();

            // Only add the predicate if it isn't contained yet
            if (!findPredicate(currentPred, valueKeyFilterPredicate)) {
                currentPred.getChildren().add(valueKeyFilterPredicate);
            }
        } else {
            AndPredicate withAndPredicate = new AndPredicate();
            withAndPredicate.getChildren().add(valueKeyFilterPredicate);
            joinNode.setWithPredicate(withAndPredicate);
        }
    }

    private JoinNode implicitJoin(JoinNode current, PathExpression pathExpression, boolean fromSelect, int start) {
        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        PathElementExpression elementExpr;

        for (int i = start; i < pathElements.size() - 1; i++) {
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
                } else if (i == start && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(joinRelationName)) != null) {
                    // The first node is allowed to be a join alias
                    if (aliasInfo instanceof SelectManager.SelectInfo) {
                        throw new IllegalArgumentException("Illegal reference to the select alias '" + joinRelationName + "'");
                    }
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    applyWithPredicate(current, arrayExpr);
                } else {
                    String joinAlias = getJoinAlias(arrayExpr);
                    current = createOrUpdateNode(current, joinRelationName, joinAlias, null, true, fromSelect, false);
                    applyWithPredicate(current, arrayExpr);
                }
            } else if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectManager.SelectInfo) {
                    throw new IllegalArgumentException("Can't dereference a select alias");
                } else {
                    // Join alias usage like in "joinAlias.relationName"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                }
            } else {
                current = implicitJoinSingle(current, elementExpr.toString(), fromSelect);
            }
        }

        return current;
    }

    private JoinNode implicitJoinSingle(JoinNode baseNode, String attributeName, boolean fromSelect) {
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
        return createOrUpdateNode(baseNode, attributeName, null, null, true, fromSelect, true);
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName, boolean objectLeafAllowed, boolean fromSelect) {
        JoinNode newBaseNode;
        String field;
        // The given path may be relative to the root or it might be an alias
        if (objectLeafAllowed) {
            newBaseNode = implicitJoinSingle(baseNode, attributeName, fromSelect);
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

    private void setSelectOnlyFalse(JoinNode baseNode) {
        JoinNode current = baseNode;
        while (current != null) {
            current.setSelectOnly(false);
            current = current.getParent();
        }
    }

    private boolean isJoinable(Attribute attr) {
        return attr.isCollection()
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    private Class<?> resolveFieldClass(Attribute attr) {
        if (attr.isCollection()) {
            return ((PluralAttribute) attr).getElementType().getJavaType();
        }

        return attr.getJavaType();
    }

    private JoinType getModelAwareType(Attribute attr) {
        if ((attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE)
                && ((SingularAttribute) attr).isOptional() == false) {
            return JoinType.INNER;
        } else {
            return JoinType.LEFT;
        }
    }

    private JoinNode createOrUpdateNode(JoinNode baseNode, String joinRelationName, String alias, JoinType joinType, boolean implicit, boolean fromSelect, boolean defaultJoin) {
        if (!fromSelect) {
            // updated base path nodes
            setSelectOnlyFalse(baseNode);
        }

        Class<?> baseNodeType = baseNode.getPropertyClass();
        ManagedType type = metamodel.managedType(baseNodeType);
        Attribute attr = type.getAttribute(joinRelationName);
        if (attr == null) {
            throw new IllegalArgumentException("Field with name "
                    + joinRelationName + " was not found within class "
                    + baseNodeType.getName());
        }
        Class<?> resolvedFieldClass = resolveFieldClass(attr);

        if (!isJoinable(attr)) {
            LOG.fine(new StringBuilder("Field with name ").append(joinRelationName).append(" of class ").append(baseNodeType.getName()).append(
                    " is parseable and therefore it has not to be fetched explicitly.").toString());
            return baseNode;
        }

        if (alias == null) {
            if (implicit) {
                alias = aliasManager.generatePostfixedAlias(joinRelationName);
            } else {
                // default alias
                alias = joinRelationName;
            }
        }
        if (joinType == null) {
            joinType = getModelAwareType(attr);
        }

        JoinNode newNode = getOrCreate(baseNode, joinRelationName, resolvedFieldClass, alias, joinType, "Ambiguous implicit join", implicit, attr.isCollection(), defaultJoin);

        if (!fromSelect) {
            newNode.setSelectOnly(false);
        }

        return newNode;
    }

    private void checkAliasIsAvailable(String alias, String currentJoinPath, String errorMessage) {
        AliasInfo oldAliasInfo = aliasManager.getAliasInfoForBottomLevel(alias);
        if (oldAliasInfo instanceof SelectManager.SelectInfo) {
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
        JoinTreeNode treeNode = baseNode.getOrCreateTreeNode(joinRelationName);
        JoinNode node = treeNode.getJoinNode(alias, defaultJoin);
        String currentJoinPath = baseNode.getAliasInfo().getAbsolutePath() + "." + joinRelationName;
        if (node == null) {
            // a join node for the join relation does not yet exist
            checkAliasIsAvailable(alias, currentJoinPath, errorMessage);

            // the alias might have to be postfixed since it might already exist in parent queries
            if (implicit) {
                alias = aliasManager.generatePostfixedAlias(alias);
            }

            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, aliasOwner);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = new JoinNode(baseNode, newAliasInfo, type, joinRelationClass, collection);
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
