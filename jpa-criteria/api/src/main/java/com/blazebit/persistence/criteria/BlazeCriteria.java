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

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.criteria.spi.BlazeCriteriaBuilderFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Christian Beikov
 * @since 1.2.1
 */
public class BlazeCriteria {

    private static volatile BlazeCriteriaBuilderFactory factory;

    private BlazeCriteria() {
    }

    /**
     * Creates a new {@link BlazeCriteriaBuilder} instance bound to the given criteria builder factory.
     *
     * @param criteriaBuilderFactory The criteria builder factory to which the persistence unit is bound
     * @return A new {@link BlazeCriteriaBuilder}
     */
    public static BlazeCriteriaBuilder get(CriteriaBuilderFactory criteriaBuilderFactory) {
        BlazeCriteriaBuilderFactory factory = BlazeCriteria.factory;
        if (factory == null) {
            synchronized (BlazeCriteria.class) {
                factory = BlazeCriteria.factory;
                if (factory == null) {
                    Iterator<BlazeCriteriaBuilderFactory> iterator = ServiceLoader.load(BlazeCriteriaBuilderFactory.class).iterator();
                    if (iterator.hasNext()) {
                        factory = iterator.next();
                        BlazeCriteria.factory = factory;
                    } else {
                        throw new IllegalStateException("Could not find a service for BlazeCriteriaBuilderFactory. Are you missing the jpa-criteria-impl module?");
                    }
                }
            }
        }

        return factory.createCriteriaBuilder(criteriaBuilderFactory);
    }

    /**
     * Creates a new {@link BlazeCriteriaQuery} instance bound to the given criteria builder factory with the given class as result type.
     *
     * @param criteriaBuilderFactory The criteria builder factory to which the persistence unit is bound
     * @param clazz The result type of the created query
     * @param <T> The type of the query result
     * @return A new {@link BlazeCriteriaQuery}
     */
    public static <T> BlazeCriteriaQuery<T> get(CriteriaBuilderFactory criteriaBuilderFactory, Class<T> clazz) {
        return get(criteriaBuilderFactory).createQuery(clazz);
    }
}
