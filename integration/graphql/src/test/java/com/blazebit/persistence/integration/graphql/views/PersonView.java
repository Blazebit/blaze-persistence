/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.views;

import com.blazebit.persistence.integration.graphql.entities.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import java.util.UUID;

/**
 * @author Moritz Becker
 * @since 1.6.4
 */
@EntityView(Person.class)
public interface PersonView {

    @IdMapping
    UUID getId();

    String getName();

    AnimalView getAnimal();
}
