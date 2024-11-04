/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.dgs.view;

import com.blazebit.persistence.examples.spring.data.dgs.model.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@EntityView(Person.class)
public interface PersonIdView {

    @IdMapping
    Long getId();

}
