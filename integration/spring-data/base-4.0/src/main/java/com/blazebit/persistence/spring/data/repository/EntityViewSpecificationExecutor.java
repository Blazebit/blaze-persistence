/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Like {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor} but allows to specify an entity view
 * return type.
 *
 * @param <V> The view type
 * @param <E> The entity type
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface EntityViewSpecificationExecutor<V, E> {

    /**
     * Returns a single view matching the given {@link Specification}.
     *
     * @param spec The specification for filtering
     * @return The matching view
     */
    V findOne(Specification<E> spec);

    /**
     * Returns all views matching the given {@link Specification}.
     *
     * @param spec The specification for filtering
     * @return All matching views
     */
    List<V> findAll(Specification<E> spec);

    /**
     * Returns a {@link Page} of views matching the given {@link Specification}.
     *
     * @param spec The specification for filtering
     * @param pageable The pagination information
     * @return The requested page of matching views
     */
    Page<V> findAll(Specification<E> spec, Pageable pageable);

    /**
     * Returns all views matching the given {@link Specification} in the order defined by {@link Sort}.
     *
     * @param spec The specification for filtering
     * @param sort The sort order definition
     * @return All matching views in the requested order
     */
    List<V> findAll(Specification<E> spec, Sort sort);

    /**
     * Returns the number of instances that the given {@link Specification} will return.
     *
     * @param spec the {@link Specification} to count instances for
     * @return the number of instances
     */
    long count(Specification<E> spec);
}
