/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import jakarta.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CompoundPredicate extends AbstractPredicate {

    private static final long serialVersionUID = 1L;

    private final BooleanOperator operator;
    private final List<Expression<Boolean>> expressions;

    public CompoundPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, BooleanOperator operator) {
        super(criteriaBuilder, false);
        this.operator = operator;
        this.expressions = new ArrayList<>();
    }

    @SafeVarargs
    public CompoundPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, BooleanOperator operator, Expression<Boolean>... expressions) {
        this(criteriaBuilder, operator);
        Collections.addAll(this.expressions, expressions);
    }

    public CompoundPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, BooleanOperator operator, List<Expression<Boolean>> expressions) {
        super(criteriaBuilder, false);
        this.operator = operator;
        this.expressions = expressions;
    }

    private CompoundPredicate(CompoundPredicate original) {
        super(original.criteriaBuilder, false);
        this.operator = original.getNegatedOperator();
        this.expressions = new ArrayList<>(original.expressions.size());
        for (Expression<Boolean> expression : original.expressions) {
            this.expressions.add(original.criteriaBuilder.not(expression));
        }
    }

    @Override
    public BooleanOperator getOperator() {
        return operator;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        return expressions;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        for (Expression<?> expression : expressions) {
            visitor.visit(expression);
        }
    }

    @Override
    public void render(RenderContext context) {
        List<Expression<Boolean>> exprs = expressions;
        int size = exprs.size();
        switch (size) {
            case 0: {
                if (operator == BooleanOperator.AND ^ isNegated()) {
                    context.getBuffer().append("1=1");
                } else {
                    context.getBuffer().append("1=0");
                }
                break;
            }
            case 1: {
                context.apply(exprs.get(0));
                break;
            }
            default: {
                final String operatorString = operator == BooleanOperator.AND ? " AND " : " OR ";
                for (int i = 0; i < size; i++) {
                    if (i != 0) {
                        context.getBuffer().append(operatorString);
                    }
                    context.getBuffer().append('(');
                    context.apply(exprs.get(i));
                    context.getBuffer().append(')');
                }
            }
        }
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new CompoundPredicate(this);
    }

    private BooleanOperator getNegatedOperator() {
        if (this.operator == BooleanOperator.AND) {
            return BooleanOperator.OR;
        } else {
            return BooleanOperator.AND;
        }
    }
}
