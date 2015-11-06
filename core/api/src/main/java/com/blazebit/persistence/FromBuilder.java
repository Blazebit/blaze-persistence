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

/**
 * An interface for builders that support the from clause.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface FromBuilder<X extends FromBuilder<X>> {

    /**
     * Like {@link FromBuilder#from(Class, String))} with the
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
     * Like {@link FromBuilder#fromCte(Class, String, String))} with the
     * alias equivalent to the camel cased result of what {@link Class#getSimpleName()} of the cte class returns.
     *
     * @param cteClass The cte class which should be queried
     * @param cteName The name of the cte which should be queried
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromCte(Class<?> cteClass, String cteName);

    /**
     * Sets the cte class and cte name on which the query should be based on with the given alias.
     *
     * @param cteClass The cte class which should be queried
     * @param cteName The name of the cte which should be queried
     * @param alias The alias for the cte
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromCte(Class<?> cteClass, String cteName, String alias);

    /**
     * Like {@link FromBuilder#from(Class))} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromOld(Class<?> entityClass);

    /**
     * Like {@link FromBuilder#from(Class, String))} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromOld(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#from(Class))} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromNew(Class<?> entityClass);

    /**
     * Like {@link FromBuilder#from(Class, String))} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromNew(Class<?> entityClass, String alias);

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
}
