/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.view;

import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Identifiable;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import java.util.UUID;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@EntityView(Person.class)
public interface PersonView extends Identifiable<UUID> {

    @IdMapping
    @Override
    UUID getId();

    String getName();

}
