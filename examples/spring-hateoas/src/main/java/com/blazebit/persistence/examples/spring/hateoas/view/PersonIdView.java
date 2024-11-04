/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.hateoas.view;

import com.blazebit.persistence.examples.spring.hateoas.model.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Person.class)
public interface PersonIdView {

    @IdMapping
    Long getId();

}
