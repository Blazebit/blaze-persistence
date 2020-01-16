/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.BaseFinalSetOperationBuilder;
import com.blazebit.persistence.impl.function.exist.ExistFunction;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.AggregateExpression;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.OrderByItem;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.WindowDefinition;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.GePredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.LikePredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.parser.util.TypeConverter;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ResolvingQueryGenerator extends SimpleQueryGenerator {

    private static final Set<String> BUILT_IN_FUNCTIONS;

    protected String aliasPrefix;
    private boolean resolveSelectAliases = true;
    private boolean externalRepresentation;
    private Set<JoinNode> renderedJoinNodes;
    private ClauseType clauseType;
    private Map<JoinNode, Boolean> treatedJoinNodesForConstraints;
    private final EntityMetamodel entityMetamodel;
    private final Set<String> currentlyResolvingAliases;
    private final AliasManager aliasManager;
    private final ParameterManager parameterManager;
    private final AssociationParameterTransformerFactory parameterTransformerFactory;
    private final JpaProvider jpaProvider;
    private final Map<String, JpqlFunction> registeredFunctions;
    private final Map<String, String> registeredFunctionsNames;

    static {
        Set<String> functions = new HashSet<>();
        functions.add("concat");
        functions.add("substring");
        functions.add("lower");
        functions.add("upper");
        functions.add("length");
        functions.add("locate");
        functions.add("abs");
        functions.add("sqrt");
        functions.add("mod");
        functions.add("coalesce");
        functions.add("nullif");
        functions.add("size");
        functions.add("type");
        functions.add("avg");
        functions.add("max");
        functions.add("min");
        functions.add("sum");
        functions.add("count");
        functions.add("current_date");
        functions.add("current_time");
        functions.add("current_timestamp");
        BUILT_IN_FUNCTIONS = functions;
    }

    public ResolvingQueryGenerator(EntityMetamodel entityMetamodel, AliasManager aliasManager, ParameterManager parameterManager, AssociationParameterTransformerFactory parameterTransformerFactory, JpaProvider jpaProvider, Map<String, JpqlFunction> registeredFunctions) {
        this.entityMetamodel = entityMetamodel;
        this.aliasManager = aliasManager;
        this.parameterManager = parameterManager;
        this.parameterTransformerFactory = parameterTransformerFactory;
        this.jpaProvider = jpaProvider;
        this.registeredFunctions = registeredFunctions;
        this.currentlyResolvingAliases = new HashSet<>();
        this.registeredFunctionsNames = new HashMap<>(registeredFunctions.size());
        for (Map.Entry<String, JpqlFunction> registeredFunctionEntry : registeredFunctions.entrySet()) {
            registeredFunctionsNames.put(registeredFunctionEntry.getKey().toLowerCase(), registeredFunctionEntry.getKey());
        }
    }

    @Override
    public void generate(Expression expression) {
        // Top level null expressions might need to be rendered as nullif because of lacking support in most JPA provider query languages
        if (expression instanceof NullExpression) {
            // The SET clause always needs the NULL literal
            if (clauseType != ClauseType.SET) {
                if (externalRepresentation) {
                    sb.append("NULL");
                } else {
                    sb.append(jpaProvider.getNullExpression());
                }
                return;
            }
        }
        expression.accept(this);
    }

    @Override
    public void visit(MapValueExpression expression) {
        // NOTE: We decide if we need a VALUE wrapper during the rendering of the path expression anyway
        expression.getPath().accept(this);
    }

    @Override
    public void visit(FunctionExpression expression) {
        if (externalRepresentation && expression.getRealArgument() != null) {
            expression.getRealArgument().accept(this);
            return;
        }
        // A type constraint of a treat expression from within an aggregate may not "escape" the aggregate
        Map<JoinNode, Boolean> oldTreatedJoinNodesForConstraints = treatedJoinNodesForConstraints;
        if (expression instanceof AggregateExpression) {
            treatedJoinNodesForConstraints = null;
        }

        if (com.blazebit.persistence.parser.util.ExpressionUtils.isOuterFunction(expression)) {
            // Outer can only have paths, no need to set expression context for parameters
            expression.getExpressions().get(0).accept(this);
        } else if (ExpressionUtils.isFunctionFunctionExpression(expression)) {
            final List<Expression> arguments = expression.getExpressions();
            final String literalFunctionName = ExpressionUtils.unwrapStringLiteral(arguments.get(0).toString());
            String resolvedFunctionName = resolveRenderedFunctionName(literalFunctionName);

            final List<Expression> argumentsWithoutFunctionName;
            if (arguments.size() > 1) {
                argumentsWithoutFunctionName = arguments.subList(1, arguments.size());
            } else {
                argumentsWithoutFunctionName = Collections.emptyList();
            }
            renderFunctionFunction(resolvedFunctionName, argumentsWithoutFunctionName, expression.getResolvedWindowDefinition());
        } else if (isCountStarFunction(expression)) {
            renderCountStar(expression.getResolvedWindowDefinition());
        } else if (BUILT_IN_FUNCTIONS.contains(expression.getFunctionName().toLowerCase()) && expression.getResolvedWindowDefinition() == null) {
            super.visit(expression);
        } else {
            renderFunctionFunction(resolveRenderedFunctionName(expression.getFunctionName()), expression.getExpressions(), expression.getResolvedWindowDefinition());
        }

        treatedJoinNodesForConstraints = oldTreatedJoinNodesForConstraints;
    }

    private String resolveRenderedFunctionName(String literalFunctionName) {
        String registeredFunctionName = registeredFunctionsNames.get(literalFunctionName.toLowerCase());
        return registeredFunctionName == null ? literalFunctionName : registeredFunctionName;
    }

    @SuppressWarnings("unchecked")
    protected void renderCountStar(WindowDefinition windowDefinition) {
        if (jpaProvider.supportsCustomFunctions()) {
            if (jpaProvider.supportsCountStar() && windowDefinition == null) {
                sb.append("COUNT(*)");
            } else if (windowDefinition != null) {
                renderFunctionFunction(resolveRenderedFunctionName("WINDOW_COUNT"), (List<Expression>) (List<?>) Collections.emptyList(), windowDefinition);
            } else {
                renderFunctionFunction(resolveRenderedFunctionName("COUNT_STAR"), (List<Expression>) (List<?>) Collections.emptyList(), null);
            }
        } else {
            if (windowDefinition != null) {
                throw new IllegalArgumentException("JPA provider does not support custom function invocation!");
            }
            sb.append("COUNT(1)");
        }
    }

    @Override
    public void visit(SubqueryExpression expression) {
        if (expression.getSubquery() instanceof SubqueryInternalBuilder) {
            final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> subquery = (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery();
            subquery.prepareAndCheck();
            final boolean hasFirstResult = subquery.getFirstResult() != 0;
            final boolean hasMaxResults = subquery.getMaxResults() != Integer.MAX_VALUE;
            final boolean hasLimit = hasFirstResult || hasMaxResults;
            final boolean hasSetOperations = subquery instanceof BaseFinalSetOperationBuilder<?, ?>;
            final boolean hasEntityFunctions = subquery.joinManager.hasEntityFunctions();
            final boolean isSimple = !hasLimit && !hasSetOperations && !hasEntityFunctions;

            if (isSimple) {
                sb.append('(');
                subquery.buildBaseQueryString(sb, externalRepresentation, true, null);
                sb.append(')');
            } else {
                if (!externalRepresentation) {
                    sb.append('(');
                }
                Expression subqueryExpression = subquery.asExpression(externalRepresentation);
                if (subqueryExpression instanceof SubqueryExpression) {
                    sb.append(((SubqueryExpression) subqueryExpression).getSubquery().getQueryString());
                } else {
                    subqueryExpression.accept(this);
                }
                if (!externalRepresentation) {
                    sb.append(')');
                }
            }
        } else {
            sb.append('(');
            sb.append(expression.getSubquery().getQueryString());
            sb.append(')');
        }
    }

    @Override
    protected boolean isSimpleSubquery(SubqueryExpression expression) {
        if (expression.getSubquery() instanceof SubqueryInternalBuilder) {
            final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> subquery = (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery();
            final boolean hasFirstResult = subquery.getFirstResult() != 0;
            final boolean hasMaxResults = subquery.getMaxResults() != Integer.MAX_VALUE;
            final boolean hasLimit = hasFirstResult || hasMaxResults;
            final boolean hasSetOperations = subquery instanceof BaseFinalSetOperationBuilder<?, ?>;
            final boolean hasEntityFunctions = subquery.joinManager.hasEntityFunctions();
            return !hasLimit && !hasSetOperations && !hasEntityFunctions && !subquery.joinManager.hasLateInlineNodes();
        }
        return super.isSimpleSubquery(expression);
    }

    protected void renderFunctionFunction(String functionName, List<Expression> arguments, WindowDefinition windowDefinition) {
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        int size = arguments.size();
        if (registeredFunctions.containsKey(functionName)) {
            sb.append(jpaProvider.getCustomFunctionInvocation(functionName, windowDefinition == null ? size : size + 1));
            if (size == 0) {
                visitWindowDefinition(windowDefinition);
            } else {
                arguments.get(0).accept(this);
                for (int i = 1; i < size; i++) {
                    sb.append(",");
                    arguments.get(i).accept(this);
                }
                if (windowDefinition != null) {
                    sb.append(",");
                    visitWindowDefinition(windowDefinition);
                }
            }
            sb.append(')');
        } else if (jpaProvider.supportsJpa21()) {
            // Add the JPA 2.1 Function style function
            sb.append("FUNCTION('");
            sb.append(functionName);
            sb.append('\'');

            for (int i = 0; i < size; i++) {
                sb.append(',');
                arguments.get(i).accept(this);
            }
            if (windowDefinition != null) {
                sb.append(",");
                visitWindowDefinition(windowDefinition);
            }

            sb.append(')');
        } else {
            throw new IllegalArgumentException("Unknown function [" + functionName + "] is used!");
        }
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    protected void visitWindowDefinition(WindowDefinition windowDefinition) {
        if (windowDefinition != null) {
            Predicate filterPredicate = windowDefinition.getFilterPredicate();
            if (filterPredicate != null) {
                sb.append("'FILTER',");
                filterPredicate.accept(this);
            }

            List<Expression> partitionExpressions = windowDefinition.getPartitionExpressions();

            int size = partitionExpressions.size();
            if (size != 0) {
                if (filterPredicate != null) {
                    sb.append(",");
                }
                sb.append("'PARTITION BY',");
                partitionExpressions.get(0).accept(this);
                for (int i = 1; i < size; i++) {
                    sb.append(",");
                    partitionExpressions.get(i).accept(this);
                }
            }

            List<OrderByItem> orderByExpressions = windowDefinition.getOrderByExpressions();
            size = orderByExpressions.size();
            if (size != 0) {
                if (filterPredicate != null || partitionExpressions.size() != 0) {
                    sb.append(",");
                }
                sb.append("'ORDER BY',");
                visit(orderByExpressions.get(0));
                for (int i = 1; i < size; i++) {
                    sb.append(",");
                    visit(orderByExpressions.get(i));
                }
            }

            if (windowDefinition.getFrameMode() != null) {
                if (filterPredicate != null || partitionExpressions.size() != 0 || orderByExpressions.size() != 0) {
                    sb.append(",");
                }
                sb.append('\'');
                sb.append(windowDefinition.getFrameMode().name());
                sb.append("'");
                if (windowDefinition.getFrameEndType() != null) {
                    sb.append(",'BETWEEN'");
                }

                if (windowDefinition.getFrameStartExpression() != null) {
                    sb.append(",");
                    windowDefinition.getFrameStartExpression().accept(this);
                }

                sb.append(",'");
                sb.append(getFrameType(windowDefinition.getFrameStartType()));
                sb.append("'");

                if (windowDefinition.getFrameEndType() != null) {
                    sb.append(",'AND'");
                    if (windowDefinition.getFrameEndExpression() != null) {
                        sb.append(",");
                        windowDefinition.getFrameEndExpression().accept(this);
                    }

                    sb.append(",'");
                    sb.append(getFrameType(windowDefinition.getFrameEndType()));
                    sb.append("'");
                }

                if (windowDefinition.getFrameExclusionType() != null) {
                    sb.append(",'");
                    sb.append(getFrameExclusionType(windowDefinition.getFrameExclusionType()));
                    sb.append("'");
                }
            }
        }
    }

    private void visit(OrderByItem orderByItem) {
        orderByItem.getExpression().accept(this);
        sb.append(",'");
        sb.append(orderByItem.isAscending() ? "ASC" : "DESC");
        sb.append(orderByItem.isNullFirst() ? " NULLS FIRST" : " NULLS LAST");
        sb.append("'");
    }

    private boolean isCountStarFunction(FunctionExpression expression) {
        return expression instanceof AggregateExpression && expression.getExpressions().isEmpty()
            && "COUNT".equalsIgnoreCase(expression.getFunctionName());
    }

    @Override
    public void visit(TreatExpression expression) {
        if (jpaProvider.supportsRootTreat()) {
            super.visit(expression);
        } else if (jpaProvider.supportsSubtypePropertyResolving()) {
            // NOTE: this might be wrong when having multiple same named properties
            expression.getExpression().accept(this);
        } else {
            throw new IllegalArgumentException("Can not render treat expression[" + expression.toString() + "] as the JPA provider does not support it!");
        }
    }

    @Override
    public void visit(PathExpression expression) {
        if (resolveSelectAliases && expression.getExpressions().size() == 1) {
            AliasInfo aliasInfo;
            String potentialAlias = expression.getExpressions().get(0).toString();
            try {
                if (currentlyResolvingAliases.add(potentialAlias) && (aliasInfo = aliasManager.getAliasInfo(potentialAlias)) != null) {
                    if (aliasInfo instanceof SelectInfo) {
                        SelectInfo selectAliasInfo = (SelectInfo) aliasInfo;
                        if (selectAliasInfo.getExpression() instanceof PathExpression) {
                            PathExpression aliasedExpression = (PathExpression) selectAliasInfo.getExpression();
                            boolean collectionKeyPath = aliasedExpression.isCollectionQualifiedPath();
                            boolean usedInCollectionFunction = aliasedExpression.isUsedInCollectionFunction();
                            aliasedExpression.setCollectionQualifiedPath(expression.isCollectionQualifiedPath());
                            aliasedExpression.setUsedInCollectionFunction(expression.isUsedInCollectionFunction());
                            try {
                                selectAliasInfo.getExpression().accept(this);
                            } finally {
                                aliasedExpression.setCollectionQualifiedPath(collectionKeyPath);
                                aliasedExpression.setUsedInCollectionFunction(usedInCollectionFunction);
                            }
                        } else {
                            selectAliasInfo.getExpression().accept(this);
                        }
                        return;
                    }
                }
            } finally {
                currentlyResolvingAliases.remove(potentialAlias);
            }
        }
        JoinNode baseNode;
        String field;
        if ((baseNode = (JoinNode) expression.getBaseNode()) == null) {
            super.visit(expression);
        } else {
            String collectionValueFunction = jpaProvider.getCollectionValueFunction();
            if ((field = expression.getField()) == null) {
                if (expression.isUsedInCollectionFunction() || renderAbsolutePath(expression)) {
                    super.visit(expression);
                } else {
                    // NOTE: Hibernate uses the column from a join table if VALUE is used which is wrong, so drop the VALUE here
                    boolean valueFunction = collectionValueFunction != null && needsValueFunction(expression, baseNode, field);

                    if (valueFunction) {
                        sb.append(collectionValueFunction);
                        sb.append('(');
                    }

                    if (aliasPrefix != null) {
                        sb.append(aliasPrefix);
                    }

                    baseNode.appendAlias(sb, externalRepresentation);

                    if (valueFunction) {
                        sb.append(')');
                    }
                }
            } else {
                List<JoinNode> treatedJoinNodes = baseNode.getJoinNodesForTreatConstraint();
                if (treatedJoinNodesForConstraints != null) {
                    for (JoinNode node : treatedJoinNodes) {
                        treatedJoinNodesForConstraints.put(node, Boolean.FALSE);
                    }
                }

                ManagedType<?> baseNodeType = baseNode.getManagedType();
                boolean addTypeCaseWhen = !treatedJoinNodes.isEmpty()
                        && baseNodeType instanceof EntityType<?>
                        && jpaProvider.needsTypeConstraintForColumnSharing()
                        && jpaProvider.isColumnShared((EntityType<?>) baseNodeType, field);
                if (addTypeCaseWhen) {
                    sb.append("CASE WHEN ");
                    boolean first = true;

                    for (int i = 0; i < treatedJoinNodes.size(); i++) {
                        JoinNode treatedJoinNode = treatedJoinNodes.get(i);

                        // When the JPA provider supports rendering treat joins and we have a treat join node
                        // we skip the type constraint as that is already applied through the join
                        if (jpaProvider.supportsTreatJoin() && treatedJoinNode.isTreatJoinNode()) {
                            continue;
                        }

                        if (first) {
                            first = false;
                        } else {
                            sb.append(" AND ");
                        }

                        sb.append("TYPE(");
                        sb.append(treatedJoinNode.getAlias());
                        sb.append(") IN (");
                        for (EntityType<?> entitySubtype : entityMetamodel.getEntitySubtypes(treatedJoinNode.getTreatType())) {
                            sb.append(entitySubtype.getName());
                            sb.append(", ");
                        }

                        sb.setLength(sb.length() - 2);
                        sb.append(')');
                    }

                    sb.append(" THEN ");
                }

                boolean valueFunction = collectionValueFunction != null && needsValueFunction(expression, baseNode, field);
                // NOTE: There is no need to check for whether the JPA provider support implicit downcasting here
                // If it didn't, the query building would have already failed before. Here we just decide whether to render the treat or not
                boolean renderTreat = jpaProvider.supportsRootTreat();

                if (valueFunction) {
                    sb.append(collectionValueFunction);
                    sb.append('(');

                    if (aliasPrefix != null) {
                        sb.append(aliasPrefix);
                    }

                    baseNode.appendAlias(sb, renderTreat, externalRepresentation);
                    sb.append(')');
                    sb.append(".").append(field);
                } else {
                    if (aliasPrefix != null) {
                        sb.append(aliasPrefix);
                    }

                    baseNode.appendDeReference(sb, field, renderTreat, externalRepresentation, jpaProvider.needsElementCollectionIdCutoff());
                }

                if (addTypeCaseWhen) {
                    sb.append(" END");
                }
            }
        }
    }

    private boolean needsValueFunction(PathExpression expression, JoinNode baseNode, String field) {
        return !expression.isCollectionQualifiedPath() && baseNode.getParentTreeNode() != null && baseNode.getParentTreeNode().isMap() && (field == null || jpaProvider.supportsCollectionValueDereference());
    }

    private boolean renderAbsolutePath(PathExpression expression) {
        JoinNode baseNode = (JoinNode) expression.getBaseNode();
        return renderedJoinNodes != null && !renderedJoinNodes.contains(baseNode);
    }

    @Override
    protected boolean needsParenthesisForCaseResult(Expression expression) {
        // Hibernate parser complains about arithmetic expression in the then clause, only works with parenthesis
        // Since other JPA providers don't have a problem with parenthesis, we don't introduce another property in the JpaProvider interface for this
        return expression instanceof ArithmeticExpression;
    }

    @Override
    protected String getBooleanConditionalExpression(boolean value) {
        return jpaProvider.getBooleanConditionalExpression(value);
    }

    @Override
    protected String getBooleanExpression(boolean value) {
        return jpaProvider.getBooleanExpression(value);
    }

    @Override
    protected String escapeCharacter(char character) {
        return jpaProvider.escapeCharacter(character);
    }

    @Override
    public void visit(ParameterExpression expression) {
        // Workaround for hibernate
        // TODO: Remove when HHH-7407 is fixed
        boolean needsBrackets = jpaProvider.needsBracketsForListParameter() && expression.isCollectionValued();

        if (needsBrackets) {
            sb.append('(');
        }

        super.visit(expression);

        if (needsBrackets) {
            sb.append(')');
        }
    }

    @Override
    protected String getLiteralParameterValue(ParameterExpression expression) {
        Object value = expression.getValue();
        if (value == null) {
            value = parameterManager.getParameterValue(expression.getName());
        }

        if (value != null) {
            final TypeConverter<Object> converter = (TypeConverter<Object>) TypeUtils.getConverter(value.getClass());
            // We can't render enum values as literals directly, only in the context of a predicate, so we need the BooleanLiteralRenderingContext.PLAIN
            if (converter != null && (!(value instanceof Enum<?>) || getBooleanLiteralRenderingContext() == BooleanLiteralRenderingContext.PLAIN)) {
                return converter.toString(value);
            }
        }

        return null;
    }

    public void setResolveSelectAliases(boolean replaceSelectAliases) {
        this.resolveSelectAliases = replaceSelectAliases;
    }

    public boolean isResolveSelectAliases() {
        return resolveSelectAliases;
    }

    public void setAliasPrefix(String aliasPrefix) {
        this.aliasPrefix = aliasPrefix;
    }

    public void addAlias(String alias) {
        currentlyResolvingAliases.add(alias);
    }

    public void removeAlias(String alias) {
        currentlyResolvingAliases.remove(alias);
    }

    public void setRenderedJoinNodes(Set<JoinNode> renderedJoinNodes) {
        this.renderedJoinNodes = renderedJoinNodes;
    }

    public void setClauseType(ClauseType clauseType) {
        this.clauseType = clauseType;
    }

    public boolean isExternalRepresentation() {
        return externalRepresentation;
    }

    public void setExternalRepresentation(boolean externalRepresentation) {
        this.externalRepresentation = externalRepresentation;
    }

    @Override
    public void visit(ArrayExpression expression) {
    }

    @Override
    public void visit(InPredicate predicate) {
        if (predicate.getRight().size() == 1 && jpaProvider.needsAssociationToIdRewriteInOnClause() && clauseType == ClauseType.JOIN) {
            Expression right = predicate.getRight().get(0);
            if (right instanceof ParameterExpression) {
                ParameterExpression parameterExpression = (ParameterExpression) right;
                @SuppressWarnings("unchecked")
                Type<?> associationType = getAssociationType(predicate.getLeft(), right);
                // If the association type is a entity type, we transform it
                if (associationType instanceof EntityType<?>) {
                    renderEquality(predicate.getLeft(), right, predicate.isNegated(), PredicateQuantifier.ONE);
                } else {
                    super.visit(predicate);
                }
            } else if (right instanceof PathExpression) {
                renderEquality(predicate.getLeft(), right, predicate.isNegated(), PredicateQuantifier.ONE);
            } else {
                super.visit(predicate);
            }
        } else {
            super.visit(predicate);
        }
    }

    private Type<?> getAssociationType(Expression expression1, Expression expression2) {
        if (expression1 instanceof PathExpression) {
            return ((PathExpression) expression1).getPathReference().getType();
        }

        return ((PathExpression) expression2).getPathReference().getType();
    }

    @Override
    public void visit(final EqPredicate predicate) {
        renderEquality(predicate.getLeft(), predicate.getRight(), predicate.isNegated(), predicate.getQuantifier());
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    private void renderEquality(Expression left, Expression right, boolean negated, PredicateQuantifier quantifier) {
        final String operator;
        if (negated) {
            operator = " <> ";
        } else {
            operator = " = ";
        }

        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        // TODO: Currently we assume that types can be inferred, and render parameters through but e.g. ":param1 = :param2" will fail
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);

        if (jpaProvider.needsAssociationToIdRewriteInOnClause() && clauseType == ClauseType.JOIN) {
            boolean rewritten = renderAssociationIdIfPossible(left);
            sb.append(operator);
            if (quantifier != PredicateQuantifier.ONE) {
                sb.append(quantifier.toString());
            }
            rewritten |= renderAssociationIdIfPossible(right);
            if (rewritten) {
                rewriteToIdParam(left);
                rewriteToIdParam(right);
            }
        } else {
            left.accept(this);
            sb.append(operator);
            if (quantifier != PredicateQuantifier.ONE) {
                sb.append(quantifier.toString());
            }
            right.accept(this);
        }
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    private boolean renderAssociationIdIfPossible(Expression expression) {
        expression.accept(this);

        if (expression instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expression;

            // Before Hibernate 5.1 there was a "broken" possibility to use multiple join nodes in the WITH clause
            // That involves only suffixing association paths so that predicates look like "p = other.relation.id"
            if (!jpaProvider.needsBrokenAssociationToIdRewriteInOnClause() || pathExpression.getBaseNode() != null && pathExpression.getField() != null) {
                Type<?> pathType = pathExpression.getPathReference().getType();
                if (pathType instanceof ManagedType<?> && JpaMetamodelUtils.isIdentifiable((ManagedType<?>) pathType)) {
                    String idName = JpaMetamodelUtils.getSingleIdAttribute((IdentifiableType<?>) pathType).getName();
                    sb.append('.');
                    sb.append(idName);
                    return true;
                }
            }
        }

        return false;
    }

    private void rewriteToIdParam(Expression expression) {
        if (!(expression instanceof ParameterExpression)) {
            return;
        }
        ParameterExpression parameterExpression = (ParameterExpression) expression;
        ParameterManager.ParameterImpl<Object> param = (ParameterManager.ParameterImpl<Object>) parameterManager.getParameter(parameterExpression.getName());
        param.setTranformer(parameterTransformerFactory.getToIdTransformer());
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        // Null check does not require a type to be known
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT NULL");
            flipTreatedJoinNodeConstraints();
        } else {
            sb.append(" IS NULL");
        }
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(LikePredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(ExistsPredicate predicate) {
        SubqueryExpression subqueryExpression;
        if (predicate.getExpression() instanceof SubqueryExpression && !isSimpleSubquery(subqueryExpression = (SubqueryExpression) predicate.getExpression())) {
            if (externalRepresentation) {
                if (predicate.isNegated()) {
                    sb.append("NOT EXISTS ");
                } else {
                    sb.append("EXISTS ");
                }
                sb.append('(');
                predicate.getExpression().accept(this);
                sb.append(')');
            } else {
                sb.append("1 = ");
                sb.append(jpaProvider.getCustomFunctionInvocation(ExistFunction.FUNCTION_NAME, 1));
                subqueryExpression.accept(this);
                if (predicate.isNegated()) {
                    sb.append(",1");
                }
                sb.append(")");
            }
        } else {
            super.visit(predicate);
        }
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(GtPredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(GePredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(LtPredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    @Override
    public void visit(LePredicate predicate) {
        super.visit(predicate);
        if (predicate.isNegated()) {
            flipTreatedJoinNodeConstraints();
        }
    }

    protected void visitWhenClauseCondition(Expression condition) {
        if (!(condition instanceof Predicate) || condition instanceof CompoundPredicate) {
            condition.accept(this);
            return;
        }

        Predicate p = (Predicate) condition;

        int startPosition = sb.length();
        Map<JoinNode, Boolean> oldTreatedJoinNodesForConstraints = treatedJoinNodesForConstraints;
        treatedJoinNodesForConstraints = new LinkedHashMap<>();

        p.accept(this);

        insertTreatJoinConstraint(startPosition, sb.length());
        treatedJoinNodesForConstraints = oldTreatedJoinNodesForConstraints;
    }

    @Override
    public void visit(CompoundPredicate predicate) {
        BooleanLiteralRenderingContext oldConditionalContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PREDICATE);
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        boolean parenthesisRequired = predicate.getChildren().size() > 1;
        if (predicate.isNegated()) {
            sb.append("NOT ");
            if (parenthesisRequired) {
                sb.append('(');
            }
        }

        if (predicate.getChildren().size() == 1) {
            int startPosition = sb.length();
            Map<JoinNode, Boolean> oldTreatedJoinNodesForConstraints = treatedJoinNodesForConstraints;
            treatedJoinNodesForConstraints = new LinkedHashMap<>();

            predicate.getChildren().get(0).accept(this);

            insertTreatJoinConstraint(startPosition, sb.length());
            treatedJoinNodesForConstraints = oldTreatedJoinNodesForConstraints;
            return;
        }
        final int startLen = sb.length();
        final String operator = " " + predicate.getOperator().toString() + " ";
        final List<Predicate> children = predicate.getChildren();
        int size = children.size();
        Map<JoinNode, Boolean> oldTreatedJoinNodesForConstraints = treatedJoinNodesForConstraints;
        treatedJoinNodesForConstraints = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            int startPosition = sb.length();
            int endPosition;

            Predicate child = children.get(i);
            if (child instanceof CompoundPredicate && ((CompoundPredicate) child).getOperator() != predicate.getOperator() && !child.isNegated()) {
                sb.append("(");
                int len = sb.length();
                child.accept(this);
                // If the child was empty, we remove the opening parenthesis again
                if (len == sb.length()) {
                    sb.deleteCharAt(len - 1);
                    endPosition = sb.length();
                } else {
                    sb.append(")");
                    endPosition = sb.length();
                    sb.append(operator);
                }
            } else {
                child.accept(this);
                endPosition = sb.length();
                sb.append(operator);
            }

            insertTreatJoinConstraint(startPosition, endPosition);
            treatedJoinNodesForConstraints.clear();
        }

        treatedJoinNodesForConstraints = oldTreatedJoinNodesForConstraints;

        // Delete the last operator only if the children actually generated something
        if (startLen < sb.length()) {
            sb.delete(sb.length() - operator.length(), sb.length());
        }
        if (predicate.isNegated() && parenthesisRequired) {
            sb.append(')');
        }
        setBooleanLiteralRenderingContext(oldConditionalContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    private void flipTreatedJoinNodeConstraints() {
        if (treatedJoinNodesForConstraints != null) {
            for (Map.Entry<JoinNode, Boolean> entry : treatedJoinNodesForConstraints.entrySet()) {
                if (entry.getValue() == Boolean.TRUE) {
                    entry.setValue(Boolean.FALSE);
                } else {
                    entry.setValue(Boolean.TRUE);
                }
            }
        }
    }

    private boolean insertTreatJoinConstraint(int startPosition, int endPosition) {
        if (!treatedJoinNodesForConstraints.isEmpty()) {
            StringBuilder treatConditionBuilder = new StringBuilder(treatedJoinNodesForConstraints.size() * 40);
            treatConditionBuilder.append('(');
            for (Map.Entry<JoinNode, Boolean> entry : treatedJoinNodesForConstraints.entrySet()) {
                JoinNode node = entry.getKey();
                // When the JPA provider supports rendering treat joins and we have a treat join node
                // we skip the type constraint as that is already applied through the join
                if (jpaProvider.supportsTreatJoin() && node.isTreatJoinNode()) {
                    continue;
                }

                treatConditionBuilder.append("TYPE(");
                treatConditionBuilder.append(node.getAlias());
                if (entry.getValue() == Boolean.TRUE) {
                    treatConditionBuilder.append(") <> ");
                    treatConditionBuilder.append(node.getTreatType().getName());
                    treatConditionBuilder.append(" OR ");
                } else {
                    treatConditionBuilder.append(") = ");
                    treatConditionBuilder.append(node.getTreatType().getName());
                    treatConditionBuilder.append(" AND ");
                }
            }

            // Because we always have the open parenthesis as first char
            if (treatConditionBuilder.length() > 1) {
                sb.insert(endPosition, ')');
                sb.insert(startPosition, treatConditionBuilder);
                return true;
            }
        }

        return false;
    }
}
