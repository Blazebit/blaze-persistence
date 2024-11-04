/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.repository;

import com.blazebit.persistence.deltaspike.data.FullEntityViewRepository;
import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Slice;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.deltaspike.data.testsuite.view.PersonView;
import org.apache.deltaspike.data.api.FirstResult;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Repository
public interface FullPersonViewRepository extends FullEntityViewRepository<Person, PersonView, String> {

    @Query("select p from Person p where p.name = 'John Doe'")
    Person getJohnDoe();

    PersonView findAnyByName(String name);

    List<PersonView> findByNameLike(String namePattern, Pageable pageable);

    Slice<PersonView> findByIdIsNotNull(Pageable pageable);

    Page<PersonView> findByNameLike(String namePattern, KeysetPageable pageable);

    List<PersonView> findByNameAndPosition(String name, int position);

    List<PersonView> findByNameOrPosition(String name, int position);

    List<PersonView> findByPositionBetween(int positionFrom, int positionTo);

    PersonView findFirst1ByNameLikeOrderByIdAsc(String namePattern);

    PersonView findTop1ByNameLikeOrderByIdAsc(String namePattern);

    List<PersonView> findFirst2ByNameLikeOrderByIdAsc(String namePattern);

    List<PersonView> findTop2ByNameLikeOrderByIdAsc(String namePattern);

    PersonView findAnyByNameIsNullOrderByIdAsc();

    List<PersonView> findAllOrderByNameDescIdAsc();

    List<PersonView> findByNameLikeOrderByIdAsc(String name, @FirstResult int start, @MaxResults int max);

    PersonView findAnyByAddress_streetLikeOrderByIdAsc(String streetPattern);

    QueryResult<PersonView> findByPosition(int position);
}