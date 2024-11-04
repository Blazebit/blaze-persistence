/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc.view;

import com.blazebit.persistence.spring.hateoas.webmvc.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import java.util.UUID;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
@EntityView(Person.class)
public interface PersonView {

    @IdMapping
    UUID getId();

    String getName();

}
