/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.repository;

import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Slice;
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

    Slice<Person> findByIdIsNotNull(Pageable pageable);

    List<Person> findByNameLike(String namePattern, Pageable pageable);

    Page<Person> findByNameLike(String namePattern, KeysetPageable pageable);
}