/*
 * Copyright 2014 - 2017 Blazebit.
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
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.ArithmeticExpression;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.MapValueExpression;
import com.blazebit.persistence.impl.expression.NullExpression;
import com.blazebit.persistence.impl.expression.NumericLiteral;
import com.blazebit.persistence.impl.expression.NumericType;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.StringLiteral;
import com.blazebit.persistence.impl.expression.Subquery;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.TreatExpression;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.PredicateQuantifier;
import com.blazebit.persistence.impl.util.TypeConverter;
import com.blazebit.persistence.impl.util.TypeUtils;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.OrderByElement;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0
 */
public class ResolvingQueryGenerator extends SimpleQueryGenerator {

    protected String aliasPrefix;
    private boolean resolveSelectAliases = true;
    private Set<JoinNode> renderedJoinNodes;
    private ClauseType clauseType;
    private final AliasManager aliasManager;
    private final ParameterManager parameterManager;
    private final EntityMetamodel metamodel;
    private final AssociationParameterTransformerFactory parameterTransformerFactory;
    private final JpaProvider jpaProvider;
    private final Map<String, JpqlFunction> registeredFunctions;

    public ResolvingQueryGenerator(AliasManager aliasManager, ParameterManager parameterManager, AssociationParameterTransformerFactory parameterTransformerFactory, EntityMetamodel metamodel, JpaProvider jpaProvider, Map<String, JpqlFunction> registeredFunctions) {
        this.aliasManager = aliasManager;
        this.parameterManager = parameterManager;
        this.metamodel = metamodel;
        this.parameterTransformerFactory = parameterTransformerFactory;
        this.jpaProvider = jpaProvider;
        this.registeredFunctions = registeredFunctions;
    }

    @Override
    public void visit(NullExpression expression) {
        sb.append(jpaProvider.getNullExpression());
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
        if (com.blazebit.persistence.impl.util.ExpressionUtils.isOuterFunction(expression)) {
            // Outer can only have paths, no need to set expression context for parameters
            expression.getExpressions().get(0).accept(this);
        } else if (ExpressionUtils.isFunctionFunctionExpression(expression)) {
            final List<Expression> arguments = expression.getExpressions();
            final String functionName = ExpressionUtils.unwrapStringLiteral(arguments.get(0).toString());
            final List<Expression> argumentsWithoutFunctionName;
            if (arguments.size() > 1) {
                argumentsWithoutFunctionName = arguments.subList(1, arguments.size());
            } else {
                argumentsWithoutFunctionName = Collections.emptyList();
            }
            renderFunctionFunction(functionName, argumentsWithoutFunctionName);
        } else if (isCountStarFunction(expression)) {
            renderCountStar();
        } else {
            super.visit(expression);
        }
    }

    @SuppressWarnings("unchecked")
    protected void renderCountStar() {
        if (jpaProvider.supportsCountStar()) {
            sb.append("COUNT(*)");
        } else {
            renderFunctionFunction("COUNT_STAR", (List<Expression>) (List<?>) Collections.emptyList());
        }
    }

    @Override
    public void visit(SubqueryExpression expression) {
        sb.append('(');

        if (expression.getSubquery() instanceof SubqueryInternalBuilder) {
            final SubqueryInternalBuilder<?> subquery = (SubqueryInternalBuilder<?>) expression.getSubquery();
            final boolean hasFirstResult = subquery.getFirstResult() != 0;
            final boolean hasMaxResults = subquery.getMaxResults() != Integer.MAX_VALUE;
            final boolean hasLimit = hasFirstResult || hasMaxResults;
            final boolean hasSetOperations = subquery instanceof BaseFinalSetOperationBuilder<?, ?>;
            final boolean isSimple = !hasLimit && !hasSetOperations;

            if (isSimple) {
                sb.append(subquery.getQueryString());
            } else if (hasSetOperations) {
                asExpression((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) subquery).accept(this);
            } else {
                asExpression((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) subquery).accept(this);
            }
        } else {
            sb.append(expression.getSubquery().getQueryString());
        }
        
        sb.append(')');
    }
    
    protected Expression asExpression(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryBuilder instanceof BaseFinalSetOperationBuilderImpl<?, ?, ?>) {
            BaseFinalSetOperationBuilderImpl<?, ?, ?> operationBuilder = (BaseFinalSetOperationBuilderImpl<?, ?, ?>) queryBuilder;
            SetOperationManager operationManager = operationBuilder.setOperationManager;
            
            if (operationManager.getOperator() == null || !operationManager.hasSetOperations()) {
                return asExpression(operationManager.getStartQueryBuilder());
            }
            
            List<Expression> setOperationArgs = new ArrayList<Expression>(operationManager.getSetOperations().size() + 2);
            StringBuilder nameSb = new StringBuilder();
            // Use prefix because hibernate uses UNION as keyword
            nameSb.append("SET_");
            nameSb.append(operationManager.getOperator().name());
            setOperationArgs.add(new StringLiteral(nameSb.toString()));
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
                    StringBuilder argSb = new StringBuilder(20);
                    argSb.append(orderByElements.get(i).toString());
                    setOperationArgs.add(new StringLiteral(argSb.toString()));
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
            
            Expression functionExpr = new FunctionExpression("FUNCTION", setOperationArgs);
            return functionExpr;
        }

