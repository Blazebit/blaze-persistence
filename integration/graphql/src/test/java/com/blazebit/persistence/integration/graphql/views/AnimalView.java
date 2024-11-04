/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.views;

import com.blazebit.persistence.integration.graphql.entities.Animal;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@EntityView(Animal.class)
@EntityViewInheritance
public interface AnimalView {

    @IdMapping
    Long getId();

    String getName();
}
