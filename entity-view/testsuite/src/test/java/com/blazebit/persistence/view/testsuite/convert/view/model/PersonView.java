/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.convert.view.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface PersonView extends SimplePersonView {

    public SimplePersonView getFriend();
}
