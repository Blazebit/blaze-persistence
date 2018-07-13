/*
 * Copyright 2011-2017 the original author or authors.
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
package com.blazebit.persistence.spring.data.base.query;

import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.expression.Expression;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.ParameterExpression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to allow easy creation of {@link ParameterMetadata}s.
 *
 * Christian Beikov: Copied to be able to share code between Spring Data integrations for 1.x and 2.x.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public class ParameterMetadataProvider {

    private final CriteriaBuilder builder;
    private final Iterator<? extends JpaParameters.JpaParameter> parameters;
    private final List<ParameterMetadata<?>> expressions;
    private final Iterator<Object> bindableParameterValues;
    private final PersistenceProvider persistenceProvider;

    /**
     * Creates a new {@link ParameterMetadataProvider} from the given {@link CriteriaBuilder} and
     * {@link ParametersParameterAccessor} with support for parameter value customizations via {@link PersistenceProvider}
     * .
     *
     * @param builder must not be {@literal null}.
     * @param accessor must not be {@literal null}.
     * @param provider must not be {@literal null}.
     */
    public ParameterMetadataProvider(CriteriaBuilder builder, ParametersParameterAccessor accessor,
                                     PersistenceProvider provider) {
        this(builder, accessor.iterator(), (JpaParameters) accessor.getParameters(), provider);
    }

    /**
     * Creates a new {@link ParameterMetadataProvider} from the given {@link CriteriaBuilder} and {@link Parameters} with
     * support for parameter value customizations via {@link PersistenceProvider}.
     *
     * @param builder must not be {@literal null}.
     * @param parameters must not be {@literal null}.
     * @param provider must not be {@literal null}.
     */
    public ParameterMetadataProvider(CriteriaBuilder builder, JpaParameters parameters, PersistenceProvider provider) {
        this(builder, null, parameters, provider);
    }

    /**
     * Creates a new {@link ParameterMetadataProvider} from the given {@link CriteriaBuilder} an {@link Iterable} of all
     * bindable parameter values, and {@link Parameters} with support for parameter value customizations via
     * {@link PersistenceProvider}.
     *
     * @param builder must not be {@literal null}.
     * @param bindableParameterValues may be {@literal null}.
     * @param parameters must not be {@literal null}.
     * @param provider must not be {@literal null}.
     */
    private ParameterMetadataProvider(CriteriaBuilder builder, Iterator<Object> bindableParameterValues,
                                      JpaParameters parameters, PersistenceProvider provider) {

        Assert.notNull(builder, "CriteriaBuilder must not be null!");
        Assert.notNull(parameters, "Parameters must not be null!");
        Assert.notNull(provider, "PesistenceProvider must not be null!");

        this.builder = builder;
        this.parameters = parameters.getBindableParameters().iterator();
        this.expressions = new ArrayList<ParameterMetadata<?>>();
        this.bindableParameterValues = bindableParameterValues;
        this.persistenceProvider = provider;
    }

    /**
     * Returns all {@link ParameterMetadata}s built.
     *
     * @return the expressions
     */
    public List<ParameterMetadata<?>> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    /**
     * Builds a new {@link ParameterMetadata} for given {@link Part} and the next {@link Parameter}.
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> ParameterMetadata<T> next(Part part) {

        Assert.isTrue(parameters.hasNext(), String.format("No parameter available for part %s.", part));

        JpaParameters.JpaParameter parameter = parameters.next();
        return (ParameterMetadata<T>) next(part, parameter.getType(), parameter);
    }

    /**
     * Builds a new {@link ParameterMetadata} of the given {@link Part} and type. Forwards the underlying
     * {@link Parameters} as well.
     *
     * @param <T>
     * @param type must not be {@literal null}.
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> ParameterMetadata<? extends T> next(Part part, Class<T> type) {

        JpaParameters.JpaParameter parameter = parameters.next();
        Class<?> typeToUse = ClassUtils.isAssignable(type, parameter.getType()) ? parameter.getType() : type;
        return (ParameterMetadata<? extends T>) next(part, typeToUse, parameter);
    }

    /**
     * Builds a new {@link ParameterMetadata} for the given type and name.
     *
     * @param <T>
     * @param part must not be {@literal null}.
     * @param type must not be {@literal null}.
     * @param parameter
     * @return
     */
    private <T> ParameterMetadata<T> next(Part part, Class<T> type, JpaParameters.JpaParameter parameter) {

        Assert.notNull(type, "Type must not be null!");

        /*
         * We treat Expression types as Object vales since the real value to be bound as a parameter is determined at query time.
         */
        @SuppressWarnings("unchecked")
        Class<T> reifiedType = Expression.class.equals(type) ? (Class<T>) Object.class : type;

        ParameterExpression<T> expression = parameter.isExplicitlyNamed()
                ? builder.parameter(reifiedType, parameter.getParameterName())
                : builder.parameter(reifiedType);
        ParameterMetadata<T> value = new ParameterMetadata<T>(expression, part.getType(),
                bindableParameterValues == null ? ParameterMetadata.PLACEHOLDER : bindableParameterValues.next(),
                this.persistenceProvider);
        expressions.add(value);

        return value;
    }

    /**
     * @author Oliver Gierke
     * @author Thomas Darimont
     * @param <T>
     */
    public static class ParameterMetadata<T> {

        static final Object PLACEHOLDER = new Object();

        private final Type type;
        private final ParameterExpression<T> expression;
        private final PersistenceProvider persistenceProvider;

        /**
         * Creates a new {@link ParameterMetadata}.
         *
         * @param expression
         * @param type
         * @param value
         * @param provider
         */
        public ParameterMetadata(ParameterExpression<T> expression, Type type, Object value, PersistenceProvider provider) {

            this.expression = expression;
            this.persistenceProvider = provider;
            this.type = value == null && Type.SIMPLE_PROPERTY.equals(type) ? Type.IS_NULL : type;
        }

        /**
         * Returns the {@link ParameterExpression}.
         *
         * @return the expression
         */
        public ParameterExpression<T> getExpression() {
            return expression;
        }

        /**
         * Returns whether the parameter shall be considered an {@literal IS NULL} parameter.
         *
         * @return
         */
        public boolean isIsNullParameter() {
            return Type.IS_NULL.equals(type);
        }

        /**
         * Prepares the object before it's actually bound to the {@link javax.persistence.Query;}.
         *
         * @param value must not be {@literal null}.
         * @return
         */
        public Object prepare(Object value) {

            Assert.notNull(value, "Value must not be null!");

            Class<? extends T> expressionType = expression.getJavaType();

            if (String.class.equals(expressionType)) {

                switch (type) {
                    case STARTING_WITH:
                        return String.format("%s%%", value.toString());
                    case ENDING_WITH:
                        return String.format("%%%s", value.toString());
                    case CONTAINING:
                    case NOT_CONTAINING:
                        return String.format("%%%s%%", value.toString());
                    default:
                        return value;
                }
            }

            return Collection.class.isAssignableFrom(expressionType)
                    ? persistenceProvider.potentiallyConvertEmptyCollection(toCollection(value))
                    : value;
        }

        /**
         * Returns the given argument as {@link Collection} which means it will return it as is if it's a
         * {@link Collections}, turn an array into an {@link ArrayList} or simply wrap any other value into a single element
         * {@link Collections}.
         *
         * @param value
         * @return
         */
        private static Collection<?> toCollection(Object value) {

            if (value == null) {
                return null;
            }

            if (value instanceof Collection) {
                return (Collection<?>) value;
            }

            if (ObjectUtils.isArray(value)) {
                return Arrays.asList(ObjectUtils.toObjectArray(value));
            }

            return Collections.singleton(value);
        }
    }
}
