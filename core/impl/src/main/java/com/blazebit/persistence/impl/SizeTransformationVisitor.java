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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type.PersistenceType;

import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PathReference;
import com.blazebit.persistence.impl.expression.PredicateModifyingResultVisitorAdapter;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.SimplePathReference;
import com.blazebit.persistence.impl.expression.Subquery;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.util.MetamodelUtils;
import com.blazebit.persistence.impl.util.PropertyUtils;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @author Moritz Becker
 */
public class SizeTransformationVisitor extends PredicateModifyingResultVisitorAdapter {

    private final Metamodel metamodel;
    private final AliasManager aliasManager;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final JoinManager joinManager;
    private final GroupByManager groupByManager;
    private final Map<String, String> properties;
    private boolean hasGroupBySelects;
    private boolean hasComplexGroupBySelects;
    private final DbmsDialect dbmsDialect;
//    private final SelectManager<?> selectManager;
    
    // state
    private boolean orderBySelectClause;
    private boolean distinctRequired;
    private ClauseType clause;
    private final Set<AggregateExpression> transformedExpressions = new HashSet<AggregateExpression>();
    // maps absolute paths to join nodes
    private final Map<String, PathReference> generatedJoins = new HashMap<String, PathReference>();
    
    public SizeTransformationVisitor(MainQuery mainQuery, AliasManager aliasManager, SubqueryInitiatorFactory subqueryInitFactory, JoinManager joinManager, GroupByManager groupByManager, DbmsDialect dbmsDialect) {
        this.metamodel = mainQuery.em.getMetamodel();
        this.aliasManager = aliasManager;
        this.subqueryInitFactory = subqueryInitFactory;
        this.joinManager = joinManager;
        this.groupByManager = groupByManager;
        this.properties = mainQuery.properties;
        this.dbmsDialect = dbmsDialect;
    }
    
    public ClauseType getClause() {
        return clause;
    }
    
    public boolean isHasComplexGroupBySelects() {
		return hasComplexGroupBySelects;
	}

	public void setHasComplexGroupBySelects(boolean hasComplexGroupBySelects) {
		this.hasComplexGroupBySelects = hasComplexGroupBySelects;
	}
	
	public boolean isHasGroupBySelects() {
		return hasGroupBySelects;
	}

	public void setHasGroupBySelects(boolean hasGroupBySelects) {
		this.hasGroupBySelects = hasGroupBySelects;
	}

	public void setClause(ClauseType clause) {
        this.clause = clause;
    }
    
    public boolean isOrderBySelectClause() {
        return orderBySelectClause;
    }
    
    public void setOrderBySelectClause(boolean orderBySelectClause) {
        this.orderBySelectClause = orderBySelectClause;
    }
    
    @Override
    public Expression visit(WhenClauseExpression expression) {
        expression.getCondition().accept(this);
        expression.setResult(expression.getResult().accept(this));
        return expression;
    }

