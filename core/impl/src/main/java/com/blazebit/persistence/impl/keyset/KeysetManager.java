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

package com.blazebit.persistence.impl.keyset;

import java.io.Serializable;
import java.util.List;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.OrderByExpression;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.ResolvingQueryGenerator;
import com.blazebit.persistence.impl.SimpleQueryGenerator;
import com.blazebit.persistence.impl.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class KeysetManager extends AbstractKeysetBuilderEndedListener {

    private static final String KEY_SET_PARAMETER_NAME = "_keysetParameter";

    private final ResolvingQueryGenerator queryGenerator;
    private final ParameterManager parameterManager;
    private List<OrderByExpression> orderByExpressions;

    public KeysetManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager) {
        this.queryGenerator = queryGenerator;
        this.parameterManager = parameterManager;
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

    public void buildKeysetPredicate(StringBuilder sb) {
        int expressionCount = orderByExpressions.size();
        KeysetLink keysetLink = getKeysetLink();
        KeysetMode keysetMode = keysetLink.getKeysetMode();
        Keyset keyset = keysetLink.getKeyset();
        Serializable[] key;

        key = keyset.getTuple();

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
                        applyKeysetNotNullableItem(orderByExpr, sb, expr, i, key, keysetMode);
                        sb.append(" OR ");
                        applyKeysetNullItem(sb, expr, false);
                        sb.append(')');
                    } else {
                        // Case for previous and null last or not previous and null first
                        applyKeysetNotNullableItem(orderByExpr, sb, expr, i, key, keysetMode);
                    }
                }
            } else {
                applyKeysetNotNullableItem(orderByExpr, sb, expr, i, key, keysetMode);
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

    private void applyKeysetNotNullableItem(OrderByExpression orderByExpr, StringBuilder sb, Expression expr, int i, Serializable[] key, KeysetMode keysetMode) {
        String operator;
        switch (keysetMode) {
            case SAME:
                operator = orderByExpr.isAscending() ? ">=" : "<=";
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

        applyKeysetItem(sb, expr, operator, i, key[i]);
    }

    private void applyKeysetItem(StringBuilder sb, Expression expr, String operator, int position, Serializable keyElement) {
        queryGenerator.setClauseType(ClauseType.WHERE);
        queryGenerator.setQueryBuffer(sb);
        expr.accept(queryGenerator);
        queryGenerator.setClauseType(null);
        sb.append(" ");
        sb.append(operator);
        sb.append(" :");
        String parameterName = new StringBuilder(KEY_SET_PARAMETER_NAME).append('_').append(position).toString();
        sb.append(parameterName);
        parameterManager.addParameterMapping(parameterName, keyElement, ClauseType.WHERE);
    }

    private void applyKeysetNullItem(StringBuilder sb, Expression expr, boolean not) {
        queryGenerator.setClauseType(ClauseType.WHERE);
        queryGenerator.setQueryBuffer(sb);
        expr.accept(queryGenerator);
        queryGenerator.setClauseType(null);

        if (not) {
            sb.append(" IS NOT NULL");
        } else {
            sb.append(" IS NULL");
        }
    }

}
