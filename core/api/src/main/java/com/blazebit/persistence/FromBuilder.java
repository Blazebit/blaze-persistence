/*
 * Copyright 2014 - 2023 Blazebit.
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

    /**
     * Returns the path object for the given path string, creating it if necessary.
     * This method differs from {@link #getPath(String)} because it forces {@link JoinType#INNER} semantics.
     *
     * @param path The path string
     * @return The path object for this query
     * @since 1.6.8
     */
    public Path getRequiredPath(String path);

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
    public X fromIdentifiableValues(Class<?> valueClass, String identifierAttribute, String alias, int valueCount);

    @Override
    public <T> X fromValues(Class<T> valueClass, String alias, Collection<T> values);

    @Override
    public X fromValues(Class<?> entityBaseClass, String attributeName, String alias, Collection<?> values);

    @Override
    public <T> X fromIdentifiableValues(Class<T> valueClass, String alias, Collection<T> values);

    @Override
    public <T> X fromIdentifiableValues(Class<T> valueClass, String identifierAttribute, String alias, Collection<T> values);

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
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinOnSubquery(Class<?> entityClass, String alias, JoinType type);

    /**
     * Adds a subquery join with an on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinOnSubquery(String base, Class<?> entityClass, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinOnSubquery(EntityType<?> entityType, String alias, JoinType type);

    /**
     * Adds a subquery join with an on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinOnSubquery(String base, EntityType<?> entityType, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(Class<?> entityClass, String alias, JoinType type);

    /**
     * Adds a subquery join with an on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(String base, Class<?> entityClass, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias, JoinType type);

    /**
     * Adds a subquery join with an on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias, JoinType type);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(EntityType<?> entityType, String alias, JoinType type);

    /**
     * Adds a subquery join with an on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(String base, EntityType<?> entityType, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias, JoinType type);

    /**
     * Adds a subquery join with an on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinOnEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinLateralOnSubquery(Class<?> entityClass, String alias, JoinType type);

    /**
     * Adds a lateral subquery join with an on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinLateralOnSubquery(String base, Class<?> entityClass, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinLateralOnSubquery(EntityType<?> entityType, String alias, JoinType type);

    /**
     * Adds a lateral subquery join with an on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinLateralOnSubquery(String base, EntityType<?> entityType, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinLateralOnEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias, JoinType type);

    /**
     * Adds a lateral subquery join with an on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinLateralOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinLateralOnEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias, JoinType type);

    /**
     * Adds a lateral subquery join with an on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinLateralOnEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias, JoinType type);

    /**
     * Correlates the given association path in a subquery in the FROM clause and returns a CTE builder for that subquery.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the FROM clause item
     * @param subqueryAlias The alias for the correlation FROM clause item in the subquery
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> joinLateralOnSubquery(String correlationPath, String alias, String subqueryAlias, JoinType type);

    /**
     * Like calling {@link FromBuilder#joinLateralOnSubquery(String, String, String, JoinType)}, but also binds all attributes.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the FROM clause item
     * @param subqueryAlias The alias for the correlation FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z joinLateralOnEntitySubquery(String correlationPath, String alias, String subqueryAlias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> joinLateralSubquery(Class<?> entityClass, String alias, JoinType type);

    /**
     * Adds a lateral subquery join with an always true on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> joinLateralSubquery(String base, Class<?> entityClass, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<X> joinLateralSubquery(EntityType<?> entityType, String alias, JoinType type);

    /**
     * Adds a lateral subquery join with an always true on-clause to the query and giving the joined element an alias.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<X> joinLateralSubquery(String base, EntityType<?> entityType, String alias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z joinLateralEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias, JoinType type);

    /**
     * Adds a lateral subquery join with an always true on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z joinLateralEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias, JoinType type);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * the query root assumed as base.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z joinLateralEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias, JoinType type);

    /**
     * Adds a lateral subquery join with an always true on-clause to the query and giving the joined element an alias.
     * The subquery will have all attributes bound and thus not allow any further select items.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z joinLateralEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias, JoinType type);

    /**
     * Correlates the given association path in a subquery in the FROM clause and returns a CTE builder for that subquery.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the FROM clause item
     * @param subqueryAlias The alias for the correlation FROM clause item in the subquery
     * @param type The join type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> joinLateralSubquery(String correlationPath, String alias, String subqueryAlias, JoinType type);

    /**
     * Like calling {@link FromBuilder#joinLateralSubquery(String, String, String, JoinType)}, but also binds all attributes.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the FROM clause item
     * @param subqueryAlias The alias for the correlation FROM clause item in the subquery
     * @param type The join type
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z joinLateralEntitySubquery(String correlationPath, String alias, String subqueryAlias, JoinType type);

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
     * Like {@link FromBuilder#joinOnSubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinOnSubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinOnSubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinOnSubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinOnSubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinOnEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinLateralOnSubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinLateralOnSubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinLateralOnSubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinLateralOnSubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinLateralOnEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinLateralOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinLateralOnEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinLateralOnEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> innerJoinLateralOnSubquery(String correlationPath, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z innerJoinLateralOnEntitySubquery(String correlationPath, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> innerJoinLateralSubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> innerJoinLateralSubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<X> innerJoinLateralSubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<X> innerJoinLateralSubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z innerJoinLateralEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z innerJoinLateralEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z innerJoinLateralEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z innerJoinLateralEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> innerJoinLateralSubquery(String correlationPath, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#INNER}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z innerJoinLateralEntitySubquery(String correlationPath, String alias, String subqueryAlias);

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
     * Like {@link FromBuilder#joinOnSubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinOnSubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinOnSubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinOnSubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinOnSubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinOnEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinLateralOnSubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinLateralOnSubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinLateralOnSubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinLateralOnSubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinLateralOnEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinLateralOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinLateralOnEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinLateralOnEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnSubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> leftJoinLateralOnSubquery(String correlationPath, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralOnEntitySubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z leftJoinLateralOnEntitySubquery(String correlationPath, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> leftJoinLateralSubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> leftJoinLateralSubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<X> leftJoinLateralSubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<X> leftJoinLateralSubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z leftJoinLateralEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z leftJoinLateralEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z leftJoinLateralEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z leftJoinLateralEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralSubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<X> leftJoinLateralSubquery(String correlationPath, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinLateralEntitySubquery(java.lang.String, java.lang.String, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#LEFT}.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<X, ? extends Z>> Z leftJoinLateralEntitySubquery(String correlationPath, String alias, String subqueryAlias);

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

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> rightJoinOnSubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> rightJoinOnSubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> rightJoinOnSubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnSubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public FullSelectCTECriteriaBuilder<JoinOnBuilder<X>> rightJoinOnSubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(String base, Class<?> entityClass, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(String base, EntityType<?> entityType, String alias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, java.lang.Class, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityClass The entity class to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.4.1
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(String base, Class<?> entityClass, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(EntityType<?> entityType, String alias, String subqueryAlias);

    /**
     * Like {@link FromBuilder#joinOnEntitySubquery(java.lang.String, javax.persistence.metamodel.EntityType, java.lang.String, com.blazebit.persistence.JoinType) } but with
     * {@link JoinType#RIGHT}.
     *
     * @param base The base node on which to join
     * @param entityType The entity type to join
     * @param alias The alias for the joined element
     * @param subqueryAlias The alias for the FROM clause item in the subquery
     * @param <Z> The builder return type
     * @return The CTE builder for the subquery in the FROM clause
     * @since 1.5.0
     */
    public <Z extends BaseFromQueryBuilder<JoinOnBuilder<X>, ? extends Z>> Z rightJoinOnEntitySubquery(String base, EntityType<?> entityType, String alias, String subqueryAlias);

}
