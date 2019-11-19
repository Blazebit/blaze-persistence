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

/**
 * An interface for builders that support just the from clause.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface FromBaseBuilder<X extends FromBaseBuilder<X>> {

    /**
     * Like {@link FromBaseBuilder#from(Class, String)} with the
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
     * Like {@link FromBaseBuilder#from(EntityType, String)} with the
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
     * Like {@link FromBaseBuilder#from(Class)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromOld(Class<?> entityClass);

    /**
     * Like {@link FromBaseBuilder#from(Class, String)} but explicitly queries the data before any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @param alias The alias for the entity
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromOld(Class<?> entityClass, String alias);

    /**
     * Like {@link FromBaseBuilder#from(Class)} but explicitly queries the data after any side effects happen because of CTEs.
     *
     * @param entityClass The entity class which should be queried
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X fromNew(Class<?> entityClass);

    /**
     * Like {@link FromBaseBuilder#from(Class, String)} but explicitly queries the data after any side effects happen because of CTEs.
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
     * Add a VALUES clause for values of the type as determined by the given entity attribute to the from clause.
     * This introduces a parameter named like the given alias.
     *
     * To set the values invoke {@link CommonQueryBuilder#setParameter(String, Object)}
     * or {@link javax.persistence.Query#setParameter(String, Object)} with the alias and a collection.
     *
     * @param entityBaseClass The entity class on which the attribute is located
     * @param attributeName The attribute name within the entity class which to use for determining the values type
     * @param alias The alias for the entity
     * @param valueCount The number of values to use for the values clause
     * @return The query builder for chaining calls
     * @since 1.3.0
     */
    public X fromValues(Class<?> entityBaseClass, String attributeName, String alias, int valueCount);

    /**
     * Add a VALUES clause for values of the given value class to the from clause.
     * This introduces a parameter named like the given alias.
     *
     * In contrast to {@link FromBaseBuilder#fromValues(Class, String, int)} this will only bind the id attribute.
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
     * Add a VALUES clause for values of the given value class to the from clause.
     * This introduces a parameter named like the given alias.
     *
     * In contrast to {@link FromBaseBuilder#fromValues(Class, String, int)} this will only bind the identifier attribute.
     *
     * To set the values invoke {@link CommonQueryBuilder#setParameter(String, Object)}
     * or {@link javax.persistence.Query#setParameter(String, Object)} with the alias and a collection.
     *
     * @param valueClass The class of the identifiable type for which to create a VALUES clause
     * @param identifierAttribute The attribute of the entity type to consider as identifier attribute
     * @param alias The alias for the entity
     * @param valueCount The number of values to use for the values clause
     * @return The query builder for chaining calls
     * @since 1.4.0
     */
    public X fromIdentifiableValues(Class<?> valueClass, String identifierAttribute, String alias, int valueCount);

    /**
     * Like {@link FromBaseBuilder#fromValues(Class, String, int)} but passes the collection size
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
     * Like {@link FromBaseBuilder#fromValues(Class, String, String, int)} but passes the collection size
     * as valueCount and directly binds the collection as parameter via {@link CommonQueryBuilder#setParameter(String, Object)}.
     *
     * @param entityBaseClass The entity class on which the attribute is located
     * @param attributeName The attribute name within the entity class which to use for determining the values type
     * @param alias The alias for the entity
     * @param values The values to use for the values clause
     * @return The query builder for chaining calls
     * @since 1.3.0
     */
    public X fromValues(Class<?> entityBaseClass, String attributeName, String alias, Collection<?> values);

    /**
     * Like {@link FromBaseBuilder#fromIdentifiableValues(Class, String, int)} but passes the collection size
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

    /**
     * Like {@link FromBaseBuilder#fromIdentifiableValues(Class, String, String, int)} but passes the collection size
     * as valueCount and directly binds the collection as parameter via {@link CommonQueryBuilder#setParameter(String, Object)}.
     *
     * @param valueClass The class of the identifiable type for which to create a VALUES clause
     * @param identifierAttribute The attribute of the entity type to consider as identifier attribute
     * @param alias The alias for the entity
     * @param values The values to use for the values clause
     * @param <T> The type of the values
     * @return The query builder for chaining calls
     * @since 1.4.0
     */
    public <T> X fromIdentifiableValues(Class<T> valueClass, String identifierAttribute, String alias, Collection<T> values);

}
