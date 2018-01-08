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

import javax.persistence.Parameter;
import javax.persistence.criteria.ParameterExpression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ParameterExpressionImpl<T> extends AbstractExpression<T> implements ParameterExpression<T> {

    private static final long serialVersionUID = 1L;

    private String name;

    public ParameterExpressionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, String name) {
        super(criteriaBuilder, javaType);
        this.name = name;
    }

    public ParameterExpressionImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType) {
        super(criteriaBuilder, javaType);
        this.name = null;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getPosition() {
        return null;
    }

    @Override
    public Class<T> getParameterType() {
        return getJavaType();
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        visitor.add(this);
    }

    @Override
    public void render(RenderContext context) {
        final String paramName = context.registerExplicitParameter(this);
        context.getBuffer().append(':').append(paramName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Parameter<?>)) {
            return false;
        }

        Parameter<?> that = (Parameter<?>) o;

        if (name == null) {
            return false;
        }
        return name.equals(that.getName());
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        // Need the same hash code as in ParameterManager.ParameterImpl
        result = 31 * result;// + (position != null ? position.hashCode() : 0);
        return result;
    }
}
