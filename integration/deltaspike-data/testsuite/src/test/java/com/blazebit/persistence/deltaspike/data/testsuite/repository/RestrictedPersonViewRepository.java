/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.repository;

import com.blazebit.persistence.deltaspike.data.EntityViewManagerConfig;
import com.blazebit.persistence.deltaspike.data.EntityViewRepository;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.deltaspike.data.testsuite.resolver.RestrictedEntityViewManagerResolver;
import com.blazebit.persistence.deltaspike.data.testsuite.view.RestrictedPersonView;
import org.apache.deltaspike.data.api.Repository;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityViewManagerConfig(entityViewManagerResolver = RestrictedEntityViewManagerResolver.class)
@Repository
public interface RestrictedPersonViewRepository extends EntityViewRepository<Person, RestrictedPersonView, Integer> {
}
