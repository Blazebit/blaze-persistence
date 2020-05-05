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

import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.parser.Part;

import javax.persistence.criteria.ParameterExpression;
import java.util.List;

/**
 * Helper class to allow easy creation of {@link ParameterMetadata}s.
 *
 * Christian Beikov:
 * We have to copy the spring data <code>ParameterMetadataProvider</code> class unfortunately to be compatible.
 * For better reuse, we introduced an interface that version specific integrations implement.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
public interface ParameterMetadataProvider {

    /**
     * Returns all {@link ParameterMetadata}s built.
     *
     * @return the expressions
     */
    public List<ParameterMetadata<?>> getExpressions();

    /**
     * Builds a new {@link ParameterMetadata} for given {@link Part} and the next {@link Parameter}.
     *
     * @param <T>
     * @return
     */
    public <T> ParameterMetadata<T> next(Part part);

    /**
     * Builds a new {@link ParameterMetadata} of the given {@link Part} and type. Forwards the underlying
     * {@link Parameters} as well.
     *
     * @param <T>
     * @param type must not be {@literal null}.
     * @return
     */
    public <T> ParameterMetadata<? extends T> next(Part part, Class<T> type);

    /**
     * @author Oliver Gierke
     * @author Thomas Darimont
     * @param <T>
     */
    public static interface ParameterMetadata<T> {

        static final Object PLACEHOLDER = new Object();

        /**
         * Returns the {@link ParameterExpression}.
         *
         * @return the expression
         */
        public ParameterExpression<T> getExpression();

        /**
         * Returns whether the parameter shall be considered an {@literal IS NULL} parameter.
         *
         * @return
         */
        public boolean isIsNullParameter();

        /**
         * Prepares the object before it's actually bound to the {@link javax.persistence.Query;}.
         *
         * @param value must not be {@literal null}.
         * @return
         */
        public Object prepare(Object value);
    }
}
