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

package com.blazebit.persistence.spring.data.impl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Like {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor} but allows to specify an entity view
 * return type.
 *
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public interface EntityViewSpecificationExecutor<V, E> {
    V findOne(Specification<E> var1);

    List<V> findAll(Specification<E> var1);

    Page<V> findAll(Specification<E> var1, Pageable var2);

    List<V> findAll(Specification<E> var1, Sort var2);

    long count(Specification<E> var1);
}
