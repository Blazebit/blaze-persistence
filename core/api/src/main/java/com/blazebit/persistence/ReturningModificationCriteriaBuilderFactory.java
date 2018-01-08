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

package com.blazebit.persistence;

/**
 * A builder for modification queries that return results.
 *
 * @param <X> The entity type for which this modification query is
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ReturningModificationCriteriaBuilderFactory<X> {

    /**
     * Like {@link ReturningModificationCriteriaBuilderFactory#delete(java.lang.Class, java.lang.String)} but with the alias
     * equivalent to the camel cased result of what {@link Class#getSimpleName()} of the delete class returns.
     *
     * @param deleteClass The entity class for the delete criteria
     * @param <T> The type of the entity for the delete criteria
     * @return A new delete criteria builder
     * @since 1.1.0
     */
    public <T> ReturningDeleteCriteriaBuilder<T, X> delete(Class<T> deleteClass);

    /**
     * Creates a new delete criteria builder for the given entity class.
     *
     * @param deleteClass The entity class for the delete criteria
     * @param alias The alias that should be used for the entity
     * @param <T> The type of the entity for the delete criteria
     * @return A new delete criteria builder
     * @since 1.1.0
     */
    public <T> ReturningDeleteCriteriaBuilder<T, X> delete(Class<T> deleteClass, String alias);

    /**
     * Like {@link ReturningModificationCriteriaBuilderFactory#deleteCollection(java.lang.Class, java.lang.String, java.lang.String)} but with the alias
     * equivalent to the camel cased result of what {@link Class#getSimpleName()} of the delete owner class returns.
     *
     * @param deleteOwnerClass The entity class owning the collection for the delete criteria
     * @param collectionName The name of the collection contained in the owner entity class
     * @param <T> The type of the entity for the delete criteria
     * @return A new delete criteria builder
     * @since 1.2.0
     */
    public <T> ReturningDeleteCriteriaBuilder<T, X> deleteCollection(Class<T> deleteOwnerClass, String collectionName);

    /**
     * Creates a new delete criteria builder for the given entity class and collection name to delete elements of the
     * entity class's collection.
     *
     * @param deleteOwnerClass The entity class owning the collection for the delete criteria
     * @param alias The alias that should be used for the entity
     * @param collectionName The name of the collection contained in the owner entity class
     * @param <T> The type of the entity for the delete criteria
     * @return A new delete criteria builder
     * @since 1.2.0
     */
    public <T> ReturningDeleteCriteriaBuilder<T, X> deleteCollection(Class<T> deleteOwnerClass, String alias, String collectionName);

    /**
     * Like {@link ReturningModificationCriteriaBuilderFactory#update(java.lang.Class, java.lang.String)} but with the alias
     * equivalent to the camel cased result of what {@link Class#getSimpleName()} of the update class returns.
     *
     * @param updateClass The entity class for the update criteria
     * @param <T> The type of the entity for the update criteria
     * @return A new update criteria builder
     * @since 1.1.0
     */
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass);

    /**
     * Creates a new update criteria builder for the given entity class.
     *
     * @param updateClass The entity class for the update criteria
     * @param alias The alias that should be used for the entity
     * @param <T> The type of the entity for the update criteria
     * @return A new update criteria builder
     * @since 1.1.0
     */
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass, String alias);

    /**
     * Like {@link CriteriaBuilderFactory#updateCollection(javax.persistence.EntityManager, java.lang.Class, java.lang.String, java.lang.String)} but with the alias
     * equivalent to the camel cased result of what {@link Class#getSimpleName()} of the delete owner class returns.
     *
     * @param updateOwnerClass The entity class owning the collection for the update criteria
     * @param collectionName The name of the collection contained in the owner entity class
     * @param <T> The type of the entity for the update criteria
     * @return A new update criteria builder
     * @since 1.2.0
     */
    public <T> ReturningUpdateCriteriaBuilder<T, X> updateCollection(Class<T> updateOwnerClass, String collectionName);

    /**
     * Creates a new update criteria builder for the given entity class and collection name to update elements of the
     * entity class's collection.
     *
     * @param updateOwnerClass The entity class owning the collection for the update criteria
     * @param alias The alias that should be used for the entity
     * @param collectionName The name of the collection contained in the owner entity class
     * @param <T> The type of the entity for the update criteria
     * @return A new update criteria builder
     * @since 1.2.0
     */
    public <T> ReturningUpdateCriteriaBuilder<T, X> updateCollection(Class<T> updateOwnerClass, String alias, String collectionName);

    /**
     * Creates a new insert criteria builder for the given entity class.
     *
     * @param insertClass The entity class for the insert criteria
     * @param <T> The type of the entity for the insert criteria
     * @return A new insert criteria builder
     * @since 1.1.0
     */
    public <T> ReturningInsertCriteriaBuilder<T, X> insert(Class<T> insertClass);

    /**
     * Creates a new insert criteria builder for the given entity class and collection name to update elements of the
     * entity class's collection.
     *
     * @param insertOwnerClass The entity class owning the collection for the insert criteria
     * @param collectionName The name of the collection contained in the owner entity class
     * @param <T> The type of the entity for the insert criteria
     * @return A new insert criteria builder
     * @since 1.2.0
     */
    public <T> ReturningInsertCriteriaBuilder<T, X> insertCollection(Class<T> insertOwnerClass, String collectionName);
}
