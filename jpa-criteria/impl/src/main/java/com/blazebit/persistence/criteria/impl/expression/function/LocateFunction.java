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
public class LocateFunction extends AbstractFunctionExpression<Integer> {

    public static final String NAME = "LOCATE";

    private static final long serialVersionUID = 1L;

    private final Expression<String> pattern;
    private final Expression<String> string;
    private final Expression<Integer> start;

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> pattern, Expression<String> string, Expression<Integer> start) {
        super(criteriaBuilder, Integer.class, NAME);
        this.pattern = pattern;
        this.string = string;
        this.start = start;
    }

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> pattern, Expression<String> string) {
        this(criteriaBuilder, pattern, string, null);
    }

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, String pattern, Expression<String> string) {
        this(criteriaBuilder, new LiteralExpression<String>(criteriaBuilder, pattern), string, null);
    }

    public LocateFunction(BlazeCriteriaBuilderImpl criteriaBuilder, String pattern, Expression<String> string, int start) {
        this(criteriaBuilder, new LiteralExpression<String>(criteriaBuilder, pattern), string, new LiteralExpression<Integer>(criteriaBuilder, start));
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(pattern);
        visitor.visit(start);
        visitor.visit(string);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("LOCATE(");
        context.apply(pattern);
        buffer.append(',');
        context.apply(string);

        if (start != null) {
            buffer.append(',');
            context.apply(start);
        }

        buffer.append(')');
    }

}
