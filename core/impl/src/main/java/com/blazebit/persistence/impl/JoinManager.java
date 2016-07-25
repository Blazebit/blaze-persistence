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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.builder.predicate.JoinOnBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PathReference;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimplePathReference;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

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
    private List<JoinNode> rootNodes = new ArrayList<JoinNode>(1);
    // root entity class
    private final String joinRestrictionKeyword;
    private final AliasManager aliasManager;
    private final Metamodel metamodel; // needed for model-aware joins
    private final JoinManager parent;
    private final JoinOnBuilderEndedListener joinOnBuilderListener;
    private SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    // helper collections for join rendering
    private final Set<JoinNode> collectionJoinNodes = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());
    private final Set<JoinNode> renderedJoins = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());
    private final Set<JoinNode> markedJoinNodes = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());

    JoinManager(MainQuery mainQuery, ResolvingQueryGenerator queryGenerator, AliasManager aliasManager, JoinManager parent, ExpressionFactory expressionFactory) {
        super(queryGenerator, mainQuery.parameterManager);
        this.aliasManager = aliasManager;
        this.metamodel = mainQuery.em.getMetamodel();
        this.parent = parent;
        this.joinRestrictionKeyword = " " + mainQuery.jpaProvider.getOnClause() + " ";
        this.joinOnBuilderListener = new JoinOnBuilderEndedListener();
        this.expressionFactory = expressionFactory;
    }

    String addRoot(EntityType<?> clazz, String rootAlias) {
        if (rootAlias == null) {
            // TODO: not sure if other JPA providers support case sensitive queries like hibernate
            StringBuilder sb = new StringBuilder(clazz.getName());
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            String alias = sb.toString();

            if (aliasManager.getAliasInfo(alias) == null) {
                rootAlias = alias;
            } else {
                rootAlias = aliasManager.generatePostfixedAlias(alias);
            }
        }
        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, aliasManager);
        JoinNode rootNode = new JoinNode(null, null, rootAliasInfo, null, clazz.getJavaType());
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
    	if (rootNodes.size() > 1) {
    		throw new IllegalArgumentException(string);
    	}
    	
		return rootNodes.get(0);
	}

    JoinNode getRootNode(String alias) {
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

    List<JoinNode> getRoots() {
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
    
    Set<JoinNode> getCollectionJoins() {
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
    
    private void fillCollectionJoinsNodesRec(JoinNode node, Set<JoinNode> collectionNodes) {
        for (JoinTreeNode treeNode : node.getNodes().values()) {
            if (treeNode.isCollection()) {
                collectionNodes.addAll(treeNode.getJoinNodes().values());
                for (JoinNode childNode : treeNode.getJoinNodes().values()) {
                    fillCollectionJoinsNodesRec(childNode, collectionNodes);
                }
            }
        }
    }

    JoinManager getParent() {
        return parent;
    }

    void setSubqueryInitFactory(SubqueryInitiatorFactory subqueryInitFactory) {
        this.subqueryInitFactory = subqueryInitFactory;
    }

    Set<JoinNode> buildClause(StringBuilder sb, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes) {
    	collectionJoinNodes.clear();
        renderedJoins.clear();
        sb.append(" FROM ");
        
        // TODO: we might have dependencies to other from clause elements which should also be accounted for
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
    	for (int i = 0; i < size; i++) {
    		if (i != 0) {
    			sb.append(", ");
    		}
    		
    		JoinNode rootNode = nodes.get(i);
            EntityType<?> type = metamodel.entity(rootNode.getPropertyClass());
            sb.append(type.getName());
            
	        sb.append(' ');
	        
	        if (aliasPrefix != null) {
	        	sb.append(aliasPrefix);
	        }
	        
	        sb.append(rootNode.getAliasInfo().getAlias());
	        
	        // TODO: not sure if needed since applyImplicitJoins will already invoke that
    		rootNode.registerDependencies();
    		applyJoins(sb, rootNode.getAliasInfo(), rootNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes);
    	}
    	
    	return collectionJoinNodes;
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

    public boolean acceptVisitor(AggregateDetectionVisitor aggregateDetector, boolean stopValue) {
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

    void applyTransformer(ExpressionTransformer transformer) {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
    	for (int i = 0; i < size; i++) {
    		nodes.get(i).accept(new OnClauseJoinNodeVisitor(new PredicateManager.TransformationVisitor(transformer, ClauseType.JOIN)));
    	}
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

            if (node.getOnPredicate() != null && !node.getOnPredicate().getChildren().isEmpty()) {
                sb.append(joinRestrictionKeyword);
                queryGenerator.setQueryBuffer(sb);
                boolean conditionalContext = queryGenerator.setConditionalContext(true);
                node.getOnPredicate().accept(queryGenerator);
                queryGenerator.setConditionalContext(conditionalContext);
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
                            throw new IllegalStateException("Cyclic join dependency detected at absolute path ["
                                + dep.getAliasInfo().getAbsolutePath() + "] with alias [" + dep.getAliasInfo().getAlias() + "]");
                        }
                        // render reverse dependencies
                        renderReverseDependency(sb, dep, aliasPrefix);
                    }
                } finally {
                    markedJoinNodes.remove(dependency);
                }
            }
            renderJoinNode(sb, dependency.getParent().getAliasInfo(), dependency, aliasPrefix);
        }
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, Map<String, JoinTreeNode> nodes, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes) {
        for (Map.Entry<String, JoinTreeNode> nodeEntry : nodes.entrySet()) {
            JoinTreeNode treeNode = nodeEntry.getValue();

            for (JoinNode node : treeNode.getJoinNodes().values()) {
                // If the clauses in which a join node occurs are all excluded or the join node is not mandatory for the cardinality, we skip it
                if (!clauseExclusions.isEmpty() && clauseExclusions.containsAll(node.getClauseDependencies()) && !node.isCardinalityMandatory()) {
                    continue;
                }

                // We have to render any dependencies this join node has before actually rendering itself 
                if (!node.getDependencies().isEmpty()) {
                    renderReverseDependency(sb, node, aliasPrefix);
                }
                
                // Collect the join nodes referring to collections
                if (collectCollectionJoinNodes && treeNode.isCollection()) {
                	collectionJoinNodes.add(node);
                }

                // Finally render this join node
                renderJoinNode(sb, joinBase, node, aliasPrefix);

                // Render child nodes recursively
                if (!node.getNodes().isEmpty()) {
                    applyJoins(sb, node.getAliasInfo(), node.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes);
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
                throw new ExternalAliasDereferencingException("Start alias [" + startAlias + "] of path [" + path.toString()
                    + "] is external and must not be dereferenced");
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
        JoinResult result = implicitJoin(null, pathExpression, 0, pathElements.size() - 1);
        JoinNode current = result.baseNode;
        
        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        } else {
            String joinRelation = result.field == null ? elementExpr.toString() : result.field + "." + elementExpr.toString();
            current = current == null ? getRootNodeOrFail("Could not join path [" + path + "] because it did not use an absolute path but multiple root nodes are available!") : current;
            result = createOrUpdateNode(current, joinRelation, alias, type, false, defaultJoin);
        }

        if (fetch) {
            fetchPath(result.baseNode);
        }

        return result.baseNode;
    }

	void implicitJoin(Expression expression, boolean objectLeafAllowed, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired) {
        implicitJoin(expression, objectLeafAllowed, fromClause, fromSubquery, fromSelectAlias, joinRequired, false);
    }

    void implicitJoin(Expression expression, boolean objectLeafAllowed, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;

            if (isJoinableSelectAlias(pathExpression, fromClause == ClauseType.SELECT, fromSubquery)) {
                String alias = pathExpression.getExpressions().get(0).toString();
                Expression expr = ((SelectInfo) aliasManager.getAliasInfo(alias)).getExpression();

                // this check is necessary to prevent infinite recursion in the case of e.g. SELECT name AS name
                if (!fromSelectAlias) {
                    // we have to do this implicit join because we might have to adjust the selectOnly flag in the referenced join nodes
                    implicitJoin(expr, true, fromClause, fromSubquery, true, joinRequired);
                }
                return;
            } else if (isExternal(pathExpression)) {
                // try to set base node and field for the external expression based
                // on existing joins in the super query
                parent.implicitJoin(pathExpression, true, fromClause, true, fromSelectAlias, joinRequired);
                return;
            }

            // First try to implicit join indices of array expressions since we will need their base nodes
            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            int pathElementSize = pathElements.size();
            for (int i = 0; i < pathElementSize; i++) {
                PathElementExpression pathElem = pathElements.get(i);
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, fromClause, fromSubquery, fromSelectAlias, joinRequired);
                }
            }

            PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);
            boolean singleValuedAssociationIdExpression = false;
            JoinNode current = null;
            List<String> resultFields = new ArrayList<String>();
            JoinResult currentResult;

            JoinNode possibleRoot = getRootNode(pathElements.get(0).toString());
            int startIndex = 0;

            if (possibleRoot != null) {
                startIndex = 1;
                current = possibleRoot;
            }

            if (pathElements.size() > startIndex + 1) {
                int maybeSingularAssociationIndex = pathElements.size() - 2;
                int maybeSingularAssociationIdIndex = pathElements.size() - 1;
                currentResult = implicitJoin(current, pathExpression, startIndex, maybeSingularAssociationIndex);
                current = currentResult.baseNode;
                
                if (currentResult.hasField()) {
                    resultFields.addAll(Arrays.asList(currentResult.field.split("\\.")));
                }
                singleValuedAssociationIdExpression = isSingleValuedAssociationId(currentResult, pathElements);

                if (singleValuedAssociationIdExpression) {
                } else {
                    if (currentResult.hasField()) {
                        // Start form the start index to respect the non joinable part
                        currentResult = createOrUpdateNode(current, currentResult.field + "." + pathElements.get(maybeSingularAssociationIndex), null, null, true, true);
                        current = currentResult.baseNode;
                        if(currentResult.hasField()){
                            currentResult = implicitJoin(current, pathExpression, startIndex, maybeSingularAssociationIdIndex);
                        }else{
                            currentResult = implicitJoin(current, pathExpression, maybeSingularAssociationIndex + 1, maybeSingularAssociationIdIndex);
                        }
                        resultFields.clear();
                    } else {
                        currentResult = implicitJoin(current, pathExpression, maybeSingularAssociationIndex, maybeSingularAssociationIdIndex);
                    }
                    
                    current = currentResult.baseNode;

                    if (currentResult.hasField()) {
                        resultFields.addAll(Arrays.asList(currentResult.field.split("\\.")));
                    }
                }
            } else {
                currentResult = implicitJoin(current, pathExpression, startIndex, pathElements.size() - 1);
                current = currentResult.baseNode;
                // TODO: Not sure if necessary
                if (currentResult.hasField()) {
                    resultFields.addAll(Arrays.asList(currentResult.field.split("\\.")));
                }
            }

            // current might be null
            if (current == null) {
            	if (rootNodes.size() > 1) {
            		throw new IllegalArgumentException("Could not join path [" + expression + "] because it did not use an absolute path but multiple root nodes are available!");
            	}
            	
            	current = rootNodes.get(0);
            }

            JoinResult result;
            AliasInfo aliasInfo;

            if (singleValuedAssociationIdExpression) {
                String associationName = pathElements.get(pathElements.size() - 2).toString();
                AliasInfo a = null;
                JoinTreeNode treeNode;

                if (currentResult.hasField()) {
                    associationName = currentResult.field + "." + associationName;
                } else {
                    a = aliasManager.getAliasInfoForBottomLevel(associationName);
                }
                
                if (a != null) {
                    result = new JoinResult(((JoinAliasInfo) a).getJoinNode(), elementExpr.toString());
                } else {
                    treeNode = current.getNodes().get(associationName);

                    if (treeNode != null && treeNode.getDefaultNode() != null) {
                        result = new JoinResult(treeNode.getDefaultNode(), elementExpr.toString());
                    } else {
                        result = new JoinResult(current, associationName + "." + elementExpr.toString());
                    }
                }
            } else if (elementExpr instanceof ArrayExpression) {
                // TODO: Not sure if necessary
                if (!resultFields.isEmpty()) {
                    throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                        + StringUtils.join(".", resultFields) + "]");
                }

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
                    currentResult = createOrUpdateNode(current, joinRelationName, joinAlias, null, true, false);
                    current = currentResult.baseNode;
                    // TODO: Not sure if necessary
                    if (currentResult.hasField()) {
                        throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part [" + currentResult.field
                            + "]");
                    }
                    generateAndApplyOnPredicate(current, arrayExpr);
                }

                result = new JoinResult(current, null);
            } else if (pathElements.size() == 1 && !fromSelectAlias
                && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                // No need to assert the resultFields here since they can't appear anyways if we enter this branch
                if (aliasInfo instanceof SelectInfo) {
                    // We actually allow usage of select aliases in expressions, but JPA doesn't, so we have to resolve them here
                    Expression selectExpr = ((SelectInfo) aliasInfo).getExpression();

                    if (!(selectExpr instanceof PathExpression)) {
                        throw new RuntimeException("The select expression '" + selectExpr.toString()
                            + "' is not a simple path expression! No idea how to implicit join that.");
                    }
                    // join the expression behind a select alias once when it is encountered the first time
                    if (((PathExpression) selectExpr).getBaseNode() == null) {
                        implicitJoin(selectExpr, objectLeafAllowed, fromClause, fromSubquery, true, joinRequired);
                    }
                    PathExpression selectPathExpr = (PathExpression) selectExpr;
                    result = new JoinResult((JoinNode) selectPathExpr.getBaseNode(), selectPathExpr.getField());
                } else {
                    // Naked join alias usage like in "KEY(joinAlias)"
                    result = new JoinResult(((JoinAliasInfo) aliasInfo).getJoinNode(), null);
                }
            } else if (!pathExpression.isUsedInCollectionFunction()) {
                if (resultFields.isEmpty()) {
                    result = implicitJoinSingle(current, elementExpr.toString(), objectLeafAllowed, joinRequired);
                } else {
                    resultFields.add(elementExpr.toString());

                    if (!validPath(current.getPropertyClass(), resultFields)) {
                        throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                            + StringUtils.join(".", resultFields) + "]");
                    }

                    result = implicitJoinSingle(current, StringUtils.join(".", resultFields), objectLeafAllowed, joinRequired);
                }
            } else {
                if (resultFields.isEmpty()) {
                    result = new JoinResult(current, elementExpr.toString());
                } else {
                    resultFields.add(elementExpr.toString());

                    if (!validPath(current.getPropertyClass(), resultFields)) {
                        throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                            + StringUtils.join(".", resultFields) + "]");
                    }

                    result = new JoinResult(current, StringUtils.join(".", resultFields));
                }
            }

            if (fetch) {
                fetchPath(result.baseNode);
            }

            // Don't forget to update the clause dependencies!!
            if (fromClause != null) {
                updateClauseDependencies(result.baseNode, fromClause);
            }

            if (result.isLazy()) {
                pathExpression.setPathReference(new LazyPathReference(result.baseNode, result.field));
            } else {
                pathExpression.setPathReference(new SimplePathReference(result.baseNode, result.field));
            }
        } else if (expression instanceof CompositeExpression) {
            List<Expression> expressions = ((CompositeExpression) expression).getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                implicitJoin(expressions.get(i), objectLeafAllowed, fromClause, fromSubquery, fromSelectAlias, joinRequired);
            }
        } else if (expression instanceof FunctionExpression) {
            List<Expression> expressions = ((FunctionExpression) expression).getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                implicitJoin(expressions.get(i), objectLeafAllowed, fromClause, fromSubquery, fromSelectAlias, joinRequired);
            }
        }
    }
    
    private static class LazyPathReference implements PathReference {
        private final JoinNode baseNode;
        private final String field;
        
        public LazyPathReference(JoinNode baseNode, String field) {
            this.baseNode = baseNode;
            this.field = field;
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((baseNode == null) ? 0 : baseNode.hashCode());
            result = prime * result + ((field == null) ? 0 : field.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof PathReference))
                return false;
            PathReference other = (PathReference) obj;
            if (baseNode == null) {
                if (other.getBaseNode() != null)
                    return false;
            } else if (!baseNode.equals(other.getBaseNode()))
                return false;
            if (field == null) {
                if (other.getField() != null)
                    return false;
            } else if (!field.equals(other.getField()))
                return false;
            return true;
        }
    }

	private boolean validPath(Class<?> currentClass, List<String> pathElements) {
        for (int i = 0; i < pathElements.size(); i++) {
            String element = pathElements.get(i);
            ManagedType<?> t = metamodel.managedType(currentClass);
            Set<Attribute<?, ?>> attributes = JpaUtils.getAttributesPolymorphic(metamodel, t, element);

            if (attributes.isEmpty()) {
                return false;
            } else if (attributes.size() == 1) {
                currentClass = attributes.iterator().next().getJavaType();
            } else {
                // Only consider a path valid when all possible paths along the polymorphic hierarchy are valid
                for (Attribute<?, ?> attr : attributes) {
                    if (!validPath(attr.getJavaType(), pathElements.subList(i, pathElements.size() - 1))) {
                        return false;
                    }
                }

                return true;
            }
        }

        return true;
    }

    private boolean isSingleValuedAssociationId(JoinResult joinResult, List<PathElementExpression> pathElements) {
        JoinNode parent = joinResult.baseNode;
        int maybeSingularAssociationIndex = pathElements.size() - 2;
        int maybeSingularAssociationIdIndex = pathElements.size() - 1;
        ManagedType<?> baseType;
        Set<Attribute<?, ?>> maybeSingularAssociationAttributes;
        String maybeSingularAssociationName = getSimpleName(pathElements.get(maybeSingularAssociationIndex));

        if (parent == null) {
            // This is the case when we have exactly 2 path elements
            AliasInfo a = aliasManager.getAliasInfo(maybeSingularAssociationName);

            if (a == null) {
                // if the path element is no alias we can do some optimizations
                baseType = metamodel.managedType(getRootNodeOrFail("Ambiguous join path [" + maybeSingularAssociationName + "] because of multiple root nodes!").getPropertyClass());
                maybeSingularAssociationAttributes = JpaUtils.getAttributesPolymorphic(metamodel, baseType, maybeSingularAssociationName);
            } else if (!(a instanceof JoinAliasInfo)) {
                throw new IllegalArgumentException("Can't dereference select alias in the expression!");
            } else {
                // If there is a JoinAliasInfo for the path element, we have to use the alias
                // So we return false in order to signal that a normal implicit join should be done
                return false;
                // JoinNode maybeSingularAssociationJoinNode = ((JoinAliasInfo) a).getJoinNode();
                // ManagedType<?> baseType = metamodel.managedType(maybeSingularAssociationJoinNode.getParent().getPropertyClass());
                // maybeSingularAssociation =
                // baseType.getAttribute(maybeSingularAssociationJoinNode.getParentTreeNode().getRelationName());
            }

        } else {
            Class<?> parentClass = parent.getPropertyClass();
            baseType = metamodel.managedType(parentClass);
            
            if (joinResult.hasField()) {
                Attribute<?, ?> fieldAttribute = JpaUtils.getAttribute(baseType, joinResult.field);
                baseType = metamodel.managedType(fieldAttribute.getJavaType());
            }
            
            maybeSingularAssociationAttributes = JpaUtils.getAttributesPolymorphic(metamodel, baseType, maybeSingularAssociationName);
        }

        if (maybeSingularAssociationAttributes.isEmpty()) {
            return false;
        }

        for (Attribute<?, ?> maybeSingularAssociation : maybeSingularAssociationAttributes) {
            if (maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.MANY_TO_ONE
            // TODO: to be able to support ONE_TO_ONE we need to know where the FK is
            // && maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE
            ) {
                return false;
            }

            Class<?> maybeSingularAssociationClass = JpaUtils.resolveFieldClass(baseType.getJavaType(), maybeSingularAssociation);
            ManagedType<?> maybeSingularAssociationType = metamodel.managedType(maybeSingularAssociationClass);
            String maybeSingularAssociationIdName = getSimpleName(pathElements.get(maybeSingularAssociationIdIndex));
            Set<Attribute<?, ?>> maybeSingularAssociationIdAttributes = JpaUtils.getAttributesPolymorphic(metamodel, maybeSingularAssociationType, maybeSingularAssociationIdName);

            if (maybeSingularAssociationIdAttributes.isEmpty()) {
                return false;
            }

            for (Attribute<?, ?> maybeSingularAssociationId : maybeSingularAssociationIdAttributes) {
                if (!(maybeSingularAssociationId instanceof SingularAttribute<?, ?>)) {
                    return false;
                }

                if (!((SingularAttribute<?, ?>) maybeSingularAssociationId).isId()) {
                    return false;
                }
            }
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
        } else {
            sb.append('_');
            sb.append(indexExpr.toString().replaceAll("\\.", "_"));
        }

        return sb.toString();
    }

    private EqPredicate getArrayExpressionPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        PathExpression keyPath = new PathExpression(new ArrayList<PathElementExpression>(), true);
        keyPath.getExpressions().add(new PropertyExpression(joinNode.getAliasInfo().getAlias()));
        keyPath.setPathReference(new SimplePathReference(joinNode, null));
        FunctionExpression keyExpression = new FunctionExpression("KEY", Arrays.asList((Expression) keyPath));
        return new EqPredicate(keyExpression, arrayExpr.getIndex());
    }

    private void registerDependencies(final JoinNode joinNode, Predicate onPredicate) {
        onPredicate.accept(new VisitorAdapter() {

            @Override
            public void visit(PathExpression pathExpr) {
                // prevent loop dependencies to the same join node
                if (pathExpr.getBaseNode() != joinNode) {
                    joinNode.getDependencies().add((JoinNode) pathExpr.getBaseNode());
                }
            }
        });
    }

    private void generateAndApplyOnPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        EqPredicate valueKeyFilterPredicate = getArrayExpressionPredicate(joinNode, arrayExpr);

        if (joinNode.getOnPredicate() != null) {
            AndPredicate currentPred = joinNode.getOnPredicate();

            // Only add the predicate if it isn't contained yet
            if (!findPredicate(currentPred, valueKeyFilterPredicate)) {
                currentPred.getChildren().add(valueKeyFilterPredicate);
                registerDependencies(joinNode, currentPred);
            }
        } else {
            AndPredicate onAndPredicate = new AndPredicate();
            onAndPredicate.getChildren().add(valueKeyFilterPredicate);
            joinNode.setOnPredicate(onAndPredicate);
            registerDependencies(joinNode, onAndPredicate);
        }
    }

    private JoinResult implicitJoin(JoinNode current, PathExpression pathExpression, int start, int end) {
        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        List<String> resultFields = new ArrayList<String>();
        PathElementExpression elementExpr;

        for (int i = start; i < end; i++) {
            AliasInfo aliasInfo;
            elementExpr = pathElements.get(i);
            if (elementExpr instanceof ArrayExpression) {
                ArrayExpression arrayExpr = (ArrayExpression) elementExpr;
                String joinRelationName = arrayExpr.getBase().toString();

                current = current == null ? getRootNodeOrFail("Ambiguous join path [" + joinRelationName + "] because of multiple root nodes!") : current;
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
                    final JoinResult result = createOrUpdateNode(current, joinRelationName, joinAlias, null, true, false);
                    current = result.baseNode;
                    // TODO: Not sure if necessary
                    if (result.hasField()) {
                        resultFields.add(result.field);
                    }
                    generateAndApplyOnPredicate(current, arrayExpr);
                }
            } else if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectInfo) {
                    throw new IllegalArgumentException("Can't dereference a select alias");
                } else {
                    // Join alias usage like in "joinAlias.relationName"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                }
            } else {
                if (!resultFields.isEmpty()) {
                    resultFields.add(elementExpr.toString());
                    JoinResult currentResult = createOrUpdateNode(current, StringUtils.join(".", resultFields), null, null, true, true);
                    current = currentResult.baseNode;
                    resultFields.clear();
                    if(currentResult.hasField()){
                        resultFields.add(currentResult.field);
                    }
                } else {
                    final JoinResult result = implicitJoinSingle(current, elementExpr.toString());
                    current = result.baseNode;
                    
                    if (result.hasField()) {
                        resultFields.add(result.field);
                    }
                }
            }
        }

        if (resultFields.isEmpty()) {
            return new JoinResult(current, null);
        } else {
            return new JoinResult(current, StringUtils.join(".", resultFields));
        }
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName) {
        if (baseNode == null) {
            // When no base is given, check if the attribute name is an alias
            AliasInfo aliasInfo = aliasManager.getAliasInfoForBottomLevel(attributeName);
            if (aliasInfo != null && aliasInfo instanceof JoinAliasInfo) {
                // if it is, we can just return the join node
                return new JoinResult(((JoinAliasInfo) aliasInfo).getJoinNode(), null);
            }
        }

        // If we have no base node, root is assumed
        if (baseNode == null) {
            baseNode = getRootNodeOrFail("Ambiguous join path [" + attributeName + "] because of multiple root nodes!");
        }

        // check if the path is joinable, assuming it is relative to the root (implicit root prefix)
        return createOrUpdateNode(baseNode, attributeName, null, null, true, true);
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName, boolean objectLeafAllowed, boolean joinRequired) {
        JoinNode newBaseNode;
        String field;
        boolean lazy = false;
        // The given path may be relative to the root or it might be an alias
        if (objectLeafAllowed) {
            Class<?> baseNodeType = baseNode.getPropertyClass();
            Attribute<?, ?> attr = getSimpleAttributeForImplicitJoining(metamodel.managedType(baseNodeType), attributeName);
            if (attr == null) {
                throw new IllegalArgumentException("Field with name " + attributeName + " was not found within class " + baseNodeType.getName());
            }
            
            if (joinRequired || attr.isCollection()) {
	            final JoinResult newBaseNodeResult = implicitJoinSingle(baseNode, attributeName);
	            newBaseNode = newBaseNodeResult.baseNode;
	            // check if the last path element was also joined
	            if (newBaseNode != baseNode) {
	                field = null;
	            } else {
	                field = attributeName;
	            }
            } else {
            	newBaseNode = baseNode;
                field = attributeName;
                lazy = true;
            }
        } else {
            Class<?> baseNodeType = baseNode.getPropertyClass();
            Attribute<?, ?> attr = getSimpleAttributeForImplicitJoining(metamodel.managedType(baseNodeType), attributeName);
            if (attr == null) {
                throw new IllegalArgumentException("Field with name " + attributeName + " was not found within class " + baseNodeType.getName());
            }
            if (JpaUtils.isJoinable(attr)) {
                throw new IllegalArgumentException("No object leaf allowed but " + attributeName + " is an object leaf");
            }
            newBaseNode = baseNode;
            field = attributeName;
        }
        return new JoinResult(newBaseNode, field, lazy);
    }

    private Attribute<?, ?> getSimpleAttributeForImplicitJoining(ManagedType<?> type, String attributeName) {
        Attribute<?, ?> attr;
        if (attributeName.indexOf('.') < 0) {
            attr = getPolymorphicSimpleAttributeForImplicitJoining(type, attributeName);
            return attr;
        }

        String[] attributeParts = attributeName.split("\\.");
        attr = getPolymorphicSimpleAttributeForImplicitJoining(type, attributeParts[0]);
        
        for (int i = 1; i < attributeParts.length; i++) {
            type = metamodel.managedType(JpaUtils.resolveFieldClass(type.getJavaType(), attr));
            attr = getPolymorphicAttributeForJoining(type, attributeParts[i]);
        }

        return attr;
    }

    private Attribute<?, ?> getPolymorphicSimpleAttributeForImplicitJoining(ManagedType<?> type, String attributeName) {
        Set<Attribute<?, ?>> resolvedAttributes = JpaUtils.getAttributesPolymorphic(metamodel, type, attributeName);
        Iterator<Attribute<?, ?>> iter = resolvedAttributes.iterator();

        if (resolvedAttributes.size() > 1) {
            // If there is more than one resolved attribute we can still save the user some trouble
            Attribute<?, ?> simpleAttribute = null;
            Set<Attribute<?, ?>> amiguousAttributes = new HashSet<Attribute<?, ?>>();

            for (Attribute<?, ?> attr : resolvedAttributes) {
                if (JpaUtils.isJoinable(attr)) {
                    amiguousAttributes.add(attr);
                } else {
                    simpleAttribute = attr;
                }
            }

            if (simpleAttribute == null) {
                return null;
            } else {
                for (Attribute<?, ?> a : amiguousAttributes) {
                    LOG.warning("The attribute [" + attributeName + "] of the class [" + a.getDeclaringType().getJavaType().getName()
                        + "] is ambiguous for polymorphic implicit joining on the type [" + type.getJavaType().getName() + "]");
                }

                return simpleAttribute;
            }
        } else if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
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

    private JoinType getModelAwareType(JoinNode baseNode, Attribute<?, ?> attr) {
        if (baseNode.getType() == JoinType.LEFT) {
            return JoinType.LEFT;
        }

        if ((attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE)
            && ((SingularAttribute<?, ?>) attr).isOptional() == false) {
            return JoinType.INNER;
        } else {
            return JoinType.LEFT;
        }
    }
    
    private static class AttributeJoinResult {
        private final Attribute<?, ?> attribute;
        private final Class<?> containingClass;
        
        public AttributeJoinResult(Attribute<?, ?> attribute, Class<?> containingClass) {
            this.attribute = attribute;
            this.containingClass = containingClass;
        }
    }

    private AttributeJoinResult getAttributeForJoining(ManagedType<?> type, String attributeName) {
        Attribute<?, ?> attr;
        if (attributeName.indexOf('.') < 0) {
            attr = getPolymorphicAttributeForJoining(type, attributeName);
            return new AttributeJoinResult(attr, type.getJavaType());
        }

        String[] attributeParts = attributeName.split("\\.");
        attr = getPolymorphicAttributeForJoining(type, attributeParts[0]);
        
        for (int i = 1; i < attributeParts.length; i++) {
            type = metamodel.managedType(JpaUtils.resolveFieldClass(type.getJavaType(), attr));
            attr = getPolymorphicAttributeForJoining(type, attributeParts[i]);
        }

        return new AttributeJoinResult(attr, type.getJavaType());
    }
    
    private Attribute<?, ?> getPolymorphicAttributeForJoining(ManagedType<?> type, String attributeName) {
        Set<Attribute<?, ?>> resolvedAttributes = JpaUtils.getAttributesPolymorphic(metamodel, type, attributeName);
        Iterator<Attribute<?, ?>> iter = resolvedAttributes.iterator();

        if (resolvedAttributes.size() > 1) {
            // If there is more than one resolved attribute we can still save the user some trouble
            Attribute<?, ?> joinableAttribute = null;
            Attribute<?, ?> attr = null;

            // Multiple non-joinable attributes would be fine since we only care for OUR join manager here
            // Multiple joinable attributes are only fine if they all have the same type
            while (iter.hasNext()) {
                attr = iter.next();
                if (JpaUtils.isJoinable(attr)) {
                    if (joinableAttribute != null && !joinableAttribute.getJavaType().equals(attr.getJavaType())) {
                        throw new IllegalArgumentException("Multiple joinable attributes with the name [" + attributeName
                            + "] but different java types in the types [" + joinableAttribute.getDeclaringType().getJavaType().getName()
                            + "] and [" + attr.getDeclaringType().getJavaType().getName() + "] found!");
                    } else {
                        joinableAttribute = attr;
                    }
                }
            }

            // We return the joinable attribute because OUR join manager needs it's type for further joining
            if (joinableAttribute != null) {
                return joinableAttribute;
            }

            return attr;
        } else if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }

    private JoinResult createOrUpdateNode(JoinNode baseNode, String joinRelationName, String alias, JoinType joinType, boolean implicit, boolean defaultJoin) {
        Class<?> baseNodeType = baseNode.getPropertyClass();
        ManagedType<?> type = metamodel.managedType(baseNodeType);
        AttributeJoinResult attrJoinResult = getAttributeForJoining(type, joinRelationName);
        Attribute<?, ?> attr = attrJoinResult.attribute;
        if (attr == null) {
            throw new IllegalArgumentException("Field with name " + joinRelationName + " was not found within class " + baseNodeType.getName());
        }
        Class<?> resolvedFieldClass = JpaUtils.resolveFieldClass(attrJoinResult.containingClass, attr);

        if (!JpaUtils.isJoinable(attr)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(new StringBuilder("Field with name ").append(joinRelationName)
                    .append(" of class ")
                    .append(baseNodeType.getName())
                    .append(" is parseable and therefore it has not to be fetched explicitly.")
                    .toString());
            }
            return new JoinResult(baseNode, joinRelationName);
        }

        if (implicit) {
            String aliasToUse = alias == null ? attr.getName() : alias;
            alias = aliasManager.generatePostfixedAlias(aliasToUse);
        }

        if (joinType == null) {
            joinType = getModelAwareType(baseNode, attr);
        }

        JoinNode newNode = getOrCreate(baseNode, joinRelationName, resolvedFieldClass, alias, joinType, "Ambiguous implicit join", implicit, defaultJoin, attr);

        return new JoinResult(newNode, null);
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
                throw new RuntimeException("Probably a programming error if this happens. An alias[" + alias + "] for the same join path["
                    + currentJoinPath + "] is available but the join node is not!");
            }
        }
    }

    private JoinNode getOrCreate(JoinNode baseNode, String joinRelationName, Class<?> joinRelationClass, String alias, JoinType type, String errorMessage, boolean implicit, boolean defaultJoin, Attribute<?, ?> attribute) {
        JoinTreeNode treeNode = baseNode.getOrCreateTreeNode(joinRelationName, attribute);
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
                    throw new IllegalArgumentException("Alias conflict [" + nodeAliasInfo.getAlias() + "=" + nodeAliasInfo.getAbsolutePath() + ", "
                        + alias + "=" + currentJoinPath + "]");
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
            AndPredicate andPredicate = node.getOnPredicate();

            if (findPredicate(andPredicate, pred)) {
                return node;
            }
        }

        return null;
    }

    private boolean findPredicate(AndPredicate andPredicate, Predicate pred) {
        if (andPredicate != null) {
            List<Predicate> children = andPredicate.getChildren();
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
     * Base node will NOT be fetched
     *
     * @param baseNode
     * @param path
     */
    private void fetchPath(JoinNode node) {
        JoinNode currentNode = node;
        while (currentNode != null) {
            currentNode.setFetch(true);
            // fetches implicitly need to be selected
            currentNode.getClauseDependencies().add(ClauseType.SELECT);
            currentNode = currentNode.getParent();
        }
    }

    // TODO: needs equals-hashCode implementation
    private static class JoinResult {

        final JoinNode baseNode;
        final String field;
        final boolean lazy;

        public JoinResult(JoinNode baseNode, String field) {
            this.baseNode = baseNode;
            this.field = field;
            this.lazy = false;
        }

        public JoinResult(JoinNode baseNode, String field, boolean lazy) {
            this.baseNode = baseNode;
            this.field = field;
            this.lazy = lazy;
        }

        private boolean hasField() {
            return field != null && !field.isEmpty();
        }
        
        private boolean isLazy() {
            return lazy;
        }
    }

    private class JoinOnBuilderEndedListener extends PredicateBuilderEndedListenerImpl {

        private JoinNode joinNode;

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            super.onBuilderEnded(builder);
            Predicate predicate = builder.getPredicate();
            predicate.accept(new VisitorAdapter() {

                private boolean isKeyFunction;

                @Override
                public void visit(FunctionExpression expression) {
                    boolean old = isKeyFunction;
                    this.isKeyFunction = "KEY".equalsIgnoreCase(expression.getFunctionName());
                    super.visit(expression);
                    this.isKeyFunction = old;
                }

                @Override
                public void visit(PathExpression expression) {
                    expression.setCollectionKeyPath(isKeyFunction);
                    super.visit(expression);
                }

            });
            joinNode.setOnPredicate((AndPredicate) predicate);
        }
    }
}
