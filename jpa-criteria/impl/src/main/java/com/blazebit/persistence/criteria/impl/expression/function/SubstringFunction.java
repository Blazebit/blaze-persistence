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

package com.blazebit.persistence.criteria.impl.expression.function;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;

import javax.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubstringFunction extends AbstractFunctionExpression<String> {

    public static final String NAME = "SUBSTRING";

    private static final long serialVersionUID = 1L;

    private final Expression<String> value;
    private final Expression<Integer> start;
    private final Expression<Integer> length;

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, Expression<Integer> start, Expression<Integer> length) {
        super(criteriaBuilder, String.class, NAME);
        this.value = value;
        this.start = start;
        this.length = length;
    }

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, Expression<Integer> start) {
        this(criteriaBuilder, value, start, (Expression<Integer>) null);
    }

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, int start) {
        this(criteriaBuilder, value, new LiteralExpression<Integer>(criteriaBuilder, start));
    }

    public SubstringFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> value, int start, int length) {
        this(criteriaBuilder, value, new LiteralExpression<Integer>(criteriaBuilder, start), new LiteralExpression<Integer>(criteriaBuilder, length));
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(value);
        visitor.visit(start);
        visitor.visit(length);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("SUBSTRING(");
        context.apply(value);
        buffer.append(',');
        context.apply(start);

        if (length != null) {
            buffer.append(',');
            context.apply(length);
        }

        buffer.append(')');
    }

}
