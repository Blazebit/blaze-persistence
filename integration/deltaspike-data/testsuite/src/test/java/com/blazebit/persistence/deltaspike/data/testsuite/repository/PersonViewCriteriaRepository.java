/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.repository;

import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person_;
import com.blazebit.persistence.deltaspike.data.testsuite.view.PersonView;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Repository
public abstract class PersonViewCriteriaRepository implements CriteriaSupport<Person> {

    public List<PersonView> getPersonViewsByComplexCondition() {
        return criteria().or(
                criteria().gt(Person_.position, 3),
                criteria().likeIgnoreCase(Person_.name, "john%")
        ).select(PersonView.class).orderAsc(Person_.id).getResultList();
    }

    public List<Person> getPersonsByComplexCondition() {
        return criteria().or(
                criteria().gt(Person_.position, 3),
                criteria().likeIgnoreCase(Person_.name, "john%")
        ).orderAsc(Person_.id).getResultList();
    }
}