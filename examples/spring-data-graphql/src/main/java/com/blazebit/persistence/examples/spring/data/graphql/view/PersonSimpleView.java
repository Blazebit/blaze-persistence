/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.view;

import com.blazebit.persistence.examples.spring.data.graphql.model.Person;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Person.class)
public interface PersonSimpleView extends PersonIdView {

    String getName();

}
