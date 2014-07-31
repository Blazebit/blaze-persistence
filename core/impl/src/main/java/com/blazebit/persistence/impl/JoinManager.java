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
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.reflection.ReflectionUtils;
import java.util.Map;
import java.util.logging.Logger;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author ccbem
 */
public class JoinManager {

    private final static Logger LOG = Logger.getLogger(JoinManager.class.getName());

    // we might have multiple nodes that depend on the same unresolved alias,
    // hence we need a List of NodeInfos.
    // e.g. SELECT a.X, a.Y FROM A a
    // a is unresolved for both X and Y
    private final JoinNode rootNode;
    // Maps alias to join path with the root as base
//    private final Map<String, JoinAliasInfo> joinAliasInfos = new HashMap<String, JoinAliasInfo>();
    private final JoinAliasInfo rootAliasInfo;
    // root entity class
    private final Class<?> clazz;
    private final QueryGenerator queryGenerator;
    private final String joinRestrictionKeyword;
    private final AliasManager aliasManager;
    private final BaseQueryBuilder<?, ?> aliasOwner;
    private final Metamodel metamodel; // needed for model-aware joins
    private final JoinManager parent;

    public JoinManager(String rootAlias, Class<?> clazz, QueryGenerator queryGenerator, JPAInfo jpaInfo, AliasManager aliasManager, BaseQueryBuilder<?, ?> aliasOwner, Metamodel metamodel, JoinManager parent) {
        if (rootAlias == null) {
            rootAlias = aliasManager.generatePostfixedAlias(clazz.getSimpleName().toLowerCase());
        }
        this.rootAliasInfo = new JoinAliasInfo(rootAlias, "", true, aliasOwner);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(this.rootAliasInfo);

        this.rootNode = new JoinNode(rootAliasInfo, null, false, null);
        this.clazz = clazz;
        this.queryGenerator = queryGenerator;
        this.aliasManager = aliasManager;
        this.aliasOwner = aliasOwner;
        this.metamodel = metamodel;
        this.parent = parent;

        if (jpaInfo.isJPA21) {
            joinRestrictionKeyword = " ON ";
        } else if (jpaInfo.isHibernate) {
            joinRestrictionKeyword = " WITH ";
        } else {
            throw new UnsupportedOperationException("Unsupported JPA provider");
        }

    }
    
    public JoinManager(String rootAlias, Class<?> clazz, QueryGenerator queryGenerator, JPAInfo jpaInfo, AliasManager aliasManager, BaseQueryBuilder<?, ?> aliasOwner, Metamodel metamodel) {
        this(rootAlias, clazz, queryGenerator, jpaInfo, aliasManager, aliasOwner, metamodel, null);
    }

    String getRootAlias() {
        return rootAliasInfo.getAlias();
    }

