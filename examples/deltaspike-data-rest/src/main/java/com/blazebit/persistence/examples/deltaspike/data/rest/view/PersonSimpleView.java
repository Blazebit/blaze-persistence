/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.deltaspike.data.rest.view;

import com.blazebit.persistence.examples.deltaspike.data.rest.model.Person;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface PersonSimpleView extends PersonIdView {

    String getName();

}
