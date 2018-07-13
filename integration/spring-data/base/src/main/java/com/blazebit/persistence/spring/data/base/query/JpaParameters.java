/*
 * Copyright 2013-2017 the original author or authors.
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

import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.repository.Temporal;

import com.blazebit.persistence.spring.data.annotation.OptionalParam;
import com.blazebit.persistence.spring.data.base.query.JpaParameters.JpaParameter;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;

import javax.persistence.TemporalType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Custom extension of {@link Parameters} discovering additional query parameter annotations.
 *
 * Christian Beikov: Copied to be able to share code between Spring Data integrations for 1.x and 2.x.
 * 
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public class JpaParameters extends Parameters<JpaParameters, JpaParameter> {

    /**
     * Creates a new {@link JpaParameters} instance from the given {@link Method}.
     * 
     * @param method must not be {@literal null}.
     */
    public JpaParameters(Method method) {
        super(method);
    }

    private JpaParameters(List<JpaParameter> parameters) {
        super(parameters);
    }

    /**
     * Gets the parameters annotated with {@link OptionalParam}.
     *
     * @return the optional parameters
     */
    public JpaParameters getOptionalParameters() {

        List<JpaParameter> parameters = new ArrayList<>();

        for (JpaParameter candidate : this) {
            if (candidate.isOptionalParameter()) {
                parameters.add(candidate);
            }
        }

        return createFrom(parameters);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.Parameters#createParameter(org.springframework.core.MethodParameter)
     */
    @Override
    protected JpaParameter createParameter(MethodParameter parameter) {
        return new JpaParameter(parameter);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.Parameters#createFrom(java.util.List)
     */
    @Override
    protected JpaParameters createFrom(List<JpaParameter> parameters) {
        return new JpaParameters(parameters);
    }

    /**
     * Custom {@link Parameter} implementation adding parameters of type {@link Temporal} to the special ones.
     * 
     * @author Thomas Darimont
     * @author Oliver Gierke
     */
    static class JpaParameter extends Parameter {

        private final MethodParameter parameter;
        private final OptionalParam optional;
        private final Temporal annotation;
        private TemporalType temporalType;

        /**
         * Creates a new {@link JpaParameter}.
         * 
         * @param parameter must not be {@literal null}.
         */
        JpaParameter(MethodParameter parameter) {

            super(parameter);

            this.parameter = parameter;
            this.optional = parameter.getParameterAnnotation(OptionalParam.class);
            this.annotation = parameter.getParameterAnnotation(Temporal.class);
            this.temporalType = null;

            if (!isDateParameter() && hasTemporalParamAnnotation()) {
                throw new IllegalArgumentException(
                    Temporal.class.getSimpleName() + " annotation is only allowed on Date parameter!");
            }
        }

        public String getParameterName() {
            Param annotation = parameter.getParameterAnnotation(Param.class);
            if (annotation != null) {
                return annotation.value();
            }
            return optional == null ? parameter.getParameterName() : optional.value();
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.query.Parameter#isBindable()
         */
        @Override
        public boolean isBindable() {
            return super.isBindable() || isTemporalParameter();
        }

        @Override
        public boolean isSpecialParameter() {
            return super.isSpecialParameter() || isOptionalParameter();
        }

        boolean isOptionalParameter() {
            return optional != null;
        }

        /**
         * @return {@literal true} if this parameter is of type {@link Date} and has an {@link Temporal} annotation.
         */
        boolean isTemporalParameter() {
            return isDateParameter() && hasTemporalParamAnnotation();
        }

        /**
         * @return the {@link TemporalType} on the {@link Temporal} annotation of the given {@link Parameter}.
         */
        TemporalType getTemporalType() {

            if (temporalType == null) {
                this.temporalType = annotation == null ? null : annotation.value();
            }

            return this.temporalType;
        }

        /**
         * @return the required {@link TemporalType} on the {@link Temporal} annotation of the given {@link Parameter}.
         * @throws IllegalStateException if the parameter does not define a {@link TemporalType}.
         * @since 2.0
         */
        TemporalType getRequiredTemporalType() throws IllegalStateException {

            TemporalType temporalType = getTemporalType();

            if (temporalType != null) {
                return temporalType;
            }

            throw new IllegalStateException(String.format("Required temporal type not found for %s!", getType()));
        }

        private boolean hasTemporalParamAnnotation() {
            return annotation != null;
        }

        private boolean isDateParameter() {
            return getType().equals(Date.class);
        }
    }
}
