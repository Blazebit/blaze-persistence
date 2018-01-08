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

import java.util.Collection;

/**
 * An interface used to create subquery builders.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SubqueryInitiator<T> {

    /**
     * Like {@link SubqueryInitiator#from(java.lang.Class, java.lang.String)} with the
     * alias equivalent to the camel cased result of what {@link Class#getSimpleName()} of the entity class returns.
     *
     * @param entityClass The entity class which should be the entity
     * @return A new subquery builder
     */
    public SubqueryBuilder<T> from(Class<?> entityClass);

    /**
     * Creates a new subquery builder with the given entity class as entity in the FROM clause with the given alias.
     *
     * @param entityClass The entity class which should be the entity
     * @param alias The alias for the entity
     * @return A new subquery builder
     */
    public SubqueryBuilder<T> from(Class<?> entityClass, String alias);

    /**
     * Like {@link SubqueryInitiator#from(String, String)} with the
     * alias equivalent to the camel cased result of the class of the correlation parent.
     *
     * @param correlationPath The correlation path which should be queried
     * @return A new subquery builder
     * @since 1.2.0
     */
    public SubqueryBuilder<T> from(String correlationPath);

    /**
     * Creates a new subquery builder with the given correlation path in the FROM clause with the given alias.
     *
     * @param correlationPath The correlation path which should be queried
     * @param alias The alias for the entity
     * @return A new subquery builder
     * @since 1.2.0
     */
    public SubqueryBuilder<T> from(String correlationPath, String alias);

    /**
     * Starts a nested set operation builder which is used as subquery.
     * Doing this is like starting a nested query that will be connected via a set operation.
     *
     * @return A new set operation builder for the subquery
     * @since 1.2.0
     */
    public StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>> startSet();

    /**
     * Like {@link SubqueryInitiator#from(Class)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public SubqueryBuilder<T> fromOld(Class<?> entityClass);

    /**
     * Like {@link SubqueryInitiator#from(Class, String)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public SubqueryBuilder<T> fromOld(Class<?> entityClass, String alias);

    /**
     * Like {@link SubqueryInitiator#from(Class)} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public SubqueryBuilder<T> fromNew(Class<?> entityClass);

    /**
     * Like {@link SubqueryInitiator#from(Class, String)} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public SubqueryBuilder<T> fromNew(Class<?> entityClass, String alias);

    /**
     * Creates a new subquery builder with a VALUES clause for values of the given value class in the from clause.
     * This introduces a parameter named like the given alias.
     *
     * To set the values invoke {@link SubqueryBuilder#setParameter(String, Object)}
     * or {@link javax.persistence.Query#setParameter(String, Object)} with the alias and a collection.
     *
     * @param valueClass The class of the basic or managed type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param valueCount The number of values to use for the values clause
     * @return A new subquery builder
     * @since 1.2.0
     */
    public SubqueryBuilder<T> fromValues(Class<?> valueClass, String alias, int valueCount);

    /**
     * Creates a new subquery builder with a VALUES clause for values of the given value class in the from clause.
     * This introduces a parameter named like the given alias.
     *
     * In contrast to {@link SubqueryInitiator#fromValues(Class, String, int)} this will only bind the id attribute.
     *
     * To set the values invoke {@link SubqueryBuilder#setParameter(String, Object)}
     * or {@link javax.persistence.Query#setParameter(String, Object)} with the alias and a collection.
     *
     * @param valueClass The class of the identifiable type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param valueCount The number of values to use for the values clause
     * @return A new subquery builder
     * @since 1.2.0
     */
    public SubqueryBuilder<T> fromIdentifiableValues(Class<?> valueClass, String alias, int valueCount);

    /**
     * Like {@link SubqueryInitiator#fromValues(Class, String, int)} but passes the collection size
     * as valueCount and directly binds the collection as parameter via {@link SubqueryBuilder#setParameter(String, Object)}.
     *
     * @param valueClass The class of the basic or managed type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param values The values to use for the values clause
     * @param <X> The type of the values
     * @return A new subquery builder
     * @since 1.2.0
     */
    public <X> SubqueryBuilder<T> fromValues(Class<X> valueClass, String alias, Collection<X> values);

    /**
     * Like {@link SubqueryInitiator#fromIdentifiableValues(Class, String, int)} but passes the collection size
     * as valueCount and directly binds the collection as parameter via {@link SubqueryBuilder#setParameter(String, Object)}.
     *
     * @param valueClass The class of the identifiable type for which to create a VALUES clause
     * @param alias The alias for the entity
     * @param values The values to use for the values clause
     * @param <X> The type of the values
     * @return A new subquery builder
     * @since 1.2.0
     */
    public <X> SubqueryBuilder<T> fromIdentifiableValues(Class<X> valueClass, String alias, Collection<X> values);
}
