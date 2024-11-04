/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.microprofile.graphql.view;

import com.blazebit.persistence.examples.microprofile.graphql.model.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Moritz Becker
 * @since 1.6.2
 */
@CreatableEntityView
@EntityView(Person.class)
public interface PersonCreateView extends PersonSimpleView {

    void setId(Long id);

    void setName(String name);
}
