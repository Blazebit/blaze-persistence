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

package com.blazebit.persistence.examples.deltaspike.data.rest.repository;

import com.blazebit.persistence.deltaspike.data.KeysetAwarePage;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.examples.deltaspike.data.rest.model.Cat;
import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.FullEntityRepository;
import org.apache.deltaspike.data.api.Repository;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Repository
public interface CatJpaRepository extends FullEntityRepository<Cat, Long> {

    @EntityGraph(paths = { "owner" })
    public KeysetAwarePage<Cat> findAll(Specification<Cat> specification, Pageable pageable);
}
