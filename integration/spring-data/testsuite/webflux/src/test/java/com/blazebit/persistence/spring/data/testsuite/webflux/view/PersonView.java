/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.view;

import com.blazebit.persistence.spring.data.testsuite.webflux.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import java.util.UUID;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@EntityView(Person.class)
public interface PersonView {

    @IdMapping
    UUID getId();

    String getName();

}
