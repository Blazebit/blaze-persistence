/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.dgs.view;

import com.blazebit.persistence.examples.spring.data.dgs.model.Person;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@EntityView(Person.class)
public interface PersonSimpleView extends PersonIdView {

    String getName();

}
