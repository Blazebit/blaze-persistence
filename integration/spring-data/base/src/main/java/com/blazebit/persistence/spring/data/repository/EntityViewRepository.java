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

package com.blazebit.persistence.spring.data.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.io.Serializable;

/**
 * Base entity view repository interface.
 *
 * @param <T> Entity view type.
 * @param <ID> Entity ID type.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@NoRepositoryBean
public interface EntityViewRepository<T, ID extends Serializable> extends Repository<T, ID> {

    /**
     * Finds the entity view of type {@code <T>} with the given id.
     *
     * @param id the id of the entity view of type {@code <T>} to find
     * @return the entity view of type {@code <T>} with the given id
     */
    T findOne(ID id);

    /**
     * Checks if an entity view of type {@code <T>} with the given id exists.
     *
     * @param id the id to check for existence
     * @return true if an entity view of type {@code <T>} exists, else false
     */
    boolean exists(ID id);

    /**
     * Returns all entity views of type {@code <T>}.
     *
     * @return an iterator over all entity views of type {@code <T>}
     */
    Iterable<T> findAll();

    /**
     * Finds all entity views of type {@code <T>} with the given ids.
     *
     * @param idIterable the ids of the entity views of type {@code <T>} to find
     * @return an iterator over the entity views of type {@code <T>}
     */
    Iterable<T> findAll(Iterable<ID> idIterable);

    /**
     * Gets the number of existing entity views.of type {@code <T>}.
     *
     * @return the number of existing entity views of type {@code <T>}
     */
    long count();
}
