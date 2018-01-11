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