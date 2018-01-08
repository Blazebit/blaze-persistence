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
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;

import javax.persistence.criteria.Expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConcatFunction extends AbstractExpression<String> {

    private static final long serialVersionUID = 1L;

    private final Expression<String> string1;
    private final Expression<String> string2;

    public ConcatFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> expression1, Expression<String> expression2) {
        super(criteriaBuilder, String.class);
        this.string1 = expression1;
        this.string2 = expression2;
    }

    public ConcatFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Expression<String> string1, String string2) {
        this(criteriaBuilder, string1, wrap(criteriaBuilder, string2));
    }

    public ConcatFunction(BlazeCriteriaBuilderImpl criteriaBuilder, String string1, Expression<String> string2) {
        this(criteriaBuilder, wrap(criteriaBuilder, string1), string2);
    }

    private static Expression<String> wrap(BlazeCriteriaBuilderImpl criteriaBuilder, String string) {
        return new LiteralExpression<String>(criteriaBuilder, string);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(string1);
        visitor.visit(string2);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("CONCAT(");
        context.apply(string1);
        buffer.append(',');
        context.apply(string2);
        buffer.append(')');
    }

}