        String queryString = queryBuilder.getQueryString();
        final StringBuilder subquerySb = new StringBuilder(queryString.length() + 2);
        subquerySb.append(queryString);
        Expression expression = new SubqueryExpression(new Subquery() {
            @Override
            public String getQueryString() {
                return subquerySb.toString();
            }
        });

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
        if (registeredFunctions.containsKey(functionName.toLowerCase())) {
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
        if (expression.getBaseNode() == null) {
            super.visit(expression);
        } else if (expression.getField() == null) {
            if (expression.isUsedInCollectionFunction() || renderAbsolutePath(expression)) {
                super.visit(expression);
            } else {
                boolean valueFunction = needsValueFunction(expression) && jpaProvider.getCollectionValueFunction() != null;

                if (valueFunction) {
                    sb.append(jpaProvider.getCollectionValueFunction());
                    sb.append('(');
                }

                if (aliasPrefix != null) {
                    sb.append(aliasPrefix);
                }

                JoinNode baseNode = (JoinNode) expression.getBaseNode();
                baseNode.appendAlias(sb, null);

                if (valueFunction) {
                    sb.append(')');
                }
            }
        } else {
            // Thats e.g. TREAT(TREAT(alias).property)
            if (expression.hasTreatedSubpath()) {
                // Actually we know that the treated subpath must be the first part of the path
                expression.getExpressions().get(0).accept(this);
                sb.append(".").append(expression.getField());
            } else {
                boolean valueFunction = needsValueFunction(expression) && jpaProvider.getCollectionValueFunction() != null;
                JoinNode baseNode = (JoinNode) expression.getBaseNode();

                if (valueFunction) {
                    sb.append(jpaProvider.getCollectionValueFunction());
                    sb.append('(');

                    if (aliasPrefix != null) {
                        sb.append(aliasPrefix);
                    }

                    baseNode.appendAlias(sb, null);
                    sb.append(')');
                    sb.append(".").append(expression.getField());
                } else {
                    if (aliasPrefix != null) {
                        sb.append(aliasPrefix);
                    }

                    baseNode.appendAlias(sb, expression.getField());
                }
            }

        }
    }

    private boolean needsValueFunction(PathExpression expression) {
        JoinNode baseNode = (JoinNode) expression.getBaseNode();
        return !expression.isCollectionKeyPath() && baseNode.getParentTreeNode() != null && baseNode.getParentTreeNode().isMap() && (expression.getField() == null || jpaProvider.supportsCollectionValueDereference());
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

    public void setAliasPrefix(String aliasPrefix) {
        this.aliasPrefix = aliasPrefix;
    }

    public void setRenderedJoinNodes(Set<JoinNode> renderedJoinNodes) {
        this.renderedJoinNodes = renderedJoinNodes;
    }

    public void setClauseType(ClauseType clauseType) {
        this.clauseType = clauseType;
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
                ParameterManager.ParameterImpl<Object> param = (ParameterManager.ParameterImpl<Object>) parameterManager.getParameter(parameterExpression.getName());
                Class<?> associationType = getAssociationType(predicate.getLeft(), right);
                ParameterManager.ParameterValueTranformer tranformer = parameterTransformerFactory.getToEntityTranformer(associationType);
                param.setTranformer(tranformer);
            } else if (right instanceof PathExpression) {
                renderEquality(predicate.getLeft(), right, predicate.isNegated(), PredicateQuantifier.ONE);
            }
        } else {
            super.visit(predicate);
        }
    }

    private Class<?> getAssociationType(Expression expression1, Expression expression2) {
        if (expression1 instanceof PathExpression) {
            return ((PathExpression) expression1).getPathReference().getType();
        }

        return ((PathExpression) expression2).getPathReference().getType();
    }

    @Override
    public void visit(final EqPredicate predicate) {
        renderEquality(predicate.getLeft(), predicate.getRight(), predicate.isNegated(), predicate.getQuantifier());
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
                Class<?> pathType = pathExpression.getPathReference().getType();
                ManagedType<?> managedType = metamodel.getManagedType(pathType);
                if (managedType instanceof IdentifiableType<?>) {
                    String idName = JpaUtils.getIdAttribute((IdentifiableType<?>) managedType).getName();
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
        } else {
            sb.append(" IS NULL");
        }
        setParameterRenderingMode(oldParameterRenderingMode);
    }
}
