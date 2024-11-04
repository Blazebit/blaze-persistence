/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.view;

import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface ChildView {

    @IdMapping("id")
    Long getId();

    String getName();
}
