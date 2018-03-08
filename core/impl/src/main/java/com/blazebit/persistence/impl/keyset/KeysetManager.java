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

package com.blazebit.persistence.impl.keyset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.OrderByExpression;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.ResolvingQueryGenerator;
import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.impl.function.rowvalue.RowValueComparisonFunction;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpaProvider;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class KeysetManager extends AbstractKeysetBuilderEndedListener {

    private static final String KEY_SET_PARAMETER_NAME = "_keysetParameter";

    private final ResolvingQueryGenerator queryGenerator;
    private final ParameterManager parameterManager;
    private final JpaProvider jpaProvider;
    private final DbmsDialect dbmsDialect;
    private List<OrderByExpression> orderByExpressions;

    public KeysetManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, JpaProvider jpaProvider, DbmsDialect dbmsDialect) {
        this.queryGenerator = queryGenerator;
        this.parameterManager = parameterManager;
        this.jpaProvider = jpaProvider;
        this.dbmsDialect = dbmsDialect;
    }

    public boolean hasKeyset() {
        return getKeysetLink() != null;
    }

    public void initialize(List<OrderByExpression> orderByExpressions) {
        this.orderByExpressions = orderByExpressions;

        KeysetLink keysetLink = getKeysetLink();
        // Checks if keyset fits for order by expressions
        keysetLink.initialize(orderByExpressions);
    }

    public void buildOptimizedKeysetPredicate(StringBuilder sb) {
        KeysetLink keysetLink = getKeysetLink();
        KeysetMode keysetMode = keysetLink.getKeysetMode();
        Keyset keyset = keysetLink.getKeyset();
        Serializable[] key = keyset.getTuple();
        OrderByExpression extractedNonNullableExpression = null;

        boolean hasNullableOrderBys = false;
        for (OrderByExpression orderByExpression : orderByExpressions) {
            if (orderByExpression.isNullable()) {
                hasNullableOrderBys = true;
                break;
            }
        }

        extractedNonNullableExpression = orderByExpressions.get(0);

        // We can only use row value based keyset predicates if the dbms supports row values and row value comparison and
        // if all order bys are non-nullable because null elements would break the row value comparison.
        if (hasNullableOrderBys || !dbmsDialect.supportsFullRowValueComparison()) {
            // Under certain conditions, we cannot render an optimized form because we would need to include
            // null checks involving disjunction on the top predicate level which would contradict the main idea of the
            // optimization.
            boolean optimizationAllowed = !extractedNonNullableExpression.isNullable()
                    || keysetMode == KeysetMode.NEXT && extractedNonNullableExpression.isNullFirst() && key[0] != null
                    || keysetMode == KeysetMode.PREVIOUS && !extractedNonNullableExpression.isNullFirst() && key[0] != null;
            if (optimizationAllowed) {
                applyOptimizedKeysetNotNullItem(extractedNonNullableExpression, sb, 0, key[0], keysetMode, false);
                if (orderByExpressions.size() > 1) {
                    sb.append(" AND NOT (");
                    applyKeysetItem(sb, extractedNonNullableExpression.getExpression(), "=", 0, key[0]);
                    sb.append(" AND ");
                    buildOptimizedPredicate0(keysetMode, key, sb, orderByExpressions);
                    sb.append(")");
                }
            } else {
                buildKeysetPredicate0(keysetMode, key, sb, orderByExpressions);
            }
        } else {
            // we can use row value constructor syntax
            List<String> leftHandsideExpressions = new ArrayList<>(orderByExpressions.size());
            List<String> rightHandsideExpressions = new ArrayList<>(orderByExpressions.size());

            StringBuilder renderingBuffer = new StringBuilder();
            queryGenerator.setClauseType(ClauseType.WHERE);
            queryGenerator.setQueryBuffer(renderingBuffer);
            queryGenerator.setClauseType(null);

            for (int i = 0; i < orderByExpressions.size(); i++) {
                OrderByExpression orderByExpression = orderByExpressions.get(i);

                renderingBuffer.setLength(0);
                queryGenerator.generate(orderByExpression.getExpression());
                String renderedOrderByExpr = renderingBuffer.toString();

                renderingBuffer.setLength(0);
                applyKeysetParameter(renderingBuffer, i, key[i]);
                String renderedKeysetParameter = renderingBuffer.toString();

                if (orderByExpression.isDescending() && keysetMode != KeysetMode.PREVIOUS) {
                    leftHandsideExpressions.add(renderedOrderByExpr);
                    rightHandsideExpressions.add(renderedKeysetParameter);
                } else {
                    leftHandsideExpressions.add(renderedKeysetParameter);
                    rightHandsideExpressions.add(renderedOrderByExpr);
                }
            }

            renderRowValueComparison(sb, keysetMode == KeysetMode.SAME ? "<=" : "<", leftHandsideExpressions, rightHandsideExpressions);
        }
    }

    private void renderRowValueComparison(StringBuilder sb, String operator, Collection<String> leftHandsideExpressions, Collection<String> rightHandsideExpressions) {
        sb.append(jpaProvider.getCustomFunctionInvocation(RowValueComparisonFunction.FUNCTION_NAME, leftHandsideExpressions.size() + rightHandsideExpressions.size() + 1))
                .append('\'').append(operator).append('\'');
        renderArguments(sb, leftHandsideExpressions);
        renderArguments(sb, rightHandsideExpressions);
        sb.append(") = true");
    }

    private void renderArguments(StringBuilder sb, Iterable<String> arguments) {
        Iterator<String> expressionIterator = arguments.iterator();
        if (expressionIterator.hasNext()) {
            sb.append(',');
            sb.append(expressionIterator.next());
            while (expressionIterator.hasNext()) {
                sb.append(',').append(expressionIterator.next());
            }
        }
    }

    public void buildKeysetPredicate(StringBuilder sb) {
        KeysetLink keysetLink = getKeysetLink();
        KeysetMode keysetMode = keysetLink.getKeysetMode();
        Keyset keyset = keysetLink.getKeyset();
        Serializable[] key;

        key = keyset.getTuple();

        buildKeysetPredicate0(keysetMode, key, sb, orderByExpressions);
    }

    private void buildOptimizedPredicate0(KeysetMode keysetMode, Serializable[] key, StringBuilder sb, List<OrderByExpression> orderByExpressions) {
        int expressionCount = orderByExpressions.size();
        int brackets = 1;
        sb.append('(');

        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        // Render a keyset predicate that includes the remaining order by clauses except for the extracted one
        // The logic needs to be inverted because the created <predicate> will reside in a negation aka
        // WHERE firstOrderBy <= :keyset AND NOT(firstOrderBy = :keyset AND <predicate>)
        for (int i = 1; i < expressionCount; i++) {
            boolean itemRendered = true;
            boolean isNotLast = i + 1 != expressionCount;
            OrderByExpression orderByExpr = orderByExpressions.get(i);
            Expression expr = orderByExpr.getExpression();

            if (orderByExpr.isNullable()) {
                if (key[i] == null) {
                    if ((keysetMode == KeysetMode.PREVIOUS) == orderByExpr.isNullFirst()) {
                        // we need to explicitely exclude non-null values
                        applyKeysetNullItem(sb, expr, true);
                    } else {
                        itemRendered = false;
                    }
                } else {
                    if ((keysetMode == KeysetMode.NEXT) == orderByExpr.isNullFirst()) {
                        applyOptimizedKeysetNotNullItem(orderByExpr, sb, i, key[i], keysetMode, true);
                    } else {
                        applyKeysetNullItem(sb, expr, true);
                        sb.append(" AND ");
                        applyOptimizedKeysetNotNullItem(orderByExpr, sb, i, key[i], keysetMode, true);
                    }
                }
            } else {
                applyOptimizedKeysetNotNullItem(orderByExpr, sb, i, key[i], keysetMode, true);
            }

            if (isNotLast) {
                if (itemRendered) {
                    brackets++;
                    sb.append(" OR (");
                }
                if (key[i] == null) {
                    applyKeysetNullItem(sb, expr, false);
                } else {
                    if (orderByExpr.isNullable() && (keysetMode == KeysetMode.PREVIOUS) == orderByExpr.isNullFirst()) {
                        applyKeysetNullItem(sb, expr, true);
                        sb.append(" AND ");
                    }
                    applyKeysetItem(sb, expr, "=", i, key[i]);
                }

                sb.append(" AND ");

                if (i + 2 != expressionCount) {
                    brackets++;
                    sb.append('(');
                }
            }
        }

        for (int i = 0; i < brackets; i++) {
            sb.append(')');
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
    }

    private void buildKeysetPredicate0(KeysetMode keysetMode, Serializable[] key, StringBuilder sb, List<OrderByExpression> orderByExpressions) {
        int expressionCount = orderByExpressions.size();
        boolean generateEqualPredicate = true;
        int brackets = 0;

        SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.CASE_WHEN);

        // We wrap the whole thing in brackets
        brackets++;
        sb.append('(');

        for (int i = 0; i < expressionCount; i++) {
            boolean isNotLast = i + 1 != expressionCount;

            OrderByExpression orderByExpr = orderByExpressions.get(i);
            Expression expr = orderByExpr.getExpression();

            if (orderByExpr.isNullable()) {
                boolean isPrevious = keysetMode == KeysetMode.PREVIOUS;

                if (key[i] == null) {
                    if (orderByExpr.isNullFirst() == isPrevious) {
                        // Case for previous and null first or not previous and null last
                        generateEqualPredicate = false;
                        applyKeysetNullItem(sb, expr, false);
                    } else {
                        // Case for previous and null last or not previous and null first
                        applyKeysetNullItem(sb, expr, true);
                    }
                } else {
                    if (orderByExpr.isNullFirst() == isPrevious) {
                        // Case for previous and null first or not previous and null last
                        sb.append('(');
                        applyKeysetNotNullableItem(orderByExpr, sb, i, key[i], keysetMode, !isNotLast);
                        sb.append(" OR ");
                        applyKeysetNullItem(sb, expr, false);
                        sb.append(')');
                    } else {
                        // Case for previous and null last or not previous and null first
                        applyKeysetNotNullableItem(orderByExpr, sb, i, key[i], keysetMode, !isNotLast);
                    }
                }
            } else {
                applyKeysetNotNullableItem(orderByExpr, sb, i, key[i], keysetMode, !isNotLast);
            }

            if (isNotLast) {
                if (generateEqualPredicate) {
                    brackets++;
                    sb.append(" OR (");
                    if (key[i] == null) {
                        applyKeysetNullItem(sb, expr, false);
                    } else {
                        applyKeysetItem(sb, expr, "=", i, key[i]);
                    }
                }

                sb.append(" AND ");
                if (i + 2 != expressionCount) {
                    brackets++;
                    sb.append('(');
                }

                generateEqualPredicate = true;
            }
        }

        for (int i = 0; i < brackets; i++) {
            sb.append(')');
        }

        queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
    }

    private void applyOptimizedKeysetNotNullItem(OrderByExpression orderByExpr, StringBuilder sb, int i, Serializable keyElement, KeysetMode keysetMode, boolean negated) {
        String operator;
        switch (keysetMode) {
            case SAME:
                if (negated) {
                    operator = orderByExpr.isAscending() ? "<" : ">";
                } else {
                    operator = orderByExpr.isAscending() ? ">=" : "<=";
                }
                break;
            case NEXT:
                if (negated) {
                    if (orderByExpr.isUnique()) {
                        operator = orderByExpr.isAscending() ? "<=" : ">=";
                    } else {
                        operator = orderByExpr.isAscending() ? "<" : ">";
                    }
                } else {
                    if (orderByExpr.isUnique()) {
                        operator = orderByExpr.isAscending() ? ">" : "<";
                    } else {
                        operator = orderByExpr.isAscending() ? ">=" : "<=";
                    }
                }

                break;
            case PREVIOUS:
                if (negated) {
                    if (orderByExpr.isUnique()) {
                        operator = orderByExpr.isAscending() ? ">=" : "<=";
                    } else {
                        operator = orderByExpr.isAscending() ? ">" : "<";
                    }
                } else {
                    if (orderByExpr.isUnique()) {
                        operator = orderByExpr.isAscending() ? "<" : ">";
                    } else {
                        operator = orderByExpr.isAscending() ? "<=" : ">=";
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown key set mode: " + keysetMode);
        }

        applyKeysetItem(sb, orderByExpr.getExpression(), operator, i, keyElement);
    }

    private void applyKeysetNotNullableItem(OrderByExpression orderByExpr, StringBuilder sb, int i, Serializable keyElement, KeysetMode keysetMode, boolean lastOrderBy) {
        String operator;
        switch (keysetMode) {
            case SAME:
                if (lastOrderBy) {
                    operator = orderByExpr.isAscending() ? ">=" : "<=";
                } else {
                    operator = orderByExpr.isAscending() ? ">" : "<";
                }
                break;
            case NEXT:
                operator = orderByExpr.isAscending() ? ">" : "<";
                break;
            case PREVIOUS:
                operator = orderByExpr.isAscending() ? "<" : ">";
                break;
            default:
                throw new IllegalArgumentException("Unknown key set mode: " + keysetMode);
        }

        applyKeysetItem(sb, orderByExpr.getExpression(), operator, i, keyElement);
    }

    private void applyKeysetItem(StringBuilder sb, Expression expr, String operator, int position, Serializable keyElement) {
        renderOrderByExpression(sb, expr);
        sb.append(' ');
        sb.append(operator).append(' ');
        applyKeysetParameter(sb, position, keyElement);
    }

    private void applyKeysetNullItem(StringBuilder sb, Expression expr, boolean not) {
        renderOrderByExpression(sb, expr);
        if (not) {
            sb.append(" IS NOT NULL");
        } else {
            sb.append(" IS NULL");
        }
    }

    private void renderOrderByExpression(StringBuilder sb, Expression expr) {
        queryGenerator.setClauseType(ClauseType.WHERE);
        queryGenerator.setQueryBuffer(sb);
        queryGenerator.generate(expr);
        queryGenerator.setClauseType(null);
    }

    private void applyKeysetParameter(StringBuilder sb, int position, Serializable keyElement) {
        sb.append(":");
        String parameterName = new StringBuilder(KEY_SET_PARAMETER_NAME).append('_').append(position).toString();
        sb.append(parameterName);
        parameterManager.addParameterMapping(parameterName, keyElement, ClauseType.WHERE);
    }

}
