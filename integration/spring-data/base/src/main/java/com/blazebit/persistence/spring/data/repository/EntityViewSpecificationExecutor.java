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
 * @since 1.2.0
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
