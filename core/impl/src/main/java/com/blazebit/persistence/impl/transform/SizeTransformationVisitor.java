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
package com.blazebit.persistence.impl.transform;

import java.util.*;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.impl.GroupByManager;
import com.blazebit.persistence.impl.JoinManager;
import com.blazebit.persistence.impl.JoinNode;
import com.blazebit.persistence.impl.JpaUtils;
import com.blazebit.persistence.impl.MainQuery;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionModifyingResultVisitorAdapter;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimplePathReference;
import com.blazebit.persistence.impl.expression.StringLiteral;
import com.blazebit.persistence.impl.expression.Subquery;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.impl.util.ExpressionUtils;
import com.blazebit.persistence.impl.util.MetamodelUtils;
import com.blazebit.persistence.impl.util.PropertyUtils;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpaProvider;

/**
 *
 * @author Moritz Becker
 */
public class SizeTransformationVisitor extends ExpressionModifyingResultVisitorAdapter {

    private final Metamodel metamodel;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final JoinManager joinManager;
    private final GroupByManager groupByManager;
    private final Map<String, String> properties;
    private boolean hasGroupBySelects;
    private boolean hasComplexGroupBySelects;
    private final DbmsDialect dbmsDialect;
    private final JpaProvider jpaProvider;

    // state
    private boolean orderBySelectClause;
    private boolean distinctRequired;
    private ClauseType clause;
    private final Set<TransformedExpressionEntry> transformedExpressions = new HashSet<TransformedExpressionEntry>();
    // maps absolute paths to late join entries
    private final Map<String, LateJoinEntry> lateJoins = new HashMap<String, LateJoinEntry>();
    private final Set<PathExpression> requiredGroupBys = new LinkedHashSet<PathExpression>();
    private final Set<PathExpression> requiredGroupBysIfOtherGroupBys = new LinkedHashSet<PathExpression>();
    private JoinNode currentJoinNode;

