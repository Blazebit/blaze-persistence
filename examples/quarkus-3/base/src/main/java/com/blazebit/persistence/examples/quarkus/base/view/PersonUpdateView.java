/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.quarkus.base.view;

import com.blazebit.persistence.examples.quarkus.base.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(Person.class)
public interface PersonUpdateView extends PersonView {

    void setName(String name);
}
