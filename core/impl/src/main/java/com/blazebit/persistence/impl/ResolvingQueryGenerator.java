/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.parser.expression.DateLiteral;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.OrderByItem;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PathReference;
import com.blazebit.persistence.parser.expression.QualifiedExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TimeLiteral;
import com.blazebit.persistence.parser.expression.TimestampLiteral;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.WindowDefinition;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.GePredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.parser.util.LiteralFunctionTypeConverter;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Type;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    private boolean quantifiedPredicate;
    private Set<JoinNode> renderedJoinNodes;
    private ClauseType clauseType;
    private final EntityMetamodel entityMetamodel;
    private final Set<String> currentlyResolvingAliases;
    private final AliasManager aliasManager;
    private final ParameterManager parameterManager;
    private final AssociationParameterTransformerFactory parameterTransformerFactory;
    private final JpaProvider jpaProvider;
    private final DbmsDialect dbmsDialect;
    private final Map<String, JpqlFunction> registeredFunctions;
    private final Map<String, String> registeredFunctionNames;

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

    public ResolvingQueryGenerator(EntityMetamodel entityMetamodel, AliasManager aliasManager, ParameterManager parameterManager, AssociationParameterTransformerFactory parameterTransformerFactory, JpaProvider jpaProvider, DbmsDialect dbmsDialect, Map<String, JpqlFunction> registeredFunctions, Map<String, String> registeredFunctionNames) {
        this.entityMetamodel = entityMetamodel;
        this.aliasManager = aliasManager;
        this.parameterManager = parameterManager;
        this.parameterTransformerFactory = parameterTransformerFactory;
        this.jpaProvider = jpaProvider;
        this.dbmsDialect = dbmsDialect;
        this.registeredFunctions = registeredFunctions;
        this.currentlyResolvingAliases = new HashSet<>();
        this.registeredFunctionNames = registeredFunctionNames;
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
    public void visit(ListIndexExpression expression) {
        PathExpression path = expression.getPath();
        String deReferenceFunction = null;
        if (!externalRepresentation && path.getPathReference() != null && (deReferenceFunction = ((JoinNode) path.getPathReference().getBaseNode()).getDeReferenceFunction()) != null)  {
            sb.append(deReferenceFunction);
        }
        sb.append("INDEX(");
        path.accept(this);
        sb.append(')');
        if (deReferenceFunction != null) {
            sb.append(')');
        }
    }

    @Override
    public void visit(MapKeyExpression expression) {
        PathExpression path = expression.getPath();
        String deReferenceFunction = null;
        if (!externalRepresentation && path.getPathReference() != null && (deReferenceFunction = ((JoinNode) path.getPathReference().getBaseNode()).getDeReferenceFunction()) != null)  {
            sb.append(deReferenceFunction);
        }
        sb.append("KEY(");
        path.accept(this);
        sb.append(')');
        if (deReferenceFunction != null) {
            sb.append(')');
        }
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
    }

    private String resolveRenderedFunctionName(String literalFunctionName) {
        String registeredFunctionName = registeredFunctionNames.get(literalFunctionName.toLowerCase());
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
        if (!externalRepresentation && expression.getSubquery() instanceof SubqueryInternalBuilder) {
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
                subquery.buildBaseQueryString(sb, externalRepresentation, null, false);
                sb.append(')');
            } else {
                if (!externalRepresentation) {
                    sb.append('(');
                }
                Expression subqueryExpression = subquery.asExpression(externalRepresentation, quantifiedPredicate);
                if (!externalRepresentation && subqueryExpression instanceof SubqueryExpression) {
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
        if (!externalRepresentation && expression.getSubquery() instanceof SubqueryInternalBuilder) {
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
            sb.append(jpaProvider.getCustomFunctionInvocation(functionName, windowDefinition == null || windowDefinition.isEmpty() ? size : size + 1));
            if (size == 0) {
                visitWindowDefinition(windowDefinition);
            } else {
                arguments.get(0).accept(this);
                for (int i = 1; i < size; i++) {
                    sb.append(",");
                    arguments.get(i).accept(this);
                }
                if (windowDefinition != null && !windowDefinition.isEmpty()) {
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
            if (windowDefinition != null && !windowDefinition.isEmpty()) {
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
                sb.append("'FILTER',CASE WHEN ");
                filterPredicate.accept(this);
                sb.append(" THEN 1 ELSE 0 END");
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
                Type<?> baseNodeType = baseNode.getBaseType();
                boolean addTypeCaseWhen = false;
                if (!treatedJoinNodes.isEmpty() && baseNodeType instanceof EntityType<?> && baseNode.getTreatType() != null && (jpaProvider.supportsSubtypePropertyResolving() || !jpaProvider.supportsRootTreat())) {
                    ExtendedManagedType<?> extendedManagedType = entityMetamodel.getManagedType(ExtendedManagedType.class, baseNode.getTreatType().getName());
                    ExtendedAttribute<?, ?> extendedAttribute = extendedManagedType.getAttributes().get(field);
                    if (extendedAttribute.isColumnShared()) {
                        // To disambiguate shared column access, we also must use the type constraint
                        addTypeCaseWhen = jpaProvider.needsTypeConstraintForColumnSharing();
                    } else {
                        // If the attribute is declared by an entity sub-type of the treat target type, we don't need the case when
                        ExtendedAttribute<?, ?> baseTypeAttribute = (ExtendedAttribute<?, ?>) entityMetamodel.getManagedType(ExtendedManagedType.class, ((EntityType<?>) baseNode.getBaseType()).getName()).getAttributes().get(field);
                        addTypeCaseWhen = baseTypeAttribute != null && !baseNode.getTreatType().getJavaType().isAssignableFrom(baseTypeAttribute.getAttributePath().get(0).getDeclaringType().getJavaType());
                    }
                }
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
                        treatedJoinNode.appendAlias(sb, externalRepresentation);
                        sb.append(") IN (");
                        for (EntityType<?> entitySubtype : entityMetamodel.getEntitySubtypes(treatedJoinNode.getTreatType())) {
                            sb.append(entitySubtype.getName());
                            sb.append(", ");
                        }

                        sb.setLength(sb.length() - 2);
                        sb.append(')');
                    }

                    if (first) {
                        sb.setLength(sb.length() - "CASE WHEN ".length());
                        addTypeCaseWhen = false;
                    } else {
                        sb.append(" THEN ");
                    }
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
                    if (jpaProvider.needsCaseWhenElseBranch()) {
                        sb.append(" ELSE NULL");
                    }
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

        ParameterManager.ParameterImpl<?> parameter = parameterManager.getParameter(expression.getName());
        if (parameter.isUsedInGroupBy()) {
            ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.LITERAL);
            super.visit(expression);
            setParameterRenderingMode(oldParameterRenderingMode);
        } else {
            super.visit(expression);
        }

        if (needsBrackets) {
            sb.append(')');
        }
    }
    @Override
    public void visit(DateLiteral expression) {
        if (jpaProvider.supportsTemporalLiteral()) {
            super.visit(expression);
        } else {
            Date value = expression.getValue();
            if (value instanceof java.sql.Date) {
                appendTemporalLiteralEmulation((LiteralFunctionTypeConverter<? super java.sql.Date>) TypeUtils.DATE_CONVERTER, (java.sql.Date) value);
            } else {
                appendTemporalLiteralEmulation((LiteralFunctionTypeConverter<? super Date>) TypeUtils.DATE_AS_DATE_CONVERTER, value);
            }
        }
    }

    @Override
    public void visit(TimeLiteral expression) {
        if (jpaProvider.supportsTemporalLiteral()) {
            super.visit(expression);
        } else {
            Date value = expression.getValue();
            if (value instanceof java.sql.Time) {
                appendTemporalLiteralEmulation((LiteralFunctionTypeConverter<? super Time>) TypeUtils.TIME_CONVERTER, (java.sql.Time) value);
            } else {
                appendTemporalLiteralEmulation((LiteralFunctionTypeConverter<? super Date>) TypeUtils.DATE_AS_TIME_CONVERTER, value);
            }
        }
    }

    @Override
    public void visit(TimestampLiteral expression) {
        if (jpaProvider.supportsTemporalLiteral()) {
            super.visit(expression);
        } else {
            Date value = expression.getValue();
            if (value instanceof java.sql.Timestamp) {
                appendTemporalLiteralEmulation((LiteralFunctionTypeConverter<? super Timestamp>) TypeUtils.TIMESTAMP_CONVERTER, (java.sql.Timestamp) value);
            } else {
                appendTemporalLiteralEmulation((LiteralFunctionTypeConverter<? super Date>) TypeUtils.DATE_TIMESTAMP_CONVERTER, value);
            }
        }
    }

    private <T> void appendTemporalLiteralEmulation(LiteralFunctionTypeConverter<? super T> converter, T value) {
        String functionInvocation = jpaProvider.getCustomFunctionInvocation(converter.getLiteralFunctionName(), 1);
        String literalValue = converter.toString(value);
        sb.append(functionInvocation);
        TypeUtils.STRING_CONVERTER.appendTo(literalValue, sb);
        sb.append(')');
    }

    @Override
    protected Set<String> getSupportedEnumTypes() {
        return entityMetamodel.getEnumTypes().keySet();
    }

    @Override
    protected String getLiteralParameterValue(ParameterExpression expression) {
        // We can't render enum values as literals directly, only in the context of a predicate, so we need the BooleanLiteralRenderingContext.PLAIN
        boolean renderEnumAsLiteral = getBooleanLiteralRenderingContext() == BooleanLiteralRenderingContext.PLAIN;
        return parameterManager.getLiteralParameterValue(expression, renderEnumAsLiteral);
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

    public ClauseType getClauseType() {
        return clauseType;
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
        boolean quantifiedPredicate = this.quantifiedPredicate;
        this.quantifiedPredicate = true;
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
        this.quantifiedPredicate = quantifiedPredicate;
    }

    private Type<?> getAssociationType(Expression expression1, Expression expression2) {
        if (expression1 instanceof PathExpression) {
            return ((PathExpression) expression1).getPathReference().getType();
        }

        return ((PathExpression) expression2).getPathReference().getType();
    }

    @Override
    public void visit(final EqPredicate predicate) {
        boolean quantifiedPredicate = this.quantifiedPredicate;
        this.quantifiedPredicate = predicate.getQuantifier() != PredicateQuantifier.ONE;
        renderEquality(predicate.getLeft(), predicate.getRight(), predicate.isNegated(), predicate.getQuantifier());
        this.quantifiedPredicate = quantifiedPredicate;
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

        Expression expressionToSplit = needsEmbeddableSplitting(left, right);

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
            if (expressionToSplit == null || dbmsDialect.supportsAnsiRowValueConstructor() || !(left instanceof ParameterExpression) && !(right instanceof ParameterExpression)) {
                left.accept(this);
                sb.append(operator);
                if (quantifier != PredicateQuantifier.ONE) {
                    sb.append(quantifier.toString());
                }
                right.accept(this);
            } else {
                // We split the path and the parameter expression accordingly
                // TODO: Try to handle map key expressions, although no JPA provider supports de-referencing map keys
                PathExpression pathExpression = (PathExpression) expressionToSplit;
                ParameterExpression parameterExpression;
                if (left instanceof ParameterExpression) {
                    parameterExpression = (ParameterExpression) left;
                } else {
                    parameterExpression = (ParameterExpression) right;
                }
                PathReference pathReference = pathExpression.getPathReference();
                EmbeddableType<?> embeddableType = (EmbeddableType<?>) pathReference.getType();
                String parameterName = parameterExpression.getName();
                Map<String, List<String>> parameterAccessPaths = new HashMap<>();
                ParameterManager.ParameterImpl<?> parameter = parameterManager.getParameter(parameterName);
                sb.append('(');
                for (Attribute<?, ?> attribute : embeddableType.getAttributes()) {
                    ((JoinNode) pathReference.getBaseNode()).appendDeReference(sb, pathReference.getField() + "." + attribute.getName(), externalRepresentation);
                    String embeddedPropertyName = attribute.getName();
                    String subParamName = "_" + parameterName + "_" + embeddedPropertyName.replace('.', '_');
                    sb.append(operator);
                    sb.append(":").append(subParamName);
                    if (parameter.getTransformer() == null) {
                        parameterManager.registerParameterName(subParamName, false, null, null);
                    }
                    parameterAccessPaths.put(subParamName, Arrays.asList(embeddedPropertyName.split("\\.")));

                    sb.append(" AND ");
                }
                sb.setLength(sb.length() - " AND ".length());
                sb.append(')');

                if (parameter.getTransformer() == null) {
                    parameter.setTransformer(new SplittingParameterTransformer(parameterManager, entityMetamodel, embeddableType.getJavaType(), parameterAccessPaths));
                }
            }
        }
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    private Expression needsEmbeddableSplitting(Expression left, Expression right) {
        Expression l = needsEmbeddableSplitting(left);
        if (l != null) {
            return l;
        }
        return needsEmbeddableSplitting(right);
    }

    private Expression needsEmbeddableSplitting(Expression expr) {
        if (expr instanceof MapKeyExpression) {
            PathReference pathReference = ((QualifiedExpression) expr).getPath().getPathReference();
            if (pathReference != null) {
                JoinNode joinNode = (JoinNode) pathReference.getBaseNode();
                if (joinNode.getDeReferenceFunction() != null && ((MapAttribute<?, ?, ?>) joinNode.getParentTreeNode().getAttribute()).getKeyType() instanceof EmbeddableType<?>) {
                    return expr;
                }
            }
        } else if (expr instanceof PathExpression) {
            PathReference pathReference = ((PathExpression) expr).getPathReference();
            if (pathReference != null) {
                JoinNode joinNode = (JoinNode) pathReference.getBaseNode();
                if (joinNode.getDeReferenceFunction() != null && pathReference.getType() instanceof EmbeddableType<?>) {
                    // We need to split this embeddable
                    return expr;
                }
            }
        }
        return null;
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
        param.setTransformer(parameterTransformerFactory.getToIdTransformer());
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        // Null check does not require a type to be known
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT NULL");
        } else {
            sb.append(" IS NULL");
        }
        setParameterRenderingMode(oldParameterRenderingMode);
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
    }

    @Override
    public void visit(GtPredicate predicate) {
        boolean quantifiedPredicate = this.quantifiedPredicate;
        this.quantifiedPredicate = predicate.getQuantifier() != PredicateQuantifier.ONE;
        super.visit(predicate);
        this.quantifiedPredicate = quantifiedPredicate;
    }

    @Override
    public void visit(GePredicate predicate) {
        boolean quantifiedPredicate = this.quantifiedPredicate;
        this.quantifiedPredicate = predicate.getQuantifier() != PredicateQuantifier.ONE;
        super.visit(predicate);
        this.quantifiedPredicate = quantifiedPredicate;
    }

    @Override
    public void visit(LtPredicate predicate) {
        boolean quantifiedPredicate = this.quantifiedPredicate;
        this.quantifiedPredicate = predicate.getQuantifier() != PredicateQuantifier.ONE;
        super.visit(predicate);
        this.quantifiedPredicate = quantifiedPredicate;
    }

    @Override
    public void visit(LePredicate predicate) {
        boolean quantifiedPredicate = this.quantifiedPredicate;
        this.quantifiedPredicate = predicate.getQuantifier() != PredicateQuantifier.ONE;
        super.visit(predicate);
        this.quantifiedPredicate = quantifiedPredicate;
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
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final int startLen = sb.length();
        final String operator = " " + predicate.getOperator().toString() + " ";
        final List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Predicate child = children.get(i);
            if (child instanceof CompoundPredicate && ((CompoundPredicate) child).getOperator() != predicate.getOperator() && !child.isNegated()) {
                sb.append("(");
                int len = sb.length();
                child.accept(this);
                // If the child was empty, we remove the opening parenthesis again
                if (len == sb.length()) {
                    sb.deleteCharAt(len - 1);
                } else {
                    sb.append(")");
                    sb.append(operator);
                }
            } else {
                child.accept(this);
                sb.append(operator);
            }
        }

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
}