    public SizeTransformationVisitor(MainQuery mainQuery, SubqueryInitiatorFactory subqueryInitFactory, JoinManager joinManager, GroupByManager groupByManager, DbmsDialect dbmsDialect, JpaProvider jpaProvider) {
        this.metamodel = mainQuery.getEm().getMetamodel();
        this.subqueryInitFactory = subqueryInitFactory;
        this.joinManager = joinManager;
        this.groupByManager = groupByManager;
        this.properties = mainQuery.getProperties();
        this.dbmsDialect = dbmsDialect;
        this.jpaProvider = jpaProvider;
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

    public void setParentModifier(ExpressionModifier<Expression> parentModifier) {
        this.parentModifier = parentModifier;
    }

    public Map<String, LateJoinEntry> getLateJoins() {
        return lateJoins;
    }

    public Set<PathExpression> getRequiredGroupBys() {
        return requiredGroupBys;
    }

    public Set<PathExpression> getRequiredGroupBysIfOtherGroupBys() {
        return requiredGroupBysIfOtherGroupBys;
    }

    private boolean isCountTransformationEnabled() {
        return PropertyUtils.getAsBooleanProperty(properties, ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION, true);
    }
    
    private boolean isImplicitGroupByFromSelectEnabled() {
        return PropertyUtils.getAsBooleanProperty(properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT, true);
    }

    @Override
    public Expression visit(PathExpression expression) {
        if (orderBySelectClause) {
            lateJoins.get(getJoinLookupKey(expression)).getClauseDependencies().add(ClauseType.ORDER_BY);
        }
        return expression;
    }

    @Override
    public Expression visit(FunctionExpression expression) {
        if (com.blazebit.persistence.impl.util.ExpressionUtils.isSizeFunction(expression) && clause != ClauseType.WHERE) {
            PathExpression sizeArg = (PathExpression) expression.getExpressions().get(0);
            JoinNode sizeArgJoin = (JoinNode) sizeArg.getBaseNode();
            String property = sizeArg.getPathReference().getField();
            Class<?> startClass = ((JoinNode) sizeArg.getBaseNode()).getPropertyClass();

            PluralAttribute<?, ?, ?> targetAttribute = (PluralAttribute<?, ?, ?>) MetamodelUtils.resolveTargetAttribute(metamodel, startClass, property);
            if (targetAttribute == null) {
                throw new RuntimeException("Attribute [" + property + "] not found on class " + startClass.getName());
            }
            PluralAttribute.CollectionType collectionType = targetAttribute.getCollectionType();
            boolean isElementCollection = targetAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
            EntityType<?> startType = metamodel.entity(startClass);

            boolean subqueryRequired;
            if (isElementCollection) {
                subqueryRequired = false;
            } else {
                ManagedType<?> managedTargetType = MetamodelUtils.resolveManagedTargetType(metamodel, startClass, property);
                if (managedTargetType instanceof EntityType<?>) {
                    subqueryRequired = ((EntityType<?>) managedTargetType).getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE;
                } else {
                    throw new RuntimeException("Path [" + sizeArg.toString() + "] does not refer to a collection");
                }
            }

            // build group by id clause
            List<PathElementExpression> pathElementExpr = new ArrayList<PathElementExpression>();
            String rootId = JpaUtils.getIdAttribute(metamodel.entity(startClass)).getName();
            pathElementExpr.add(new PropertyExpression(sizeArgJoin.getAlias()));
            pathElementExpr.add(new PropertyExpression(rootId));
            PathExpression groupByExpr = new PathExpression(pathElementExpr);

            if (groupByManager.hasGroupBys()) {
                groupByManager.groupBy(groupByExpr);
            } else {
                requiredGroupBysIfOtherGroupBys.add(groupByExpr);
            }

            subqueryRequired = subqueryRequired ||
                    !metamodel.entity(startClass).hasSingleIdAttribute() ||
                    joinManager.getRoots().size() > 1 ||
                    clause == ClauseType.JOIN ||
                    !isCountTransformationEnabled() ||
                    (hasComplexGroupBySelects && !dbmsDialect.supportsComplexGroupBy()) ||
                    (hasGroupBySelects && !isImplicitGroupByFromSelectEnabled()) ||
                    jpaProvider.isBag(targetAttribute);

            if (subqueryRequired) {
                return generateSubquery(sizeArg, startClass);
            } else {
                if (currentJoinNode != null &&
                        (!currentJoinNode.equals(sizeArgJoin))) {
                    int currentJoinDepth = currentJoinNode.getJoinDepth();
                    int sizeArgJoinDepth = sizeArgJoin.getJoinDepth();
                    if (currentJoinDepth > sizeArgJoinDepth) {
                        return generateSubquery(sizeArg, startClass);
                    } else {
                        // we have to change all transformed expressions to subqueries
                        for (TransformedExpressionEntry transformedExpressionEntry : transformedExpressions) {
                            PathExpression originalSizeArg = transformedExpressionEntry.getOriginalSizeArg();
                            Class<?> originalStartClass = ((JoinNode) originalSizeArg.getBaseNode()).getPropertyClass();
                            transformedExpressionEntry.getParentModifier().set(generateSubquery(originalSizeArg, originalStartClass));
                        }
                        transformedExpressions.clear();
                        requiredGroupBys.clear();
                        lateJoins.clear();
                        distinctRequired = false;

                        if (currentJoinDepth == sizeArgJoinDepth) {
                            return generateSubquery(sizeArg, startClass);
                        }
                    }
                }

                joinManager.implicitJoin(groupByExpr, true, null, null, false, false, false);

                PathExpression originalSizeArg = sizeArg.clone();

                sizeArg.setUsedInCollectionFunction(false);

                String alias = ((JoinNode) sizeArg.getPathReference().getBaseNode()).getAlias();
                String id = JpaUtils.getIdAttribute(startType).getName();

                List<PathElementExpression> pathElems = new ArrayList<PathElementExpression>();
                pathElems.add(new PropertyExpression(alias));
                pathElems.add(new PropertyExpression(id));
                PathExpression parentIdPath = new PathExpression(pathElems);
                parentIdPath.setPathReference(new SimplePathReference(sizeArg.getPathReference().getBaseNode(), id, null));

                List<Expression> countArguments = new ArrayList<Expression>();

                Expression keyExpression;
                if ((targetAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION && collectionType != PluralAttribute.CollectionType.MAP)
                        || collectionType == PluralAttribute.CollectionType.SET) {
                    keyExpression = sizeArg;
                } else {
                    final String keyOrIndexFunctionName;
                    List<Expression> keyArg = new ArrayList<Expression>(1);

                    sizeArg.setCollectionKeyPath(true);
                    keyArg.add(sizeArg);
                    if (collectionType == PluralAttribute.CollectionType.LIST) {
                        keyOrIndexFunctionName = "INDEX";
                    } else {
                        keyOrIndexFunctionName = "KEY";
                    }
                    keyExpression = new FunctionExpression(keyOrIndexFunctionName, keyArg);
                }
                countArguments.add(keyExpression);

                AggregateExpression countExpr = createCountFunction(distinctRequired, countArguments);
                transformedExpressions.add(new TransformedExpressionEntry(countExpr, originalSizeArg, (ExpressionModifier<Expression>) parentModifier.clone()));

                String joinLookupKey = getJoinLookupKey(sizeArg);
                LateJoinEntry lateJoin = lateJoins.get(joinLookupKey);
                if (lateJoin == null) {
                    lateJoin = new LateJoinEntry();
                    lateJoins.put(joinLookupKey, lateJoin);
                }
                lateJoin.getPathsToJoin().add(sizeArg);
                lateJoin.getClauseDependencies().add(clause);

                currentJoinNode = (JoinNode) originalSizeArg.getBaseNode();
                
                if (distinctRequired == false) {
                    if (lateJoins.size() + joinManager.getCollectionJoins().size() > 1) {
                        distinctRequired = true;
                        /**
                         *  As soon as we encounter another collection join, set previously 
                         *  performed transformations to distinct.
                         */
                        for (TransformedExpressionEntry transformedExpressionEntry: transformedExpressions) {
                            AggregateExpression transformedExpr = transformedExpressionEntry.getTransformedExpression();
                            if (ExpressionUtils.isCustomFunctionInvocation(transformedExpr) &&
                                    AbstractCountFunction.FUNCTION_NAME.equalsIgnoreCase(((StringLiteral) transformedExpr.getExpressions().get(0)).getValue())) {
                                // AbstractCountFunction
                                if (!AbstractCountFunction.DISTINCT_QUALIFIER.equals(transformedExpr.getExpressions().get(1))) {
                                    transformedExpr.getExpressions().add(1, new StringLiteral(AbstractCountFunction.DISTINCT_QUALIFIER));
                                }
                            } else {
                                transformedExpr.setDistinct(true);
                            }
                        }
                    }
                }

                requiredGroupBys.add(groupByExpr);
                super.visit(expression);
                
                return countExpr;
            }
        } else {
            super.visit(expression);
        }
        return expression;
    }

    private String getJoinLookupKey(PathExpression pathExpression) {
        JoinNode originalNode = (JoinNode) pathExpression.getBaseNode();
        return originalNode.getAliasInfo().getAbsolutePath() + "." + pathExpression.getField();
    }

    private AggregateExpression createCountFunction(boolean distinct, List<Expression> countTupleArguments) {
        countTupleArguments.add(0, new StringLiteral(AbstractCountFunction.FUNCTION_NAME.toUpperCase()));
        if (distinct) {
            countTupleArguments.add(1, new StringLiteral(AbstractCountFunction.DISTINCT_QUALIFIER));
        }
        return new AggregateExpression(false, "FUNCTION", countTupleArguments);
    }

    private SubqueryExpression generateSubquery(PathExpression sizeArg, Class<?> collectionClass) {
        Subquery countSubquery = (Subquery) subqueryInitFactory.createSubqueryInitiator(null, new SubqueryBuilderListenerImpl<Object>())
                .from(sizeArg.getPath())
                .select("COUNT(*)");

        return new SubqueryExpression(countSubquery);
    }

    private static class TransformedExpressionEntry {
        private final AggregateExpression transformedExpression;
        private final PathExpression originalSizeArg;
        private final ExpressionModifier<Expression> parentModifier;

        public TransformedExpressionEntry(AggregateExpression transformedExpression, PathExpression originalSizeArg, ExpressionModifier<Expression> parentModifier) {
            this.transformedExpression = transformedExpression;
            this.originalSizeArg = originalSizeArg;
            this.parentModifier = parentModifier;
        }

        public AggregateExpression getTransformedExpression() {
            return transformedExpression;
        }

        public PathExpression getOriginalSizeArg() {
            return originalSizeArg;
        }

        public ExpressionModifier<Expression> getParentModifier() {
            return parentModifier;
        }
    }

    static class LateJoinEntry {
        private final EnumSet<ClauseType> clauseDependencies = EnumSet.noneOf(ClauseType.class);
        private final List<PathExpression> pathsToJoin = new ArrayList<PathExpression>();

        public EnumSet<ClauseType> getClauseDependencies() {
            return clauseDependencies;
        }

        public List<PathExpression> getPathsToJoin() {
            return pathsToJoin;
        }
    }
}
