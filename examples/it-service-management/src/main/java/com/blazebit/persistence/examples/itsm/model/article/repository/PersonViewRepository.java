/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;

import com.blazebit.persistence.examples.itsm.model.article.entity.Person;
import com.blazebit.persistence.examples.itsm.model.article.view.PersonView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface PersonViewRepository extends JpaRepository<PersonView, Long>,
        EntityViewSpecificationExecutor<PersonView, Person> {

}
