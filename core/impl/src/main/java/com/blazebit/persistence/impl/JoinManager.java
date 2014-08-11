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

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinOnOrBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.Predicate.Visitor;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.predicate.PredicateBuilderEndedListener;
import java.util.Map;
import java.util.logging.Logger;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

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

    public static enum JoinClauseBuildMode {

        NORMAL, COUNT, ID
    };

    public JoinManager(String rootAlias, Class<?> clazz, QueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, JPAInfo jpaInfo, AliasManager aliasManager, BaseQueryBuilder<?, ?> aliasOwner, Metamodel metamodel, JoinManager parent) {
        super(queryGenerator, parameterManager);
        if (rootAlias == null) {
            rootAlias = aliasManager.generatePostfixedAlias(clazz.getSimpleName().toLowerCase());
        }
        this.rootAliasInfo = new JoinAliasInfo(rootAlias, "", true, aliasOwner);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(this.rootAliasInfo);

        this.rootNode = new JoinNode(rootAliasInfo, null, false, null, false);
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

    void buildJoins(boolean includeSelect, StringBuilder sb) {
        applyJoins(sb, rootAliasInfo, rootNode.getNodes(), includeSelect);
    }

    void verifyBuilderEnded() {
        joinOnBuilderListener.verifyBuilderEnded();
    }

    void acceptVisitor(Visitor v) {
        acceptVisitorRecursive(v, rootNode);
    }

    void applyTransformer(ExpressionTransformer transformer) {
        Visitor transformationVisitor = new PredicateManager.TransformationVisitor(transformer);
        acceptVisitorRecursive(transformationVisitor, rootNode);
    }

    private void acceptVisitorRecursive(Visitor visitor, JoinNode node) {
        if (node.getWithPredicate() != null) {

            node.getWithPredicate().accept(visitor);
        }
        for (JoinNode child : node.getNodes().values()) {
            acceptVisitorRecursive(visitor, child);
        }
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, Map<String, JoinNode> nodes, boolean includeSelect) {
        for (Map.Entry<String, JoinNode> nodeEntry : nodes.entrySet()) {
            String relation = nodeEntry.getKey();
            JoinNode node = nodeEntry.getValue();
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

            if (node.getWithPredicate() != null) {
                sb.append(joinRestrictionKeyword);
                queryGenerator.setQueryBuffer(sb);
                node.getWithPredicate().accept(queryGenerator);
            }
            if (!node.getNodes().isEmpty()) {
                applyJoins(sb, node.getAliasInfo(), node.getNodes(), includeSelect);
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

            // check for unsupported collection accesses in the path
            JoinAliasInfo joinAliasInfo = (JoinAliasInfo) aliasInfo;

            JoinNode aliasJoinNode;
            if (joinAliasInfo.getAbsolutePath().isEmpty()) {
                aliasJoinNode = parent.rootNode;
            } else {
                aliasJoinNode = parent.findNode(joinAliasInfo.getAbsolutePath());
            }

            if (aliasJoinNode.isCollection()) {
                throw new UnsupportedOperationException("Unsupported external collection access [" + aliasJoinNode.getAliasInfo().getAbsolutePath() + "]");
            }
            for (int i = 1; i < path.getExpressions().size(); i++) {
                PathElementExpression pathElem = path.getExpressions().get(i);
                String pathElemStr;
                if (pathElem instanceof ArrayExpression) {
                    pathElemStr = ((ArrayExpression) pathElem).getBase().toString();
                } else {
                    pathElemStr = pathElem.toString();
                }
                aliasJoinNode = parent.findNode(aliasJoinNode, pathElemStr);
                if (aliasJoinNode != null && aliasJoinNode.isCollection()) {
                    throw new UnsupportedOperationException("Unsupported external collection access [" + aliasJoinNode.getAliasInfo().getAbsolutePath() + "]");
                }
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
        joinOnBuilderListener.joinNode = join(path, alias, type, false);
        return joinOnBuilderListener.startBuilder(new JoinOnBuilderImpl<X>(result, joinOnBuilderListener, parameterManager, expressionFactory, subqueryInitFactory));
    }

    JoinNode join(String path, String alias, JoinType type, boolean fetch) {
        Expression expr = expressionFactory.createSimpleExpression(path);
        PathExpression pathExpr;
        if (expr instanceof PathExpression) {
            pathExpr = (PathExpression) expr;
        } else {
            throw new IllegalArgumentException("Join path [" + path + "] is not a path");
        }

        if (isExternal(pathExpr) || isSkipableSelectAlias(path, false, false)) {
            throw new IllegalArgumentException("No external path or select alias allowed in join path");
        }
        String normalizedPath = normalizePath(path);
        if (startsAtRootAlias(path)) {
            // The given path is relative to the root
            return createOrUpdateNode(rootNode, "", normalizedPath, alias, null, type, fetch, false, true);
        } else {
            // The path is either already normalized or uses a specific alias as base
            int dotIndex;
            if ((dotIndex = normalizedPath.indexOf('.')) != -1) {
                // We found a dot in the path, so it either uses an alias or does chained joining
                String potentialBase = normalizedPath.substring(0, dotIndex);
                AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialBase);
                if (aliasInfo instanceof SelectManager.SelectInfo) {
                    throw new RuntimeException("Cannot dereference select alias");
                }
                JoinAliasInfo potentialBaseInfo = (JoinAliasInfo) aliasInfo;
                if (potentialBaseInfo != null) {
                    // We found an alias for the first part of the path
                    String potentialBasePath = potentialBaseInfo.getAbsolutePath();
                    JoinNode aliasNode = findNode(rootNode, potentialBasePath);
                    String relativePath = normalizedPath.substring(dotIndex + 1);
                    //                    normalizedPath = potentialBasePath + '.' + relativePath;
                    JoinNode result = createOrUpdateNode(aliasNode, potentialBasePath, relativePath, alias, null, type, fetch, false, true);
                    //if fetch is true we have to fetch the whole path from aliasNode back to the root
                    if (fetch) {
                        fetchPath(rootNode, potentialBasePath);
                    }
                    return result;
                } else {
                    // The given path is relative to the root
                    return createOrUpdateNode(rootNode, "", normalizedPath, alias, null, type, fetch, false, true);
                }
            } else {
                // The given path is relative to the root
                return createOrUpdateNode(rootNode, "", normalizedPath, alias, null, type, fetch, false, true);
            }
        }
    }

    public JoinManager getParent() {
        return parent;
    }

    private String[] separateLastPathElement(String path) {
        int lastDotIndex;
        String[] result = new String[2];
        if ((lastDotIndex = path.lastIndexOf('.')) != -1) {
            result[0] = path.substring(0, lastDotIndex);
            result[1] = path.substring(lastDotIndex + 1);
            return result;
        }
        return null;
    }

    private boolean resolve(PathExpression pathExpr) {
        String path = pathExpr.toString();
        String[] pathField = path.split("\\.");
        if (startsAtRootAlias(path) && (pathField.length == 2)) {
            pathExpr.setBaseNode(rootNode);
            pathExpr.setField(pathField[1]);
            return true;
        } else if (path.equals(rootAliasInfo.getAlias())) {
            pathExpr.setBaseNode(rootNode);
            return true;
        }
        String normalized = normalizePath(path);
        JoinNode node = findNode(normalized);
        if (node != null) {
            pathExpr.setBaseNode(node);
            return true;
        }
        // last path element might be a field
        pathField = separateLastPathElement(normalized);
        if (pathField != null) {
            node = findNode(pathField[0]);
            if (node != null) {
                pathExpr.setBaseNode(node);
                pathExpr.setField(pathField[1]);
                return true;
            }
        }
        return false;
    }

    void implicitJoin(Expression expression, boolean objectLeafAllowed, boolean fromSelect, boolean fromSubquery) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;
            String path = pathExpression.getPath();
            String normalizedPath = normalizePath(path);

            if (isSkipableSelectAlias(path, fromSelect, fromSubquery)) {
                Expression expr = ((SelectManager.SelectInfo) aliasManager.getAliasInfo(path)).getExpression();

                // this check is necessary to prevent infinite recursion in the case of e.g. SELECT name AS name
                if (!expr.toString().equals(path)) {
                    // we have to do this implicit join because we might have to adjust the selectOnly flag in the referenced join nodes
                    implicitJoin(expr, true, fromSelect, fromSubquery);
                }
                return;
            } else if (isExternal(pathExpression)) {
                // try to set base node and field for the external expression based
                // on existing joins in the super query
                if (parent != null) {
                    if (parent.resolve(pathExpression)) {
                        return;
                    }
                }
                throw new IllegalStateException("Cannot resolve external path [" + path.toString() + "]");
            }

            JoinResult result = implicitJoin(normalizedPath, objectLeafAllowed, fromSelect);

            if (pathExpression.isUsedInCollectionFunction() && result.field == null) {
                // we have to reset the field and the base node
                String absPath = result.baseNode.getAliasInfo().getAbsolutePath();

                String[] pathField;
                if ((pathField = separateLastPathElement(absPath)) == null) {
                    result = new JoinResult(rootNode, absPath);
                } else {
                    JoinNode newBaseNode = findNode(pathField[0]);
                    result = new JoinResult(newBaseNode, pathField[1]);
                }
            }
            pathExpression.setBaseNode(result.baseNode);
            pathExpression.setField(result.field);
            //also do implicit joins for array indices
            for (PathElementExpression pathElem : pathExpression.getExpressions()) {
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, fromSelect, fromSubquery);
                }
            }
        } else if (expression instanceof CompositeExpression) {
            for (Expression exp : ((CompositeExpression) expression).getExpressions()) {
                implicitJoin(exp, objectLeafAllowed, fromSelect, fromSubquery);
            }
        }
    }

    private String normalizePath(String path) {
        String normalizedPath;
        if (startsAtRootAlias(path)) {
            // The given path is relative to the root
            normalizedPath = path.substring(rootAliasInfo.getAlias().length() + 1);
        } else {
            // The path is either already normalized or uses a specific alias as base
            normalizedPath = path;
        }
        // remove array indices
        return normalizedPath.replaceAll("\\[[^\\]]+\\]", "");
    }

    JoinResult implicitJoin(String normalizedPath, boolean objectLeafAllowed, boolean fromSelect) {
        JoinNode baseNode;
        String field;
        int dotIndex;
        String[] pathField = separateLastPathElement(normalizedPath);
        if (pathField != null) {
            // First we extract the field by which should be ordered
            field = pathField[1];
            String joinPath = pathField[0];
            JoinAliasInfo potentialBaseInfo;
            if ((dotIndex = joinPath.indexOf('.')) != -1) {
                // We found a dot in the path, so it either uses an alias or does chained joining
                String potentialBase = normalizedPath.substring(0, dotIndex);
                AliasInfo aliasInfo = aliasManager.getAliasInfo(potentialBase);
                if (aliasInfo instanceof SelectManager.SelectInfo) {
                    throw new RuntimeException("Cannot dereference select alias");
                }
                potentialBaseInfo = (JoinAliasInfo) aliasInfo;
            } else {
                AliasInfo aliasInfo = aliasManager.getAliasInfo(joinPath);
                if (aliasInfo instanceof SelectManager.SelectInfo) {
                    throw new RuntimeException("Cannot join select alias");
                }
                potentialBaseInfo = (JoinAliasInfo) aliasInfo;
            }
            if (potentialBaseInfo != null) {
                // We found an alias for the first part of the path
                String potentialBasePath = potentialBaseInfo.getAbsolutePath();
                JoinNode aliasNode = findNode(rootNode, potentialBasePath);
                // Note: aliasNode may never be null since a join alias info exists and therefore a join node must exist
                String relativePath = normalizedPath.substring(aliasNode.getAliasInfo().getAlias().length() + 1);
                String relativeJoinPath;

                if (relativePath.indexOf('.') == -1) {
                    // relativePath contains the field only                    
                    if (objectLeafAllowed) {
                        // Note: field cannot be null
                        baseNode = createOrUpdateNode(aliasNode, potentialBasePath, field, null, null, null, false, true, fromSelect);
                        if (baseNode.getAliasInfo().getAbsolutePath().endsWith(field)) {
                            field = null;
                        }
                    } else {
                        // although the join path is empty we need to do the call since we might have to reset the selectOnly flag in the potentialBasePath nodes
                        baseNode = createOrUpdateNode(aliasNode, potentialBasePath, "", null, field, null, false, true, fromSelect);
                    }
                } else {
                    relativeJoinPath = relativePath.substring(0, relativePath.length() - field.length() - 1);
                    if (objectLeafAllowed) {
                        // Note: field cannot be null
                        baseNode = createOrUpdateNode(aliasNode, potentialBasePath, relativeJoinPath + "." + field, null, null, null, false, true, fromSelect);
                        // if the field is not joinable we must not set the field to null
                        if (baseNode.getAliasInfo().getAbsolutePath().endsWith(field)) {
                            field = null;
                        }
                    } else {
                        baseNode = createOrUpdateNode(aliasNode, potentialBasePath, relativeJoinPath, null, field, null, false, true, fromSelect);
                    }
                }
            } else {
                // check if field is joinable
                // The given path is relative to the root
                if (objectLeafAllowed) {
                    // Note: field cannot be null
                    baseNode = createOrUpdateNode(rootNode, "", joinPath + (!joinPath.isEmpty() ? "." : "") + field, null, null, null, false, true, fromSelect);
                    // if the field is not joinable we must not set the field to null
                    if (baseNode.getAliasInfo().getAbsolutePath().endsWith(field)) {
                        field = null;
                    }
                } else {
                    baseNode = createOrUpdateNode(rootNode, "", joinPath, null, field, null, false, true, fromSelect);
                }
            }
        } else {
            // The given path may be relative to the root or it might be an alias
            if (objectLeafAllowed) {
                AliasInfo aliasInfo = aliasManager.getAliasInfoForBottomLevel(normalizedPath);
                if (aliasInfo != null) {
                    if (aliasInfo instanceof SelectManager.SelectInfo) {
                        /**
                         * We have a normalized path consisting of a single
                         * element that corresponds to a select alias. The
                         * original path must have started with the root alias,
                         * otherwise the path would have been filtered by
                         * isSkipable. Hence, to facilitate the replacement of
                         * this path with the selectAlias later on we have to
                         * set the base node.
                         */
                        aliasInfo = null;
                    }
                }
                JoinAliasInfo alias = (JoinAliasInfo) aliasInfo;
                if (alias == rootAliasInfo) {
                    baseNode = rootNode;
                    field = null;
                } else if (alias != null) {
                    baseNode = findNode(rootNode, alias.getAbsolutePath());
                    field = null;
                } else {
                    // check if the path is joinable, assuming it is relative to the root (implicit root prefix)
                    baseNode = createOrUpdateNode(rootNode, "", normalizedPath, null, null, null, false, true, fromSelect);
                    // check if the last path element was also joined
                    if (baseNode.getAliasInfo().getAbsolutePath().endsWith(normalizedPath)) {
                        field = null;
                    } else {
                        field = normalizedPath;
                    }
                }
            } else {
                Attribute attr = metamodel.entity(clazz).getAttribute(normalizedPath);
                if (attr == null) {
                    throw new IllegalArgumentException("Field with name "
                            + normalizedPath + " was not found within class "
                            + clazz.getName());
                }
                if (ModelUtils.isJoinable(attr)) {
                    throw new IllegalArgumentException("No object leaf allowed but " + normalizedPath + " is an object leaf");
                }
                baseNode = rootNode;
                field = normalizedPath;
            }
        }
        return new JoinResult(baseNode, field);
    }

    protected JoinNode createOrUpdateNode(JoinNode baseNode, String basePath, String joinPath, String alias, String field, JoinType joinType, boolean fetch, boolean implicit, boolean fromSelect) {
        JoinNode currentNode = baseNode;
        StringBuilder currentPath = new StringBuilder(basePath);
        String[] pathElements = (joinPath + (!joinPath.isEmpty() && field != null ? "." : "") + (field == null ? "" : field)).split("\\.");

        if (!fromSelect && !basePath.isEmpty()) {
            // updated base path nodes
            baseNode.setSelectOnly(false);
            if (basePath.contains(".")) {
                for (String pathElem : basePath.split(".")) {
                    currentNode = currentNode.getNodes().get(pathElem);
                    currentNode.setSelectOnly(false);
                }
                currentNode = baseNode;
            }
        }

        Class<?> currentClass;
        if (baseNode.getPropertyClass() == null) {
            currentClass = clazz;
        } else {
            currentClass = baseNode.getPropertyClass();
        }

        // Iterate through all property names
        for (int j = 0; j < pathElements.length; j++) {
            String propertyName = pathElements[j];

            EntityType type = metamodel.entity(currentClass);
            Attribute attr = type.getAttribute(propertyName);
            if (attr == null) {
                throw new IllegalArgumentException("Field with name "
                        + propertyName + " was not found within class "
                        + currentClass.getName());
            }
            boolean collectionValued = attr.isCollection();
            Class<?> resolvedFieldClass = ModelUtils.resolveFieldClass(attr);
            // Parseable types do not need to be fetched, so also sub
            // properties would not have to be fetched
            // Christian Beikov 14.09.13:
            // Added check for collection and map types since fieldClass evaluates to V if the field is of type Map<K, V>
            if (!ModelUtils.isJoinable(attr)) {
                LOG.fine(new StringBuilder("Field with name ").append(propertyName).append(" of class ").append(currentClass.getName()).append(" is parseable and therefore it has not to be fetched explicitly.").toString());
                break;
            }

            currentClass = resolvedFieldClass;

            JoinType modelAwareType;
            if ((attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE)
                    && ((SingularAttribute) attr).isOptional() == false) {
                modelAwareType = JoinType.INNER;
            } else {

                modelAwareType = JoinType.LEFT;
            }

            if (j == pathElements.length - 1) {
                // use parameters for joining the last path property
                if (alias == null) {
                    // default alias
                    alias = propertyName;
                }
                if (joinType == null) {
                    joinType = modelAwareType;
                }
                currentNode = getOrCreate(currentPath, currentNode, propertyName, resolvedFieldClass, alias, joinType, fetch, "Ambiguous implicit join", implicit, attr.isCollection());
            } else {
                currentNode = getOrCreate(currentPath, currentNode, propertyName, resolvedFieldClass, propertyName, modelAwareType, fetch, "Ambiguous implicit join", true, attr.isCollection());
            }
            if (fetch) {
                currentNode.setFetch(true);
            }
            if (!fromSelect) {
                currentNode.setSelectOnly(false);
            }
        }
        return currentNode;
    }

    protected JoinNode getOrCreate(StringBuilder currentPath, JoinNode currentNode, String joinRelationName, Class<?> joinRelationClass, String alias, JoinType type, boolean fetch, String errorMessage, boolean implicit, boolean collection) {
        JoinNode node = currentNode.getNodes().get(joinRelationName);
        if (currentPath.length() > 0) {
            currentPath.append('.');
        }
        currentPath.append(joinRelationName);
        if (node == null) {
            // a join node for the join relation does not yet exist
            String currentJoinPath = currentPath.toString();
            AliasInfo oldAliasInfo = aliasManager.getAliasInfoForBottomLevel(alias);
            if (oldAliasInfo instanceof SelectManager.SelectInfo) {
                throw new IllegalStateException("Alias [" + oldAliasInfo.getAlias() + "] already used as select alias");
            }
            JoinAliasInfo oldJoinAliasInfo = (JoinAliasInfo) oldAliasInfo;
            if (oldJoinAliasInfo != null) {
                if (!oldJoinAliasInfo.getAbsolutePath().equals(currentJoinPath)) {
                    throw new IllegalArgumentException(errorMessage);
                } else {
                    throw new RuntimeException("Probably a programming error if this happens. An alias[" + alias + "] for the same join path[" + currentJoinPath + "] is available but the join node is not!");
                }
            }

            // we alias might have to be postfixed since it might already exist in parent queries
            if (implicit) {
                alias = aliasManager.generatePostfixedAlias(alias);
            }
            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, aliasOwner);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = new JoinNode(newAliasInfo, type, fetch, joinRelationClass, collection);
            currentNode.getNodes().put(joinRelationName, node);
        } else {
            JoinAliasInfo nodeAliasInfo = node.getAliasInfo();
            if (!alias.equals(nodeAliasInfo.getAlias())) {
                // Aliases for the same join paths don't match
                if (nodeAliasInfo.isImplicit() && !implicit) {
                    // Overwrite implicit aliases
                    aliasManager.unregisterAliasInfoForBottomLevel(nodeAliasInfo);
                    // we must alter the nodeAliasInfo instance since this instance
                    // is also set on the join node
                    nodeAliasInfo.setAlias(alias);
                    nodeAliasInfo.setImplicit(false);
                    // We can only change the join type if the existing node is implicit and the update on the node is not implicit
                    node.setType(type);

                    aliasManager.registerAliasInfo(nodeAliasInfo);
                } else if (!nodeAliasInfo.isImplicit() && !implicit) {
                    throw new IllegalArgumentException("Alias conflict[" + nodeAliasInfo.getAlias() + "=" + nodeAliasInfo.getAbsolutePath() + ", " + alias + "=" + currentPath.toString() + "]");
                }
            }
        }
        return node;
    }

    JoinNode findNode(JoinNode baseNode, String path) {
        JoinNode currentNode = baseNode;
        String[] pathElements = path.split("\\.");
        for (int i = 0; i < pathElements.length; i++) {
            currentNode = currentNode.getNodes().get(pathElements[i]);
        }
        return currentNode;
    }

    JoinNode findNode(String path) {
        return findNode(rootNode, path);
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
    private void fetchPath(JoinNode baseNode, String path) {
        JoinNode currentNode = baseNode;
        String[] pathElements = path.split("\\.");
        for (int i = 0; i < pathElements.length; i++) {
            currentNode = currentNode.getNodes().get(pathElements[i]);
            currentNode.setFetch(true);
        }
    }

    public void setSubqueryInitFactory(SubqueryInitiatorFactory subqueryInitFactory) {
        this.subqueryInitFactory = subqueryInitFactory;
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
            joinNode.setWithPredicate(builder.getPredicate());
        }
    }
}