    void buildJoins(boolean includeSelect, StringBuilder sb) {
        applyJoins(sb, rootAliasInfo, rootNode.getNodes(), includeSelect);
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
                case OUTER:
                    sb.append(" OUTER JOIN ");
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

    private boolean isSkipablePath(String path, boolean fromSelect) {
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

        if (aliasInfo.getAliasOwner() != aliasOwner) {
            // the alias exists but does not originate from the current query builder
            if (singlePathElement) {
                // the alias is external so we do not have to treat it
                return true;
            }
            // an external select alias must not be dereferenced
            if (aliasInfo instanceof SelectManager.SelectInfo) {
                throw new ExternalAliasDereferencingException("Start alias of path '" + path + "' is external and must not be dereferenced");
            } else {
                // dereferencing external join aliases is allowed for collection expressions
                return true;
            }
        }

        if (aliasInfo instanceof SelectManager.SelectInfo && !fromSelect) {
            // select alias
            if (!singlePathElement) {
                throw new IllegalStateException("Path starting with select alias not allowed");
            }

            // do not join select aliases
            return true;
        }
        return false;
    }

    void join(String path, String alias, JoinType type, boolean fetch) {
        if (isSkipablePath(path, false)) {
            return;
        }
        String normalizedPath;
        if (startsAtRootAlias(path)) {
            // The given path is relative to the root
            normalizedPath = path.substring(rootAliasInfo.getAlias().length() + 1);
            createOrUpdateNode(rootNode, "", normalizedPath, alias, null, type, fetch, false, true);
        } else {
            // The path is either already normalized or uses a specific alias as base
            normalizedPath = path;
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
                    createOrUpdateNode(aliasNode, potentialBasePath, relativePath, alias, null, type, fetch, false, true);
                    //if fetch is true we have to fetch the whole path from aliasNode back to the root
                    if (fetch) {
                        fetchPath(rootNode, potentialBasePath);
                    }
                } else {
                    // The given path is relative to the root
                    createOrUpdateNode(rootNode, "", normalizedPath, alias, null, type, fetch, false, true);
                }
            } else {
                // The given path is relative to the root
                createOrUpdateNode(rootNode, "", normalizedPath, alias, null, type, fetch, false, true);
            }
        }
    }
    
    void implicitParentJoin(Expression expression){
        if(parent == null){
            
        }
    }

    void implicitJoin(Expression expression, boolean objectLeafAllowed, boolean fromSelect) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;
            String path = pathExpression.getPath();
            String normalizedPath = normalizePath(path);
            if (isSkipablePath(path, fromSelect)) {
                return;
            }
            JoinResult result = implicitJoin(normalizedPath, objectLeafAllowed, fromSelect);

            if (pathExpression.isCollectionValued() && result.field == null) {
                // we have to reset the field and the base node
                String absPath = result.baseNode.getAliasInfo().getAbsolutePath();
                int lastDotIndex;
                if ((lastDotIndex = absPath.lastIndexOf('.')) == -1) {
                    result = new JoinResult(rootNode, absPath);
                } else {
                    JoinNode newBaseNode = findNode(absPath.substring(0, lastDotIndex));
                    result = new JoinResult(newBaseNode, absPath.substring(lastDotIndex + 1));
                }
            }
            pathExpression.setBaseNode(result.baseNode);
            pathExpression.setField(result.field);
            //also do implicit joins for array indices
            for (PathElementExpression pathElem : pathExpression.getExpressions()) {
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, fromSelect);
                }
            }
        } else if (expression instanceof CompositeExpression) {
            for (Expression exp : ((CompositeExpression) expression).getExpressions()) {
                implicitJoin(exp, objectLeafAllowed, fromSelect);
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
        int fieldStartDotIndex;
        if ((fieldStartDotIndex = normalizedPath.lastIndexOf('.')) != -1) {
            // First we extract the field by which should be ordered
            field = normalizedPath.substring(fieldStartDotIndex + 1);
            String joinPath = normalizedPath.substring(0, fieldStartDotIndex);
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
                // TODO: if aliasNode is null, then probably a subpath is not yet joined
                String relativePath = normalizedPath.substring(aliasNode.getAliasInfo().getAlias().length() + 1);
                normalizedPath = potentialBasePath + '.' + relativePath;
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
                currentNode = getOrCreate(currentPath, currentNode, propertyName, resolvedFieldClass, alias, joinType, fetch, "Ambiguous implicit join", implicit);
            } else {
                currentNode = getOrCreate(currentPath, currentNode, propertyName, resolvedFieldClass, propertyName, modelAwareType, fetch, "Ambiguous implicit join", true);
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

    protected JoinNode getOrCreate(StringBuilder currentPath, JoinNode currentNode, String joinRelationName, Class<?> joinRelationClass, String alias, JoinType type, boolean fetch, String errorMessage, boolean implicit) {
        JoinNode node = currentNode.getNodes().get(joinRelationName);
        if (currentPath.length() > 0) {
            currentPath.append('.');
        }
        currentPath.append(joinRelationName);
        if (node == null) {
            // a join node for the join relation does not yet exist
            String currentJoinPath = currentPath.toString();
            AliasInfo oldAliasInfo = aliasManager.getAliasInfoForBottomLevel(alias);
            if(oldAliasInfo instanceof SelectManager.SelectInfo){
                throw new IllegalStateException("Alias [" + oldAliasInfo.getAlias() + "] already used as select alias");
            }
            JoinAliasInfo oldJoinAliasInfo = (JoinAliasInfo) oldAliasInfo;
            if (oldJoinAliasInfo != null) {
                if (!oldJoinAliasInfo.getAbsolutePath().equals(currentJoinPath)) {
                    throw new IllegalArgumentException(errorMessage);
                } else {
                    throw new RuntimeException("Probably a programming error if this happens. An alias[" + alias + "] for the same join path[" + currentJoinPath + "] is available but the join node is not!");
                }
            } else {
                // we alias might have to be postfixed since it might already exist in parent queries
                if (implicit) {
                    alias = aliasManager.generatePostfixedAlias(alias);
                }
                JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, aliasOwner);
                aliasManager.registerAliasInfo(newAliasInfo);
                node = new JoinNode(newAliasInfo, type, fetch, joinRelationClass);
            }
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

    private static class JoinResult {

        final JoinNode baseNode;
        final String field;

        public JoinResult(JoinNode baseNode, String field) {
            this.baseNode = baseNode;
            this.field = field;
        }
    }
}
