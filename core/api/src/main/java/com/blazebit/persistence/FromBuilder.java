/*
 * Copyright 2014 - 2019 Blazebit.
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

import javax.persistence.metamodel.EntityType;
import java.util.Collection;
import java.util.Set;

/**
 * An interface for builders that support the from clause.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface FromBuilder<X extends FromBuilder<X>> extends FromBaseBuilder<X>, FromProvider {

    /**
     * Returns the query roots.
     *
     * @return The roots of this query
     * @since 1.2.0
     */
    public Set<From> getRoots();

    /**
     * Returns the from element for the given alias or null.
     *
     * @param alias The alias of the from element
     * @return The from element of this query or null if not found
     * @since 1.2.0
     */
    public From getFrom(String alias);

    /**
     * Returns the from element for the given path, creating it if necessary.
     *
     * @param path The path to the from element
     * @return The from element of this query
     * @since 1.2.0
     */
    public From getFromByPath(String path);

    /**
     * Returns the path object for the given path string, creating it if necessary.
     *
     * @param path The path string
     * @return The path object for this query
     * @since 1.2.1
     */
    public Path getPath(String path);

    /* Declarations to retain binary compatibility */

    @Override
    public X from(Class<?> entityClass);

    @Override
    public X from(Class<?> entityClass, String alias);

    @Override
    public X from(EntityType<?> entityType);

    @Override
    public X from(EntityType<?> entityType, String alias);

    @Override
    public X fromOld(Class<?> entityClass);

    @Override
    public X fromOld(Class<?> entityClass, String alias);

    @Override
    public X fromNew(Class<?> entityClass);

    @Override
    public X fromNew(Class<?> entityClass, String alias);

    @Override
    public X fromValues(Class<?> valueClass, String alias, int valueCount);

    @Override
    public X fromValues(Class<?> entityBaseClass, String attributeName, String alias, int valueCount);

    @Override
    public X fromIdentifiableValues(Class<?> valueClass, String alias, int valueCount);

    @Override
    public <T> X fromValues(Class<T> valueClass, String alias, Collection<T> values);

    @Override
    public X fromValues(Class<?> entityBaseClass, String attributeName, String alias, Collection<?> values);

    @Override
    public <T> X fromIdentifiableValues(Class<T> valueClass, String alias, Collection<T> values);

    /*
     * Join methods
     */
    /**
     * Adds a join to the query, possibly specializing implicit joins, and giving the joined element an alias.
     * The resulting join is different from a default join because it can only be referred to via it's alias.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The query builder for chaining calls
     */
    public X join(String path, String alias, JoinType type);

    /**
     * Adds a join to the query, possibly specializing implicit joins, and giving the joined element an alias.
     * The resulting join will be the default join meaning that expressions which use the absolute path will refer to this join.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The query builder for chaining calls
     */
    public X joinDefault(String path, String alias, JoinType type);

    /**
     * Adds a join with an on-clause to the query, possibly specializing implicit joins, and giving the joined element an alias.
     * 
     * <p>
     * The resulting join is different from a default join because it can only be referred to via it's alias. The absolute path can only
     * be used if the joined path is a map and the on-clause contains a EQ predicate with the KEY on the left hand side.
     * </p>
     * 
     * @param path The path to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> joinOn(String path, String alias, JoinType type);

    /**
     * Adds a join with an on-clause to the query, possibly specializing implicit joins, and giving the joined element an alias.
     * The resulting join will be the default join meaning that expressions which use the absolute path will refer to this join.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> joinDefaultOn(String path, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> joinOn(Class<?> entityClass, String alias, JoinType type);

    /**
     * Adds an entity join with an on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> joinOn(String base, Class<?> entityClass, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> joinOn(EntityType<?> entityType, String alias, JoinType type);

    /**
     * Adds an entity join with an on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> joinOn(String base, EntityType<?> entityType, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X innerJoin(String path, String alias);

    /**
     * Like {@link FromBuilder#joinDefault(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X innerJoinDefault(String path, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> innerJoinOn(String path, String alias);

    /**
     * Like {@link FromBuilder#joinDefaultOn(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> innerJoinDefaultOn(String path, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> innerJoinOn(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> innerJoinOn(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOn(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> innerJoinOn(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> innerJoinOn(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X leftJoin(String path, String alias);

    /**
     * Like {@link FromBuilder#joinDefault(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X leftJoinDefault(String path, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> leftJoinOn(String path, String alias);

    /**
     * Like {@link FromBuilder#joinDefaultOn(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> leftJoinDefaultOn(String path, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> leftJoinOn(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> leftJoinOn(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOn(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> leftJoinOn(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> leftJoinOn(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#join(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X rightJoin(String path, String alias);

    /**
     * Like {@link FromBuilder#joinDefault(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The query builder for chaining calls
     */
    public X rightJoinDefault(String path, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> rightJoinOn(String path, String alias);

    /**
     * Like {@link FromBuilder#joinDefaultOn(java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param path The path to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     */
    public JoinOnBuilder<X> rightJoinDefaultOn(String path, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> rightJoinOn(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.2.0
     */
    public JoinOnBuilder<X> rightJoinOn(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOn(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> rightJoinOn(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOn(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The restriction builder for the on-clause
     * @since 1.3.0
     */
    public JoinOnBuilder<X> rightJoinOn(String base, EntityType<?> entityType, String alias);

}
