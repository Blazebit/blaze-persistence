/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.flat.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Person.class)
public abstract class ConstructorOnlyPersonFlatView {

    private final String name;

    public ConstructorOnlyPersonFlatView(@Mapping("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
