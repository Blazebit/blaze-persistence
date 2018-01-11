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

package com.blazebit.persistence.deltaspike.data.api;

import org.apache.deltaspike.core.spi.activation.Deactivatable;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.List;

/**
 * Base entity view repository interface. All methods are implemented by the CDI extension.
 *
 * @param <E>   Entity type.
 * @param <V>   Entity view type.
 * @param <PK>  Primary key type.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface EntityViewRepository<E, V, PK extends Serializable> extends Deactivatable {
    /**
     * Entity lookup by primary key. Convenicence method around
     * {@link javax.persistence.EntityManager#find(Class, Object)}.
     * @param primaryKey        DB primary key.
     * @return                  Entity identified by primary or null if it does not exist.
     */
    V findBy(PK primaryKey);

    /**
     * Lookup all existing entities of entity class {@code <E>}.
     * @return                  List of entities, empty if none found.
     */
    List<V> findAll();

    /**
     * Lookup a range of existing entities of entity class {@code <E>} with support for pagination.
     * @param start             The starting position.
     * @param max               The maximum number of results to return
     * @return                  List of entities, empty if none found.
     */
    List<V> findAll(int start, int max);

    /**
     * Query by example - for a given object and a specific set of properties.
     * @param example           Sample entity. Query all like.
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<V> findBy(E example, SingularAttribute<E, ?>... attributes);

    /**
     * Query by example - for a given object and a specific set of properties with support for pagination.
     * @param example           Sample entity. Query all like.
     * @param start             The starting position.
     * @param max               The maximum number of results to return
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<V> findBy(E example, int start, int max, SingularAttribute<E, ?>... attributes);

    /**
     * Query by example - for a given object and a specific set of properties using a like operator for Strings.
     * @param example           Sample entity. Query all like.
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<V> findByLike(E example, SingularAttribute<E, ?>... attributes);

    /**
     * Query by example - for a given object and a specific set of properties
     * using a like operator for Strings with support for pagination.
     * @param example           Sample entity. Query all like.
     * @param start             The starting position.
     * @param max               The maximum number of results to return
     * @param attributes        Which attributes to consider for the query.
     * @return                  List of entities matching the example, or empty if none found.
     */
    List<V> findByLike(E example, int start, int max, SingularAttribute<E, ?>... attributes);
}