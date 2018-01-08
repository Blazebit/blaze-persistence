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

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.AttributeHolder;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.JoinManager;
import com.blazebit.persistence.impl.JoinNode;
import com.blazebit.persistence.impl.JpaUtils;
import com.blazebit.persistence.impl.MainQuery;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionModifierCollectingResultVisitorAdapter;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.ListIndexExpression;
import com.blazebit.persistence.impl.expression.MapKeyExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.StringLiteral;
import com.blazebit.persistence.impl.expression.Subquery;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.impl.util.ExpressionUtils;
import com.blazebit.persistence.impl.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;
import java.util.*;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SizeTransformationVisitor extends ExpressionModifierCollectingResultVisitorAdapter {

    private static final Set<PersistenceType> IDENTIFIABLE_PERSISTENCE_TYPES = EnumSet.of(PersistenceType.ENTITY, PersistenceType.MAPPED_SUPERCLASS);

    private final MainQuery mainQuery;
    private final EntityMetamodel metamodel;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final JoinManager joinManager;
    private final JpaProvider jpaProvider;

    // state
    private boolean orderBySelectClause;
    private boolean distinctRequired;
    private ClauseType clause;
    private final Set<TransformedExpressionEntry> transformedExpressions = new HashSet<TransformedExpressionEntry>();
    // maps absolute paths to late join entries
    private final Map<String, LateJoinEntry> lateJoins = new HashMap<String, LateJoinEntry>();
    private final Set<String> requiredGroupBys = new LinkedHashSet<>();
    private final Set<String> subqueryGroupBys = new LinkedHashSet<>();
    private JoinNode currentJoinNode;
    // size expressions with arguments having a blacklisted base node will become subqueries
    private Set<JoinNode> joinNodeBlacklist = new HashSet<>();

    public SizeTransformationVisitor(MainQuery mainQuery, SubqueryInitiatorFactory subqueryInitFactory, JoinManager joinManager, JpaProvider jpaProvider) {
        this.mainQuery = mainQuery;
        this.metamodel = mainQuery.getMetamodel();
        this.subqueryInitFactory = subqueryInitFactory;
        this.joinManager = joinManager;
        this.jpaProvider = jpaProvider;
    }
    
    public ClauseType getClause() {
        return clause;
    }

    public void setClause(ClauseType clause) {
        this.clause = clause;
    }

    public void setOrderBySelectClause(boolean orderBySelectClause) {
        this.orderBySelectClause = orderBySelectClause;
    }

    public Map<String, LateJoinEntry> getLateJoins() {
        return lateJoins;
    }

    public Set<String> getRequiredGroupBys() {
        return requiredGroupBys;
    }

    public Set<String> getSubqueryGroupBys() {
        return subqueryGroupBys;
    }

    private boolean isCountTransformationEnabled() {
        return mainQuery.getQueryConfiguration().isCountTransformationEnabled();
    }
    
    @Override
    public Boolean visit(PathExpression expression) {
        if (orderBySelectClause) {
            LateJoinEntry lateJoinEntry = lateJoins.get(getJoinLookupKey(expression));
            if (lateJoinEntry != null) {
                lateJoinEntry.getClauseDependencies().add(ClauseType.ORDER_BY);
            }
        }
        if (clause == ClauseType.SELECT) {
            // for the select clause we blacklist all the join nodes that are required by other select items
            JoinNode current = (JoinNode) expression.getBaseNode();
            while (current != null) {
                joinNodeBlacklist.add(current);
                current = current.getParent();
            }
        }
        return super.visit(expression);
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        if (clause != ClauseType.WHERE && com.blazebit.persistence.impl.util.ExpressionUtils.isSizeFunction(expression)) {
            return true;
        }
        return super.visit(expression);
    }

    protected void onModifier(ExpressionModifier parentModifier) {
        PathExpression sizeArg = (PathExpression) ((FunctionExpression) parentModifier.get()).getExpressions().get(0);
        parentModifier.set(getSizeExpression(parentModifier, sizeArg));
        sizeArg.accept(this);
    }

    private boolean requiresBlacklistedNode(PathExpression sizeArg) {
        JoinNode sizeArgBaseNode = (JoinNode) sizeArg.getBaseNode();
        if (joinNodeBlacklist.contains(sizeArgBaseNode)) {
            return sizeArgBaseNode.getNodes().keySet().contains(sizeArg.getField());
        } else {
            return false;
        }
    }

    private Expression getSizeExpression(ExpressionModifier parentModifier, PathExpression sizeArg) {
        JoinNode sizeArgJoin = (JoinNode) sizeArg.getBaseNode();
        String property = sizeArg.getPathReference().getField();
        final Type<?> nodeType = ((JoinNode) sizeArg.getBaseNode()).getBaseType();
        if (!(nodeType instanceof EntityType<?>)) {
            throw new IllegalArgumentException("Size on a collection owned by a non-entity type is not supported yet: " + sizeArg);
        }
        final EntityType<?> startType = (EntityType<?>) nodeType;

        AttributeHolder result = JpaUtils.getAttributeForJoining(metamodel, sizeArg);
        PluralAttribute<?, ?, ?> targetAttribute = (PluralAttribute<?, ?, ?>) result.getAttribute();
        if (targetAttribute == null) {
            throw new RuntimeException("Attribute [" + property + "] not found on class " + startType.getJavaType().getName());
        }
        final PluralAttribute.CollectionType collectionType = targetAttribute.getCollectionType();
        final boolean isElementCollection = targetAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION;

        boolean subqueryRequired;
        if (isElementCollection) {
            subqueryRequired = false;
        } else {
            ManagedType<?> managedTargetType = JpaMetamodelUtils.getManagedType(metamodel, result.getAttributeJavaType(), null);
            if (managedTargetType instanceof EntityType<?>) {
                // we could also generate counts for collections with embeddable id but we do not implement this for now
                subqueryRequired = ((EntityType<?>) managedTargetType).getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE;
            } else {
                throw new RuntimeException("Path [" + sizeArg.toString() + "] does not refer to a collection");
            }
        }

        // build group by id clause
        List<PathElementExpression> pathElementExpr = new ArrayList<PathElementExpression>();
        String rootId = JpaMetamodelUtils.getIdAttribute(startType).getName();
        pathElementExpr.add(new PropertyExpression(sizeArgJoin.getAlias()));
        pathElementExpr.add(new PropertyExpression(rootId));
        PathExpression groupByExpr = new PathExpression(pathElementExpr);
        String groupByExprString = groupByExpr.toString();

        subqueryRequired = subqueryRequired ||
                // we could also generate counts for collections with IdClass attributes but we do not implement this for now
                !startType.hasSingleIdAttribute() ||
                joinManager.getRoots().size() > 1 ||
                clause == ClauseType.JOIN ||
                !isCountTransformationEnabled() ||
                // a subquery is required for bags when other collections are joined as well because we cannot rely on distinctness for bags
                // for now, we always generate a subquery when a bag is encountered
                jpaProvider.isBag((EntityType<?>) targetAttribute.getDeclaringType(), targetAttribute.getName()) ||
                requiresBlacklistedNode(sizeArg);

        if (subqueryRequired) {
            return generateSubquery(sizeArg);
        } else {
            if (currentJoinNode != null &&
                    (!currentJoinNode.equals(sizeArgJoin))) {
                int currentJoinDepth = currentJoinNode.getJoinDepth();
                int sizeArgJoinDepth = sizeArgJoin.getJoinDepth();
                if (currentJoinDepth > sizeArgJoinDepth) {
                    return generateSubquery(sizeArg);
                } else {
                    // we have to change all transformed expressions to subqueries
                    for (TransformedExpressionEntry transformedExpressionEntry : transformedExpressions) {
                        PathExpression originalSizeArg = transformedExpressionEntry.getOriginalSizeArg();
                        transformedExpressionEntry.getParentModifier().set(generateSubquery(originalSizeArg));
                    }
                    transformedExpressions.clear();
                    requiredGroupBys.clear();
                    lateJoins.clear();
                    distinctRequired = false;

                    if (currentJoinDepth == sizeArgJoinDepth) {
                        return generateSubquery(sizeArg);
                    }
                }
            }

            joinManager.implicitJoin(groupByExpr, true, null, null, false, false, false, false);

            PathExpression originalSizeArg = (PathExpression) sizeArg.clone(false);
            originalSizeArg.setPathReference(sizeArg.getPathReference());

            sizeArg.setUsedInCollectionFunction(false);

            List<Expression> countArguments = new ArrayList<Expression>();

            Expression keyExpression;
            if ((isElementCollection && collectionType != PluralAttribute.CollectionType.MAP)
                    || collectionType == PluralAttribute.CollectionType.SET) {
                if (IDENTIFIABLE_PERSISTENCE_TYPES.contains(targetAttribute.getElementType().getPersistenceType()) && targetAttribute.isCollection()) {
                    // append id attribute name of joinable size argument
                    PluralAttribute<?, ?, ?> sizeArgTargetAttribute = (PluralAttribute<?, ?, ?>) JpaMetamodelUtils.getAttribute(startType, sizeArg.getPathReference().getField());
                    Attribute<?, ?> idAttribute = JpaMetamodelUtils.getIdAttribute(((IdentifiableType<?>) sizeArgTargetAttribute.getElementType()));
                    sizeArg.getExpressions().add(new PropertyExpression(idAttribute.getName()));
                }

                keyExpression = sizeArg;
            } else {
                sizeArg.setCollectionKeyPath(true);
                if (collectionType == PluralAttribute.CollectionType.LIST) {
                    keyExpression = new ListIndexExpression(sizeArg);
                } else {
                    keyExpression = new MapKeyExpression(sizeArg);
                }
            }
            countArguments.add(keyExpression);

            AggregateExpression countExpr = createCountFunction(distinctRequired, countArguments);
            transformedExpressions.add(new TransformedExpressionEntry(countExpr, originalSizeArg, parentModifier));

            String joinLookupKey = getJoinLookupKey(sizeArg);
            LateJoinEntry lateJoin = lateJoins.get(joinLookupKey);
            if (lateJoin == null) {
                lateJoin = new LateJoinEntry();
                lateJoins.put(joinLookupKey, lateJoin);
            }
            lateJoin.getPathsToJoin().add(sizeArg);
            lateJoin.getClauseDependencies().add(clause);

            currentJoinNode = (JoinNode) originalSizeArg.getBaseNode();

            if (!distinctRequired) {
                if (lateJoins.size() + joinManager.getCollectionJoins().size() > 1) {
                    distinctRequired = true;
                    /**
                     *  As soon as we encounter another collection join, set previously
                     *  performed transformations to distinct.
                     */
                    for (TransformedExpressionEntry transformedExpressionEntry : transformedExpressions) {
                        AggregateExpression transformedExpr = transformedExpressionEntry.getTransformedExpression();
                        if (ExpressionUtils.isCustomFunctionInvocation(transformedExpr) &&
                            AbstractCountFunction.FUNCTION_NAME.equalsIgnoreCase(((StringLiteral) transformedExpr.getExpressions().get(0)).getValue())) {
                            Expression possibleDistinct = transformedExpr.getExpressions().get(1);
                            if (!(possibleDistinct instanceof StringLiteral) || !AbstractCountFunction.DISTINCT_QUALIFIER.equals(((StringLiteral) possibleDistinct).getValue())) {
                                transformedExpr.getExpressions().add(1, new StringLiteral(AbstractCountFunction.DISTINCT_QUALIFIER));
                            }
                        } else {
                            transformedExpr.setDistinct(true);
                        }
                    }
                }
            }

            requiredGroupBys.add(groupByExprString);

            return countExpr;
        }
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

    private SubqueryExpression generateSubquery(PathExpression sizeArg) {
        JoinNode sizeArgJoin = (JoinNode) sizeArg.getBaseNode();
        final Type<?> nodeType = sizeArgJoin.getNodeType();
        if (!(nodeType instanceof EntityType<?>)) {
            throw new IllegalArgumentException("Size on a collection owned by a non-entity type is not supported yet: " + sizeArg);
        }
        final EntityType<?> startType = (EntityType<?>) nodeType;

        Subquery countSubquery = (Subquery) subqueryInitFactory.createSubqueryInitiator(null, new SubqueryBuilderListenerImpl<Object>(), false)
                .from(sizeArg.clone(true).toString())
                .select("COUNT(*)");

        List<PathElementExpression> pathElementExpr = new ArrayList<PathElementExpression>();
        String rootId = JpaMetamodelUtils.getIdAttribute(startType).getName();
        pathElementExpr.add(new PropertyExpression(sizeArgJoin.getAlias()));
        pathElementExpr.add(new PropertyExpression(rootId));
        PathExpression groupByExpr = new PathExpression(pathElementExpr);
        String groupByExprString = groupByExpr.toString();

        subqueryGroupBys.add(groupByExprString);
        return new SubqueryExpression(countSubquery);
    }

    private static class TransformedExpressionEntry {
        private final AggregateExpression transformedExpression;
        private final PathExpression originalSizeArg;
        private final ExpressionModifier parentModifier;

        public TransformedExpressionEntry(AggregateExpression transformedExpression, PathExpression originalSizeArg, ExpressionModifier parentModifier) {
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

        public ExpressionModifier getParentModifier() {
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
