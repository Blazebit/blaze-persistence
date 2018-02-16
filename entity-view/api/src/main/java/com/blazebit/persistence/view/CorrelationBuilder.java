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

package com.blazebit.persistence.view;

import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;

/**
 * A builder for correlating a basis with an entity class.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CorrelationBuilder {

    /**
     * Returns the service or null if none is available.
     *
     * @param serviceClass The type of the service
     * @param <T> The service type
     * @return The service or null
     */
    public <T> T getService(Class<T> serviceClass);

    /**
     * Generates a meaningful alias that can be used for the correlation.
     *
     * @return The generated alias
     */
    public String getCorrelationAlias();

    /**
     * Correlates a basis with the given entity class.
     *
     * @param entityClass The entity class which should be correlated
     * @return The restriction builder for the correlation predicate
     */
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass);
}
