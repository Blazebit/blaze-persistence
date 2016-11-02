/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.ServiceProvider;


import javax.persistence.EntityManager;

/**
 * An interface used to create criteria builders.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface CriteriaBuilderFactory extends ServiceProvider, ConfigurationSource {

    // TODO: documentation
    public <T> StartOngoingSetOperationCriteriaBuilder<T, LeafOngoingSetOperationCriteriaBuilder<T>> startSet(EntityManager entityManager, Class<T> resultClass);

    /**
     * Like {@link CriteriaBuilderFactory#create(javax.persistence.EntityManager, java.lang.Class, java.lang.String)} but with the alias
     * equivalent to the camel cased result of what {@link Class#getSimpleName()} of the result class returns.
     *
     * @param entityManager The entity manager to use for the criteria builder
     * @param resultClass The result class of the query
     * @param <T> The type of the result class
     * @return A new criteria builder
     */
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass);

    /**
     * Creates a new criteria builder with the given result class. The result class will be used as default from class.
     * The alias will be used as default alias for the from class. Both can be overridden by invoking
     * {@link BaseQueryBuilder#from(java.lang.Class, java.lang.String)}.
     *
     * @param entityManager The entity manager to use for the criteria builder
     * @param resultClass The result class of the query
     * @param <T> The type of the result class
     * @param alias The alias that should be used for the result class from clause
     * @return A new criteria builder
     */
    public <T> CriteriaBuilder<T> create(EntityManager entityManager, Class<T> resultClass, String alias);

    /**
     * Like {@link CriteriaBuilderFactory#delete(javax.persistence.EntityManager, java.lang.Class, java.lang.String)} but with the alias
     * equivalent to the camel cased result of what {@link Class#getSimpleName()} of the delete class returns.
     *
     * @param entityManager The entity manager to use for the delete criteria builder
     * @param deleteClass The entity class for the delete criteria
     * @param <T> The type of the entity for the delete criteria
     * @return A new delete criteria builder
     * @since 1.1.0
     */
    public <T> DeleteCriteriaBuilder<T> delete(EntityManager entityManager, Class<T> deleteClass);

    /**
     * Creates a new delete criteria builder for the given entity class.
     *
     * @param entityManager The entity manager to use for the delete criteria builder
     * @param deleteClass The entity class for the delete criteria
     * @param alias The alias that should be used for the entity
     * @param <T> The type of the entity for the delete criteria
     * @return A new delete criteria builder
     * @since 1.1.0
     */
    public <T> DeleteCriteriaBuilder<T> delete(EntityManager entityManager, Class<T> deleteClass, String alias);

    /**
     * Like {@link CriteriaBuilderFactory#update(javax.persistence.EntityManager, java.lang.Class, java.lang.String)} but with the alias
     * equivalent to the camel cased result of what {@link Class#getSimpleName()} of the update class returns.
     *
     * @param entityManager The entity manager to use for the update criteria builder
     * @param updateClass The entity class for the update criteria
     * @param <T> The type of the entity for the update criteria
     * @return A new update criteria builder
     * @since 1.1.0
     */
    public <T> UpdateCriteriaBuilder<T> update(EntityManager entityManager, Class<T> updateClass);

    /**
     * Creates a new update criteria builder for the given entity class.
     *
     * @param entityManager The entity manager to use for the update criteria builder
     * @param updateClass The entity class for the update criteria
     * @param alias The alias that should be used for the entity
     * @param <T> The type of the entity for the update criteria
     * @return A new update criteria builder
     * @since 1.1.0
     */
    public <T> UpdateCriteriaBuilder<T> update(EntityManager entityManager, Class<T> updateClass, String alias);

    /**
     * Creates a new insert criteria builder for the given entity class.
     *
     * @param entityManager The entity manager to use for the insert criteria builder
     * @param insertClass The entity class for the insert criteria
     * @param <T> The type of the entity for the insert criteria
     * @return A new insert criteria builder
     * @since 1.1.0
     */
    public <T> InsertCriteriaBuilder<T> insert(EntityManager entityManager, Class<T> insertClass);
}
