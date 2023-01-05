/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.parser.util.TypeConverter;
import com.blazebit.persistence.parser.util.TypeUtils;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LiteralExpression<T> extends AbstractExpression<T> {

    private static final long serialVersionUID = 1L;

    private Object literal;

    @SuppressWarnings({"unchecked"})
    public LiteralExpression(BlazeCriteriaBuilderImpl criteriaBuilder, T literal) {
        this(criteriaBuilder, (Class<T>) determineClass(literal), literal);
    }

    public LiteralExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> type, T literal) {
        super(criteriaBuilder, type);
        this.literal = literal;
    }

    private static Class<?> determineClass(Object literal) {
        return literal == null ? null : literal.getClass();
    }

    @SuppressWarnings({"unchecked"})
    public T getLiteral() {
        return (T) literal;
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        // no-op
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        final TypeConverter converter = TypeUtils.getConverter(literal.getClass(), criteriaBuilder.getEntityMetamodel().getEnumTypes().keySet());
        if (converter != null) {
            converter.appendTo(literal, buffer);
        } else {
            final String paramName = context.registerLiteralParameterBinding(getLiteral(), getJavaType());
            buffer.append(':').append(paramName);
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setJavaType(Class targetType) {
        super.setJavaType(targetType);
        TypeConverter<T> converter = getConverter();
        if (converter == null) {
            converter = TypeUtils.getConverter(targetType, criteriaBuilder.getEntityMetamodel().getEnumTypes().keySet());
            setConverter(converter);
        }

        if (converter != null) {
            literal = converter.convert(literal);
        }
    }
}
