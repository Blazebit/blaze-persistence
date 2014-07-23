/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.spi;

import com.blazebit.persistence.CriteriaBuilder;
import javax.persistence.EntityManager;

/**
 * Interface implemented by the criteria provider.
 *
 * It is invoked to create criteria builders.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface CriteriaProvider {

    /**
     * Creates a new {@linkplain CriteriaBuilder} for the given {@linkplain EntityManager} and from class.
     *
     * @param <T>   The query result type
     * @param em    The entity manager that should be used for the query
     * @param clazz The from class
     * @return A criteria builder for the given from class
     */
    public <T> CriteriaBuilder<T> from(EntityManager em, Class<T> clazz);

    /**
     * Creates a new {@linkplain CriteriaBuilder} for the given {@linkplain EntityManager}, from class and alias.
     *
     * @param <T>   The query result type
     * @param em    The entity manager that should be used for the query
     * @param clazz The from class
     * @param alias The alias for the from class
     * @return A criteria builder for the given from class
     */
    public <T> CriteriaBuilder<T> from(EntityManager em, Class<T> clazz, String alias);
}
