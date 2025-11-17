/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InPredicate<T> extends AbstractSimplePredicate implements BlazeCriteriaBuilderImpl.In<T> {

    private static final long serialVersionUID = 1L;

    private final Expression<? extends T> expression;
    private final List<Expression<? extends T>> values;
    private boolean allValues = true;

    public InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends T> expression) {
        this(criteriaBuilder, expression, new ArrayList<Expression<? extends T>>());
    }

    public InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<? extends T> expression, List<Expression<? extends T>> values) {
        super(criteriaBuilder, false);

        if (expression instanceof ParameterExpressionImpl<?>) {
            throw new IllegalArgumentException("A parameter can't be the left hand expression of an in predicate!");
        }

        this.expression = expression;
        this.values = values;

        for (int i = 0; i < values.size(); i++) {
            Expression<? extends T> expr = values.get(i);
            if (expr == null) {
                throw new IllegalArgumentException("Null expression at index: " + (i + 1));
            }
            allValues = allValues && expr instanceof ParameterExpressionImpl<?> && ((ParameterExpressionImpl<?>) expr).getValue() != null;
        }
    }

    // Copy-constructor
    private InPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<? extends T> expression, List<Expression<? extends T>> values, boolean allValues) {
        super(criteriaBuilder, negated);
        this.expression = expression;
        this.values = values;
        this.allValues = allValues;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new InPredicate<T>(criteriaBuilder, !isNegated(), expression, values, allValues);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Expression<T> getExpression() {
        return (Expression<T>) expression;
    }

    @Override
    public InPredicate<T> value(T value) {
        return value(criteriaBuilder.value(value));
    }

    @Override
    public InPredicate<T> value(Expression<? extends T> value) {
        values.add(value);
        allValues = allValues && value instanceof ParameterExpressionImpl<?> && ((ParameterExpressionImpl<?>) value).getValue() != null;
        return this;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(expression);

        for (Expression<?> expr : values) {
            visitor.visit(expr);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(RenderContext context) {
        List<Expression<? extends T>> values = this.values;
        StringBuilder buffer = context.getBuffer();

        switch (values.size()) {
            case 0:
                if (isNegated()) {
                    buffer.append("1=1");
                } else {
                    buffer.append("1=0");
                }
                return;
            case 1:
                Expression<? extends T> first = values.get(0);
                if (first instanceof Subquery<?> || (first instanceof ParameterExpressionImpl<?> && Collection.class.isAssignableFrom(((ParameterExpressionImpl<?>) first).getParameterType())) || allValues) {
                    context.apply(expression);

                    if (isNegated()) {
                        buffer.append(" NOT");
                    }

                    buffer.append(" IN ");
                    if (allValues) {
                        List<Object> literalValues = new ArrayList<>(values.size());
                        for (ParameterExpressionImpl<T> value : (Collection<ParameterExpressionImpl<T>>) (Collection<?>) values) {
                            literalValues.add(value.getRealValue());
                        }

                        final String paramName = context.registerLiteralParameterBinding(literalValues, Collection.class);
                        buffer.append(':').append(paramName);
                    } else {
                        context.apply(first);
                    }
                    return;
                }
                //CHECKSTYLE:OFF: FallThrough
            default:
                //CHECKSTYLE:ON: FallThrough
                context.apply(expression);

                if (isNegated()) {
                    buffer.append(" NOT");
                }

                buffer.append(" IN ");
                if (allValues) {
                    List<Object> literalValues = new ArrayList<>(values.size());
                    for (ParameterExpressionImpl<T> value : (Collection<ParameterExpressionImpl<T>>) (Collection<?>) values) {
                        literalValues.add(value.getRealValue());
                    }

                    final String paramName = context.registerLiteralParameterBinding(literalValues, Collection.class);
                    buffer.append(':').append(paramName);
                } else {
                    buffer.append('(');
                    for (int i = 0; i < values.size(); i++) {
                        if (i != 0) {
                            buffer.append(',');
                        }
                        context.apply(values.get(i));
                    }
                    buffer.append(')');
                }
        }
    }

}
