/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
 * @author Eugen Mayer
 * @since 1.6.9
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
