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
import static com.blazebit.persistence.impl.AbstractBaseQueryBuilder.log;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.reflection.ReflectionUtils;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ccbem
 */
public class JoinManager {

    // we might have multiple nodes that depend on the same unresolved alias,
    // hence we need a List of NodeInfos.
    // e.g. SELECT a.X, a.Y FROM A a
    // a is unresolved for both X and Y
    private final JoinNode rootNode;
    // Maps alias to join path with the root as base
    private final Map<String, AliasInfo> joinAliasInfos = new HashMap<String, AliasInfo>();
    private final AliasInfo rootAliasInfo;
    // root entity class
    private final Class<?> clazz;
    private final QueryGenerator queryGenerator;
    private final String joinRestrictionKeyword;

    public JoinManager(String rootAlias, Class<?> clazz, QueryGenerator queryGenerator, AbstractBaseQueryBuilder.JPAInfo jpaInfo) {
        this.rootAliasInfo = new AliasInfo(rootAlias, "", true);
        this.joinAliasInfos.put(rootAlias, rootAliasInfo);
        this.rootNode = new JoinNode(rootAliasInfo, null, false, null);
        this.clazz = clazz;
        this.queryGenerator = queryGenerator;
        
        if(AbstractBaseQueryBuilder.JPAInfo.JPA_2_1){
            joinRestrictionKeyword = " ON ";
        }else if(jpaInfo.isHibernate){ //TODO: add version check
            joinRestrictionKeyword = " WITH ";
        }else{// TODO: add workaround for hibernate
            throw new UnsupportedOperationException("Unsupported JPA provider");
        }
    }

    String getRootAlias() {
        return rootAliasInfo.getAlias();
    }

    String buildJoins(boolean includeSelect) {
        StringBuilder sb = new StringBuilder();
        applyJoins(sb, rootAliasInfo, rootNode.getNodes(), includeSelect);
        return sb.toString();
    }

    AliasInfo getAliasInfoByJoinPath(String joinPath) {
        return joinAliasInfos.get(joinPath);
    }

