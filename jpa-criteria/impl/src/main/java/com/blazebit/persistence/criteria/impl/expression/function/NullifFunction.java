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
public class NullifFunction<T> extends AbstractExpression<T> {

    private static final long serialVersionUID = 1L;

    private final Expression<? extends T> expression1;
    private final Expression<?> expression2;

    public NullifFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, Expression<? extends T> expression1, Expression<?> expression2) {
        super(criteriaBuilder, determineType(javaType, expression1));
        this.expression1 = expression1;
        this.expression2 = expression2;
    }

    public NullifFunction(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, Expression<? extends T> expression1, Object expression2) {
        super(criteriaBuilder, determineType(javaType, expression1));
        this.expression1 = expression1;
        this.expression2 = new LiteralExpression<Object>(criteriaBuilder, expression2);
    }

    @SuppressWarnings({"unchecked"})
    private static <T> Class<T> determineType(Class<T> javaType, Expression expression) {
        return javaType != null ? javaType : (Class<T>) expression.getJavaType();
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.visit(expression1);
        visitor.visit(expression2);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("NULLIF(");
        context.apply(expression1);
        buffer.append(',');
        context.apply(expression2);
        buffer.append(')');
    }

}
