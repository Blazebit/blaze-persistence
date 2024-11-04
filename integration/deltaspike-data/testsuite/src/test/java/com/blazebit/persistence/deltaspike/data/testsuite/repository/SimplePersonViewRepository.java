/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.repository;

import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.deltaspike.data.testsuite.view.PersonView;
import org.apache.deltaspike.data.api.Repository;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Repository(forEntity = Person.class)
public interface SimplePersonViewRepository {
    PersonView findAnyByName(String name);
}