    private void applyJoins(StringBuilder sb, AliasInfo joinBase, Map<String, JoinNode> nodes, boolean includeSelect) {
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

    void join(String path, String alias, JoinType type, boolean fetch) {
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
                AliasInfo potentialBaseInfo = joinAliasInfos.get(potentialBase);
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

    void implicitJoin(Expression expression, boolean objectLeafAllowed, boolean fromSelect) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;
            // normalize the path, i.e. if it starts at the root alias, remove this part
//            normalizePath(pathExpression);
            JoinResult result = implicitJoin(pathExpression.getPath(), objectLeafAllowed, fromSelect);
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

    private void normalizePath(PathExpression path) {
        if (path.getExpressions().get(0).toString().equals(rootAliasInfo.getAlias())) {
            path.getExpressions().remove(0);
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

    JoinResult implicitJoin(String path, boolean objectLeafAllowed, boolean fromSelect) {
        String normalizedPath = normalizePath(path);

        JoinNode baseNode;
        String field;
        int dotIndex;
        int fieldStartDotIndex;
        if ((fieldStartDotIndex = normalizedPath.lastIndexOf('.')) != -1) {
            // First we extract the field by which should be ordered
            field = normalizedPath.substring(fieldStartDotIndex + 1);
            String joinPath = normalizedPath.substring(0, fieldStartDotIndex);
            //TEST
//            joinPath = normalizedPath;
            AliasInfo potentialBaseInfo;
            if ((dotIndex = joinPath.indexOf('.')) != -1) {
                // We found a dot in the path, so it either uses an alias or does chained joining
                String potentialBase = normalizedPath.substring(0, dotIndex);
                potentialBaseInfo = joinAliasInfos.get(potentialBase);
            } else {
                potentialBaseInfo = joinAliasInfos.get(joinPath);
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
                        baseNode = createOrUpdateNode(aliasNode, potentialBasePath, relativeJoinPath + (!relativeJoinPath.isEmpty() ? "." : "") + field, null, null, null, false, true, fromSelect);
                        // if the field is not joinable we must not set the field to null
                        if (baseNode.getAliasInfo().getAbsolutePath().endsWith(field)) {
                            field = null;
                        }
                    } else {
                        baseNode = createOrUpdateNode(aliasNode, potentialBasePath, relativeJoinPath, null, field, null, false, true, fromSelect);
                    }
                }
                //TEST
//                relativeJoinPath = relativePath;
//                if (relativeJoinPath.isEmpty()) {

//                } else {
//                    baseNode = createOrUpdateNode(aliasNode, potentialBasePath, relativeJoinPath, null, null, false, true, fromSelect);
//                    if (baseNode.getAliasInfo().getAbsolutePath().endsWith(relativeJoinPath)) {
//                        field = null;
//                    }
//                }
            } else {
                //                String potentialRootProperty = ExpressionUtils.getFirstPathElement(normalizedPath);
                //                if (ReflectionUtils.getField(clazz, potentialRootProperty) == null) {
                //                    throw new IllegalStateException("Unresolved alias: " + normalizedPath);
                //                }
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
//                if (baseNode.getAliasInfo().getAbsolutePath().endsWith(joinPath)) {
//                    field = null;
//                }
            }
        } else {
            // The given path may be relative to the root or it might be an alias
            if (objectLeafAllowed) {
                AliasInfo alias = joinAliasInfos.get(normalizedPath);
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
                Class<?> fieldClass = ModelUtils.resolveFieldClass(clazz, normalizedPath);
                if (ModelUtils.isJoinable(fieldClass)) {
                    throw new IllegalArgumentException("No object leaf allowed but " + normalizedPath + " is an object leaf");
                }
                baseNode = rootNode;
                field = normalizedPath;
            }
        }
        return new JoinResult(baseNode, field);
    }

    protected JoinNode createOrUpdateNode(JoinNode baseNode, String basePath, String joinPath, String alias, String field, JoinType type, boolean fetch, boolean implicit, boolean fromSelect) {
        JoinNode currentNode = baseNode;
        StringBuilder currentPath = new StringBuilder(basePath);
        String joinAlias = alias;
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
        // TODO: Implement model aware joining or use fetch profiles or so to decide the join types automatically

        Class<?> currentClass;
        if (baseNode.getPropertyClass() == null) {
            currentClass = clazz;
        } else {
            currentClass = baseNode.getPropertyClass();
        }
        // Iterate through all property names
        for (int j = 0; j < pathElements.length; j++) {
            String propertyName = pathElements[j];

            //            Field propertyField = ReflectionUtils.getField(currentClass, propertyName);
            Class<?> rawFieldClass = ReflectionUtils.getResolvedFieldType(currentClass, propertyName);
            Class<?> resolvedFieldClass = ModelUtils.resolveFieldClass(currentClass, propertyName);
            // Parseable types do not need to be fetched, so also sub
            // properties would not have to be fetched
            // Christian Beikov 14.09.13:
            // Added check for collection and map types since fieldClass evaluates to V if the field is of type Map<K, V>
            if (!ModelUtils.isJoinable(rawFieldClass)) {
                log.info(new StringBuilder("Field with name ").append(propertyName).append(" of class ").append(currentClass.getName()).append(" is parseable and therefore it has not to be fetched explicitly.").toString());
                break;
            }
            currentClass = resolvedFieldClass;
            if (j == pathElements.length - 1) {
                // use parameters for joining the last path property
                if (joinAlias == null) {
                    joinAlias = propertyName;
                }
                if (type == null) {
                    // TODO: Implement model aware joining
                    type = JoinType.LEFT;
                }
                currentNode = getOrCreate(currentPath, currentNode, propertyName, resolvedFieldClass, joinAlias, type, fetch, "Ambiguous implicit join", implicit);
            } else {
                currentNode = getOrCreate(currentPath, currentNode, propertyName, resolvedFieldClass, propertyName, JoinType.LEFT, fetch, "Ambiguous implicit join", true);
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
            String currentJoinPath = currentPath.toString();
            AliasInfo oldAliasInfo = joinAliasInfos.get(alias);
            if (oldAliasInfo != null) {
                if (!oldAliasInfo.getAbsolutePath().equals(currentJoinPath)) {
                    throw new IllegalArgumentException(errorMessage);
                } else {
                    throw new RuntimeException("Probably a programming error if this happens. An alias[" + alias + "] for the same join path[" + currentJoinPath + "] is available but the join node is not!");
                }
            } else {
                node = new JoinNode(new AliasInfo(alias, currentJoinPath, implicit), type, fetch, joinRelationClass);
                joinAliasInfos.put(alias, node.getAliasInfo());
            }
            currentNode.getNodes().put(joinRelationName, node);
        } else {
            AliasInfo nodeAliasInfo = node.getAliasInfo();
            if (!alias.equals(nodeAliasInfo.getAlias())) {
                // Aliases for the same join paths don't match
                if (nodeAliasInfo.isImplicit() && !implicit) {
                    // Overwrite implicit aliases
                    String oldAlias = nodeAliasInfo.getAlias();
                    nodeAliasInfo.setAlias(alias);
                    nodeAliasInfo.setImplicit(false);
                    // We can only change the join type if the existing node is implicit and the update on the node is not implicit
                    node.setType(type);
                    joinAliasInfos.remove(oldAlias);
                    joinAliasInfos.put(alias, nodeAliasInfo);
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
