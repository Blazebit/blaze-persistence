/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import javax.persistence.metamodel.EntityType;
import java.util.Collection;

/**
 * An interface used to create subquery builders.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SubqueryInitiator<T> extends FromBaseBuilder<SubqueryBuilder<T>> {

    /**
     * Returns the parent query builder.
     *
     * @return The parent query builder
     * @since 1.3.0
     */
    public CommonQueryBuilder<?> getParentQueryBuilder();

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

    /* Overrides for proper JavaDoc */

    /**
     * Like {@link SubqueryInitiator#from(java.lang.Class, java.lang.String)} with the
     * alias equivalent to the camel cased result of what {@link Class#getSimpleName()} of the entity class returns.
     *
     * @param entityClass The entity class which should be the entity
     * @return A new subquery builder
     */
    @Override
    public SubqueryBuilder<T> from(Class<?> entityClass);

    /**
     * Creates a new subquery builder with the given entity class as entity in the FROM clause with the given alias.
     *
     * @param entityClass The entity class which should be the entity
     * @param alias The alias for the entity
     * @return A new subquery builder
     */
    @Override
    public SubqueryBuilder<T> from(Class<?> entityClass, String alias);

    /**
     * Like {@link SubqueryInitiator#from(EntityType, String)} with the
     * alias equivalent to the camel cased result of what {@link EntityType#getName()} of the entity class returns.
     *
     * @param entityType The entity type which should be queried
     * @return A new subquery builder
     * @since 1.3.0
     */
    @Override
    SubqueryBuilder<T> from(EntityType<?> entityType);

    /**
     * Creates a new subquery builder with the entity class on which the query should be based on with the given alias.
     *
     * @param entityType The entity type which should be queried
     * @param alias The alias for the entity
     * @return A new subquery builder
     * @since 1.3.0
     */
    @Override
    SubqueryBuilder<T> from(EntityType<?> entityType, String alias);

    /**
     * Like {@link SubqueryInitiator#from(Class)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return A new subquery builder
     * @since 1.2.0
     */
    @Override
    public SubqueryBuilder<T> fromOld(Class<?> entityClass);

    /**
     * Like {@link SubqueryInitiator#from(Class, String)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return A new subquery builder
     * @since 1.2.0
     */
    @Override
    public SubqueryBuilder<T> fromOld(Class<?> entityClass, String alias);

    /**
     * Like {@link SubqueryInitiator#from(Class)} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return A new subquery builder
     * @since 1.2.0
     */
    @Override
    public SubqueryBuilder<T> fromNew(Class<?> entityClass);

    /**
     * Like {@link SubqueryInitiator#from(Class, String)} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return A new subquery builder
     * @since 1.2.0
     */
    @Override
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
    @Override
    public SubqueryBuilder<T> fromValues(Class<?> valueClass, String alias, int valueCount);

    /**
     * Creates a new subquery builder with a VALUES clause for values of the type as determined by the given entity attribute to the from clause.
     * This introduces a parameter named like the given alias.
     *
     * To set the values invoke {@link CommonQueryBuilder#setParameter(String, Object)}
     * or {@link javax.persistence.Query#setParameter(String, Object)} with the alias and a collection.
     *
     * @param entityBaseClass The entity class on which the attribute is located
     * @param attributeName The attribute name within the entity class which to use for determining the values type
     * @param alias The alias for the entity
     * @param valueCount The number of values to use for the values clause
     * @return The new subquery builder
     * @since 1.3.0
     */
    @Override
    SubqueryBuilder<T> fromValues(Class<?> entityBaseClass, String attributeName, String alias, int valueCount);

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
    @Override
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
    @Override
    public <X> SubqueryBuilder<T> fromValues(Class<X> valueClass, String alias, Collection<X> values);

    /**
     * Like {@link SubqueryInitiator#fromValues(Class, String, String, int)} but passes the collection size
     * as valueCount and directly binds the collection as parameter via {@link SubqueryBuilder#setParameter(String, Object)}.
     *
     * @param entityBaseClass The entity class on which the attribute is located
     * @param attributeName The attribute name within the entity class which to use for determining the values type
     * @param alias The alias for the entity
     * @param values The values to use for the values clause
     * @return The new subquery builder
     * @since 1.3.0
     */
    @Override
    SubqueryBuilder<T> fromValues(Class<?> entityBaseClass, String attributeName, String alias, Collection<?> values);

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
    @Override
    public <X> SubqueryBuilder<T> fromIdentifiableValues(Class<X> valueClass, String alias, Collection<X> values);

}
