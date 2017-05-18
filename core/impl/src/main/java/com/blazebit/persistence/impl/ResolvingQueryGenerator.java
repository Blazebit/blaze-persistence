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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.BaseFinalSetOperationBuilder;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.AggregateExpression;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.NumericType;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.Subquery;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;
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
import com.blazebit.persistence.spi.OrderByElement;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    protected String aliasPrefix;
    private boolean resolveSelectAliases = true;
    private boolean externalRepresentation;
    private Set<JoinNode> renderedJoinNodes;
    private ClauseType clauseType;
    private Map<JoinNode, Boolean> treatedJoinNodesForConstraints;
    private final AliasManager aliasManager;
    private final ParameterManager parameterManager;
    private final AssociationParameterTransformerFactory parameterTransformerFactory;
    private final JpaProvider jpaProvider;
    private final Map<String, JpqlFunction> registeredFunctions;
    private final Map<String, String> registeredFunctionsNames;

    public ResolvingQueryGenerator(AliasManager aliasManager, ParameterManager parameterManager, AssociationParameterTransformerFactory parameterTransformerFactory, JpaProvider jpaProvider, Map<String, JpqlFunction> registeredFunctions) {
        this.aliasManager = aliasManager;
        this.parameterManager = parameterManager;
        this.parameterTransformerFactory = parameterTransformerFactory;
        this.jpaProvider = jpaProvider;
        this.registeredFunctions = registeredFunctions;
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
                sb.append(jpaProvider.getNullExpression());
                return;
            }
        }
        expression.accept(this);
    }

    @Override
    public void visit(MapValueExpression expression) {
        // NOTE: Hibernate uses the column from a join table if VALUE is used which is wrong, so drop the VALUE here
        String valueFunction = jpaProvider.getCollectionValueFunction();
        if (valueFunction != null) {
            sb.append(valueFunction);
            sb.append('(');
            expression.getPath().accept(this);
            sb.append(')');
        } else {
            expression.getPath().accept(this);
        }
    }

    @Override
    public void visit(FunctionExpression expression) {
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
            renderFunctionFunction(resolvedFunctionName, argumentsWithoutFunctionName);
        } else if (isCountStarFunction(expression)) {
            renderCountStar();
        } else {
            super.visit(expression);
        }

        treatedJoinNodesForConstraints = oldTreatedJoinNodesForConstraints;
    }

    private String resolveRenderedFunctionName(String literalFunctionName) {
        String registeredFunctionName = registeredFunctionsNames.get(literalFunctionName.toLowerCase());
        return registeredFunctionName == null ? literalFunctionName : registeredFunctionName;
    }

    @SuppressWarnings("unchecked")
    protected void renderCountStar() {
        if (jpaProvider.supportsCountStar()) {
            sb.append("COUNT(*)");
        } else {
            renderFunctionFunction(resolveRenderedFunctionName("COUNT_STAR"), (List<Expression>) (List<?>) Collections.emptyList());
        }
    }

    @Override
    public void visit(SubqueryExpression expression) {
        if (expression.getSubquery() instanceof SubqueryInternalBuilder) {
            final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> subquery = (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery();
            final boolean hasFirstResult = subquery.getFirstResult() != 0;
            final boolean hasMaxResults = subquery.getMaxResults() != Integer.MAX_VALUE;
            final boolean hasLimit = hasFirstResult || hasMaxResults;
            final boolean hasSetOperations = subquery instanceof BaseFinalSetOperationBuilder<?, ?>;
            final boolean hasEntityFunctions = subquery.joinManager.hasEntityFunctions();
            final boolean isSimple = !hasLimit && !hasSetOperations && !hasEntityFunctions;

            if (isSimple) {
                sb.append('(');
                sb.append(subquery.getQueryString());
                sb.append(')');
            } else {
                asExpression(subquery).accept(this);
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
            return !hasLimit && !hasSetOperations && !hasEntityFunctions;
        }
        return super.isSimpleSubquery(expression);
    }
    
    protected Expression asExpression(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            BaseFinalSetOperationBuilderImpl<?, ?, ?> operationBuilder = (BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder;
            SetOperationManager operationManager = operationBuilder.setOperationManager;
            
            if (operationManager.getOperator() == null || !operationManager.hasSetOperations()) {
                return asExpression(operationManager.getStartQueryBuilder());
            }
            
            List<Expression> setOperationArgs = new ArrayList<Expression>(operationManager.getSetOperations().size() + 2);
            // Use prefix because hibernate uses UNION as keyword
            setOperationArgs.add(new StringLiteral("SET_" + operationManager.getOperator().name()));
            setOperationArgs.add(asExpression(operationManager.getStartQueryBuilder()));

            List<AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> setOperands = operationManager.getSetOperations();
            int operandsSize = setOperands.size();
            for (int i = 0; i < operandsSize; i++) {
                setOperationArgs.add(asExpression(setOperands.get(i)));
            }
            
            List<? extends OrderByElement> orderByElements = operationBuilder.getOrderByElements();
            if (orderByElements.size() > 0) {
                setOperationArgs.add(new StringLiteral("ORDER_BY"));
                
                int orderByElementsSize = orderByElements.size();
                for (int i = 0; i < orderByElementsSize; i++) {
                    setOperationArgs.add(new StringLiteral(orderByElements.get(i).toString()));
                }
            }
            
            if (operationBuilder.hasLimit()) {
                if (operationBuilder.maxResults != Integer.MAX_VALUE) {
                    setOperationArgs.add(new StringLiteral("LIMIT"));
                    setOperationArgs.add(new NumericLiteral(Integer.toString(operationBuilder.maxResults), NumericType.INTEGER));
                }
                if (operationBuilder.firstResult != 0) {
                    setOperationArgs.add(new StringLiteral("OFFSET"));
                    setOperationArgs.add(new NumericLiteral(Integer.toString(operationBuilder.firstResult), NumericType.INTEGER));
                }
            }
            
            return new FunctionExpression("FUNCTION", setOperationArgs);
        }

        final String queryString = queryBuilder.buildBaseQueryString(externalRepresentation);
        Expression expression = new SubqueryExpression(new Subquery() {
            @Override
            public String getQueryString() {
                return queryString;
            }
        });

        if (queryBuilder.joinManager.hasEntityFunctions()) {
            for (EntityFunctionNode node : queryBuilder.getEntityFunctionNodes(null)) {
                List<Expression> arguments = new ArrayList<Expression>(2);
                arguments.add(new StringLiteral("ENTITY_FUNCTION"));
                arguments.add(expression);

                String valuesClause = node.getValuesClause();
                String valuesAliases = node.getValuesAliases();
                String syntheticPredicate = node.getSyntheticPredicate();

                // TODO: this is a hibernate specific integration detail
                // Replace the subview subselect that is generated for this subselect
                String entityName = node.getEntityClass().getSimpleName();
                arguments.add(new StringLiteral(entityName));
                arguments.add(new StringLiteral(valuesClause));
                arguments.add(new StringLiteral(valuesAliases == null ? "" : valuesAliases));
                arguments.add(new StringLiteral(syntheticPredicate));

                expression = new FunctionExpression("FUNCTION", arguments);
            }
        }

        if (queryBuilder.hasLimit()) {
            final boolean hasFirstResult = queryBuilder.getFirstResult() != 0;
            final boolean hasMaxResults = queryBuilder.getMaxResults() != Integer.MAX_VALUE;
            List<Expression> arguments = new ArrayList<Expression>(2);
            arguments.add(new StringLiteral("LIMIT"));
            arguments.add(expression);

            if (!hasMaxResults) {
                throw new IllegalArgumentException("First result without max results is not supported!");
            } else {
                arguments.add(new NumericLiteral(Integer.toString(queryBuilder.getMaxResults()), NumericType.INTEGER));
            }

            if (hasFirstResult) {
                arguments.add(new NumericLiteral(Integer.toString(queryBuilder.getFirstResult()), NumericType.INTEGER));
            }

            expression = new FunctionExpression("FUNCTION", arguments);
        }

        return expression;
    }

    protected void renderFunctionFunction(String functionName, List<Expression> arguments) {
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        if (registeredFunctions.containsKey(functionName)) {
            sb.append(jpaProvider.getCustomFunctionInvocation(functionName, arguments.size()));
            if (arguments.size() > 0) {
                arguments.get(0).accept(this);
                for (int i = 1; i < arguments.size(); i++) {
                    sb.append(",");
                    arguments.get(i).accept(this);
                }
            }
            sb.append(')');
        } else if (jpaProvider.supportsJpa21()) {
            // Add the JPA 2.1 Function style function
            sb.append("FUNCTION('");
            sb.append(functionName);
            sb.append('\'');

            for (int i = 0; i < arguments.size(); i++) {
                sb.append(',');
                arguments.get(i).accept(this);
            }

            sb.append(')');
        } else {
            throw new IllegalArgumentException("Unknown function [" + functionName + "] is used!");
        }
        setParameterRenderingMode(oldParameterRenderingMode);
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
        if (resolveSelectAliases) {
            // if path expression should not be replaced by select aliases we
            // check for select aliases that have to be replaced with the corresponding
            // path expressions
            if (expression.getBaseNode() == null) {
                AliasInfo aliasInfo;
                if ((aliasInfo = aliasManager.getAliasInfo(expression.toString())) != null) {
                    if (aliasInfo instanceof SelectInfo) {
                        SelectInfo selectAliasInfo = (SelectInfo) aliasInfo;
                        if (selectAliasInfo.getExpression() instanceof PathExpression) {
                            PathExpression clonedSelectExpression = (PathExpression) selectAliasInfo.getExpression().clone(false);
                            clonedSelectExpression.setUsedInCollectionFunction(expression.isUsedInCollectionFunction());
                            clonedSelectExpression.setPathReference(((PathExpression) selectAliasInfo.getExpression()).getPathReference());
                            clonedSelectExpression.accept(this);
                            return;
                        }
                    }
                }
            }
        }
        JoinNode baseNode;
        String field;
        if ((baseNode = (JoinNode) expression.getBaseNode()) == null) {
            super.visit(expression);
        } else if ((field = expression.getField()) == null) {
            if (expression.isUsedInCollectionFunction() || renderAbsolutePath(expression)) {
                super.visit(expression);
            } else {
                boolean valueFunction = needsValueFunction(expression, baseNode, field) && jpaProvider.getCollectionValueFunction() != null;

                if (valueFunction) {
                    sb.append(jpaProvider.getCollectionValueFunction());
                    sb.append('(');
                }

                if (aliasPrefix != null) {
                    sb.append(aliasPrefix);
                }

                baseNode.appendAlias(sb);

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
                    sb.append(") = ");
                    sb.append(treatedJoinNode.getTreatType().getName());
                }

                sb.append(" THEN ");
            }

            boolean valueFunction = needsValueFunction(expression, baseNode, field) && jpaProvider.getCollectionValueFunction() != null;
            // NOTE: There is no need to check for whether the JPA provider support implicit downcasting here
            // If it didn't, the query building would have already failed before. Here we just decide whether to render the treat or not
            boolean renderTreat = jpaProvider.supportsRootTreat();

            if (valueFunction) {
                sb.append(jpaProvider.getCollectionValueFunction());
                sb.append('(');

                if (aliasPrefix != null) {
                    sb.append(aliasPrefix);
                }

                baseNode.appendAlias(sb, renderTreat);
                sb.append(')');
                sb.append(".").append(field);
            } else {
                if (aliasPrefix != null) {
                    sb.append(aliasPrefix);
                }

                baseNode.appendDeReference(sb, field, renderTreat);
            }

            if (addTypeCaseWhen) {
                sb.append(" END");
            }
        }
    }

    private boolean needsValueFunction(PathExpression expression, JoinNode baseNode, String field) {
        return !expression.isCollectionKeyPath() && baseNode.getParentTreeNode() != null && baseNode.getParentTreeNode().isMap() && (field == null || jpaProvider.supportsCollectionValueDereference());
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
        boolean needsBrackets = jpaProvider.needsBracketsForListParamter() && expression.isCollectionValued();

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
            return converter.toString(value);
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
                if (pathType instanceof IdentifiableType<?>) {
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
        super.visit(predicate);
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
