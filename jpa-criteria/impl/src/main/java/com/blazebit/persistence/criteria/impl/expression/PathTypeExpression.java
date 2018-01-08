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
import com.blazebit.persistence.criteria.impl.path.AbstractPath;
import com.blazebit.persistence.criteria.impl.path.TreatedPath;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PathTypeExpression<T> extends AbstractExpression<T> {

    private static final long serialVersionUID = 1L;

    private final AbstractPath<T> path;

    public PathTypeExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, AbstractPath<T> path) {
        super(criteriaBuilder, javaType);
        this.path = path;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    public void render(RenderContext context) {
        StringBuilder buffer = context.getBuffer();
        buffer.append("TYPE(");
        if (path instanceof TreatedPath<?>) {
            ((TreatedPath) path).getTreatedPath().renderPathExpression(context);
        } else {
            path.renderPathExpression(context);
        }
        buffer.append(')');
    }

}
