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

package com.blazebit.persistence.deltaspike.data.testsuite.repository;

import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import org.apache.deltaspike.data.api.FullEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Repository
public interface PersonRepository extends FullEntityRepository<Person, Long> {

    List<Person> findByNameLike(String namePattern, Pageable pageable);

    Page<Person> findByNameLike(String namePattern, KeysetPageable pageable);
}