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
package com.blazebit.persistence;

import javax.persistence.EntityManager;

/**
 * An interface used to create criteria builders.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface CriteriaBuilderFactory {

    /**
     * Like {@link CriteriaBuilderFactory#from(javax.persistence.EntityManager, java.lang.Class, java.lang.String)} with the
     * alias equivalent to the camel cased result of what {@link Class#getSimpleName()} of the entity class returns.
     * 
     * @param entityManager The entity manager to use for the criteria builder
     * @param entityClass The entity class which should be the root entity
     * @param <T> The type of the entity class
     * @return A new criteria builder
     */
    public <T> CriteriaBuilder<T> from(EntityManager entityManager, Class<T> entityClass);

    /**
     * Creates a new criteria builder with the given entity class as root entity in the FROM clause with the given alias.
     * 
     * @param entityManager The entity manager to use for the criteria builder
     * @param entityClass The entity class which should be the root entity
     * @param alias The alias for the root entity
     * @param <T> The type of the entity class
     * @return A new criteria builder
     */
    public <T> CriteriaBuilder<T> from(EntityManager entityManager, Class<T> entityClass, String alias);
}