    @Override
    public Expression visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.get(i).accept(this);
        }
        expression.setDefaultExpr(expression.getDefaultExpr().accept(this));
        return expression;
    }

    @Override
    public Expression visit(SimpleCaseExpression expression) {
        return visit((GeneralCaseExpression) expression);
    }
    
    @Override
    public Expression visit(PathExpression expression) {
        if (orderBySelectClause) {
            ((JoinNode) expression.getBaseNode()).getClauseDependencies().add(ClauseType.ORDER_BY);
        }
        return expression;
    }
    
    private boolean isCountTransformationEnabled() {
    	return PropertyUtils.getAsBooleanProperty(properties, ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION, true);
    }
    
    private boolean isImplicitGroupByFromSelectEnabled() {
    	return PropertyUtils.getAsBooleanProperty(properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT, true);
    }

    @Override
    public Expression visit(FunctionExpression expression) {
        if (ExpressionUtils.isSizeFunction(expression) && clause != ClauseType.WHERE) {
            PathExpression sizeArg = (PathExpression) expression.getExpressions().get(0);
            Class<?> startClass = ((JoinNode) sizeArg.getBaseNode()).getPropertyClass();

            ManagedType<?> managedTargetType = MetamodelUtils.resolveManagedTargetType(metamodel, startClass, sizeArg.getPathReference().getField());
            
            PersistenceType collectionIdType;
            if (managedTargetType instanceof EntityType<?>) {
            	collectionIdType = ((EntityType<?>) managedTargetType).getIdType().getPersistenceType();
            } else {
            	throw new RuntimeException("Path [" + sizeArg.toString() + "] does not refer to a collection");
            }
            
            if (collectionIdType == PersistenceType.EMBEDDABLE || 
            		!metamodel.entity(startClass).hasSingleIdAttribute() || 
            		joinManager.getRoots().size() > 1 || 
            		clause == ClauseType.JOIN || 
            		!isCountTransformationEnabled() || 
            		(hasComplexGroupBySelects && !dbmsDialect.supportsComplexGroupBy()) || 
            		(hasGroupBySelects && !isImplicitGroupByFromSelectEnabled())) {
                return generateSubquery(sizeArg, startClass);
            } else {
                // build group by id clause
                List<PathElementExpression> pathElementExpr = new ArrayList<PathElementExpression>();
                List<JoinNode> roots = joinManager.getRoots();
                
                String rootAlias = roots.get(0).getAliasInfo().getAlias();
                String rootId = JpaUtils.getIdAttribute(metamodel.entity(roots.get(0).getPropertyClass())).getName();
                pathElementExpr.add(new PropertyExpression(rootAlias));
                pathElementExpr.add(new PropertyExpression(rootId));
                PathExpression groupByExpr = new PathExpression(pathElementExpr);
                joinManager.implicitJoin(groupByExpr, true, null, false, false, false);
                
                if (groupByManager.hasGroupBys() && !groupByManager.existsGroupBy(groupByExpr)) {
                    return generateSubquery(sizeArg, startClass);
                }
                
                // join
                sizeArg.setUsedInCollectionFunction(false);
                AggregateExpression countExpr = new AggregateExpression(distinctRequired, "COUNT", expression.getExpressions());
                transformedExpressions.add(countExpr);
                
                JoinNode originalNode = (JoinNode) sizeArg.getBaseNode();
                String nodeLookupKey = originalNode.getAliasInfo().getAbsolutePath() + "." + sizeArg.getField();
                PathReference generatedJoin = generatedJoins.get(nodeLookupKey);
                if (generatedJoin == null) { 
                    joinManager.implicitJoin(sizeArg, true, clause, false, false, true);
                    generatedJoin = sizeArg.getPathReference();
                    generatedJoins.put(((JoinNode) generatedJoin.getBaseNode()).getAliasInfo().getAbsolutePath(), generatedJoin);
                } else {
                    sizeArg.setPathReference(new SimplePathReference(generatedJoin.getBaseNode(), generatedJoin.getField()));
                }
                
                if (distinctRequired == false) { 
                    if(joinManager.getCollectionJoins().size() > 1) {
                        distinctRequired = true;
                        /**
                         *  As soon as we encounter another collection join, set previously 
                         *  performed transformations to distinct.
                         */
                        for (AggregateExpression aggregateExpr : transformedExpressions) {
                            aggregateExpr.setDistinct(true);
                        }
                    }
                }
                
                groupByManager.groupBy(groupByExpr);
                super.visit(expression);
                
                return countExpr;
            }
        } else {
            super.visit(expression);
        }
        return expression;
    }
    
    private SubqueryExpression generateSubquery(PathExpression sizeArg, Class<?> collectionClass) {
        String baseAlias = ((JoinNode) sizeArg.getBaseNode()).getAliasInfo().getAlias();
        String collectionPropertyName = sizeArg.getField() != null ? sizeArg.getField() : baseAlias;
        String collectionPropertyAlias = collectionPropertyName.replace('.', '_');
        String collectionPropertyClassName = collectionClass.getSimpleName().toLowerCase();
        String collectionPropertyClassAlias = collectionPropertyClassName;

        if (aliasManager.getAliasInfo(collectionPropertyClassName) != null) {
            collectionPropertyClassAlias = aliasManager.generatePostfixedAlias(collectionPropertyClassName);
        }
        if (aliasManager.getAliasInfo(collectionPropertyName) != null) {
            collectionPropertyAlias = aliasManager.generatePostfixedAlias(collectionPropertyName);
        }
        
        Subquery countSubquery = (Subquery) subqueryInitFactory.createSubqueryInitiator(null, new SubqueryBuilderListenerImpl<Object>())
            .from(collectionClass, collectionPropertyClassAlias)
            .select(new StringBuilder("COUNT(").append(collectionPropertyAlias).append(")").toString())
            .leftJoin(new StringBuilder(collectionPropertyClassAlias).append('.').append(collectionPropertyName).toString(), collectionPropertyAlias)
            .where(collectionPropertyClassAlias)
            .eqExpression(baseAlias);


        return new SubqueryExpression(countSubquery);
    }
}
