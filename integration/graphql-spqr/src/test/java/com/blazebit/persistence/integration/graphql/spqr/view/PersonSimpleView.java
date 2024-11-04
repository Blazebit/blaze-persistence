/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.spqr.view;

import com.blazebit.persistence.integration.graphql.spqr.model.Person;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@EntityView(Person.class)
public interface PersonSimpleView extends PersonIdView {

    String getName();

}
