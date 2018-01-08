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

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;

import javax.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BetweenPredicate<Y> extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1L;

    private final Expression<? extends Y> expression;
    private final Expression<? extends Y> lowerBound;
    private final Expression<? extends Y> upperBound;

    public BetweenPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<? extends Y> expression, Y lowerBound, Y upperBound) {
        this(criteriaBuilder, negated, expression, criteriaBuilder.literal(lowerBound), criteriaBuilder.literal(upperBound));
    }

    public BetweenPredicate(BlazeCriteriaBuilderImpl criteriaBuilder, boolean negated, Expression<? extends Y> expression, Expression<? extends Y> lowerBound, Expression<? extends Y> upperBound) {
        super(criteriaBuilder, negated);
        this.expression = expression;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public AbstractPredicate copyNegated() {
        return new BetweenPredicate<Y>(criteriaBuilder, !isNegated(), expression, lowerBound, upperBound);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(expression);
        visitor.visit(lowerBound);
        visitor.visit(upperBound);
    }

    @Override
    public void render(RenderContext context) {
        final String operator = isNegated() ? " NOT BETWEEN " : " BETWEEN ";
        context.apply(expression);
        context.getBuffer().append(operator);
        context.apply(lowerBound);
        context.getBuffer().append(" AND ");
        context.apply(upperBound);
    }

}
