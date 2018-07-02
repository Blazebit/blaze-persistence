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
public interface FromBuilder<X extends FromBuilder<X>> {

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

    /**
     * Like {@link FromBuilder#from(Class, String)} with the
     * alias equivalent to the camel cased result of what {@link Class#getSimpleName()} of the entity class returns.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     */
    public X from(Class<?> entityClass);

    /**
     * Sets the entity class on which the query should be based on with the given alias.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     */
    public X from(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#from(EntityType, String)} with the
     * alias equivalent to the camel cased result of what {@link EntityType#getName()} of the entity class returns.
     *
     * @param entityType The entity type which should be queried
     * @return The query builder for chaining calls
     * @since 1.3.0
     */
    public X from(EntityType<?> entityType);

    /**
     * Sets the entity class on which the query should be based on with the given alias.
     *
     * @param entityType The entity type which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.3.0
     */
    public X from(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#from(Class)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromOld(Class<?> entityClass);

    /**
     * Like {@link FromBuilder#from(Class, String)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromOld(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#from(Class)} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromNew(Class<?> entityClass);

    /**
     * Like {@link FromBuilder#from(Class, String)} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromNew(Class<?> entityClass, String alias);

    /**
     * Add a VALUES clause for values of the given value class to the from clause.
     * This introduces a parameter named like the given alias.
     *
     * To set the values invoke {@link CommonQueryBuilder#setParameter(String, Object)}
     * or {@link javax.persistence.Query#setParameter(String, Object)} with the alias and a collection.
     *
     * @param valueClass The class of the basic or managed type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param valueCount The number of values to use for the values clause
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public X fromValues(Class<?> valueClass, String alias, int valueCount);

    /**
     * Add a VALUES clause for values of the given value class to the from clause.
     * This introduces a parameter named like the given alias.
     *
     * In contrast to {@link FromBuilder#fromValues(Class, String, int)} this will only bind the id attribute.
     *
     * To set the values invoke {@link CommonQueryBuilder#setParameter(String, Object)}
     * or {@link javax.persistence.Query#setParameter(String, Object)} with the alias and a collection.
     *
     * @param valueClass The class of the identifiable type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param valueCount The number of values to use for the values clause
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public X fromIdentifiableValues(Class<?> valueClass, String alias, int valueCount);

    /**
     * Like {@link FromBuilder#fromValues(Class, String, int)} but passes the collection size
     * as valueCount and directly binds the collection as parameter via {@link CommonQueryBuilder#setParameter(String, Object)}.
     *
     * @param valueClass The class of the basic or managed type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param values The values to use for the values clause
     * @param <T> The type of the values
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public <T> X fromValues(Class<T> valueClass, String alias, Collection<T> values);

    /**
     * Like {@link FromBuilder#fromIdentifiableValues(Class, String, int)} but passes the collection size
     * as valueCount and directly binds the collection as parameter via {@link CommonQueryBuilder#setParameter(String, Object)}.
     *
     * @param valueClass The class of the identifiable type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param values The values to use for the values clause
     * @param <T> The type of the values
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
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
