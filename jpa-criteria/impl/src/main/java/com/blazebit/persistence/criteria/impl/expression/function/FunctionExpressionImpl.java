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

import javax.persistence.criteria.Expression;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FunctionExpressionImpl<X> extends AbstractFunctionExpression<X> {

    private static final long serialVersionUID = 1L;

    private final List<Expression<?>> argumentExpressions;

    public FunctionExpressionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, String functionName, Expression<?>... argumentExpressions) {
        super(criteriaBuilder, javaType, functionName);
        this.argumentExpressions = Arrays.asList(argumentExpressions);
    }

    public List<Expression<?>> getArgumentExpressions() {
        return argumentExpressions;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        for (Expression<?> argument : getArgumentExpressions()) {
            visitor.visit(argument);
        }
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        List<Expression<?>> args = getArgumentExpressions();
        buffer.append(getFunctionName()).append('(');
        for (int i = 0; i < args.size(); i++) {
            if (i != 0) {
                buffer.append(',');
            }

            context.apply(args.get(i));
        }
        buffer.append(')');
    }
}
