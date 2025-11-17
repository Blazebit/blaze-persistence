/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.BlazeAggregateFunctionExpression;
import com.blazebit.persistence.criteria.BlazeWindow;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public class AggregationFunctionExpressionImpl<T> extends WindowFunctionExpressionImpl<T> implements BlazeAggregateFunctionExpression<T> {

    private static final long serialVersionUID = 1L;

    private final boolean distinct;
    private Predicate filter;

    public AggregationFunctionExpressionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType, String functionName, boolean distinct, Expression<?> argument) {
        super(criteriaBuilder, returnType, functionName, argument);
        this.distinct = distinct;
    }

    public AggregationFunctionExpressionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, String functionName, boolean distinct, Expression<?>... argumentExpressions) {
        super(criteriaBuilder, javaType, functionName, argumentExpressions);
        this.distinct = distinct;
    }

    @Override
    public Predicate getFilter() {
        return filter;
    }

    @Override
    public BlazeAggregateFunctionExpression<T> filter(Predicate filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public BlazeAggregateFunctionExpression<T> window(BlazeWindow window) {
        super.window(window);
        return this;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        super.visitParameters(visitor);
        if (filter != null) {
            ((AbstractSelection<?>) filter).visitParameters(visitor);
        }
    }

    @Override
    public void render(RenderContext context) {
        StringBuilder buffer = context.getBuffer();
        if (distinct) {
            List<Expression<?>> args = getArgumentExpressions();
            buffer.append(getFunctionName()).append('(');
            for (int i = 0; i < args.size(); i++) {
                if (i != 0) {
                    buffer.append(',');
                } else {
                    buffer.append("DISTINCT ");
                }

                context.apply(args.get(i));
            }
            buffer.append(')');
            renderFilter(context);
            renderWindow(context);
        } else {
            super.render(context);
        }
    }

    protected void renderFilter(RenderContext context) {
        if (filter == null) {
            return;
        }
        StringBuilder buffer = context.getBuffer();
        buffer.append(" FILTER (WHERE ");
        ((AbstractExpression<?>) filter).render(context);
        buffer.append(')');
    }
}
