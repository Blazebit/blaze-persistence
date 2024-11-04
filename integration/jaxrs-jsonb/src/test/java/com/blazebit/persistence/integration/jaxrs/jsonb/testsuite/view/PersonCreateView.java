/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.view;

import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.6.4
 */
@CreatableEntityView
@EntityView(Person.class)
public interface PersonCreateView {

    @IdMapping
    String getId();
    void setId(String id);

    String getName();
    void setName(String name);
}
