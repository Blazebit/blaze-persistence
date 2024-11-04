/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.view;

import com.blazebit.persistence.integration.quarkus.deployment.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import java.util.UUID;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@EntityView(Person.class)
@CreatableEntityView
public interface PersonCreateView {
    @IdMapping
    UUID getId();

    String getName();

    void setName(String name);
}
