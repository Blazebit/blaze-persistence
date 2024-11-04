/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.repository;

import org.springframework.data.repository.CrudRepository;

import com.blazebit.persistence.examples.itsm.model.article.entity.Person;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface PersonRepository extends CrudRepository<Person, Long> {

    long countByName(String name);

}
