/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.quarkus.base.view;

import com.blazebit.persistence.examples.quarkus.base.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

import java.util.UUID;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@CreatableEntityView
@EntityView(Person.class)
public interface PersonCreateView extends PersonView {

    void setId(UUID id);

    void setName(String name);
}
