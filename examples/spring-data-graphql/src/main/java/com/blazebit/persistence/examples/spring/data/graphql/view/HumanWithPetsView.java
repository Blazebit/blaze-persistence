/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.view;

import java.util.Set;

import com.blazebit.persistence.examples.spring.data.graphql.model.Human;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@EntityView(Human.class)
public interface HumanWithPetsView {

    @IdMapping
    Long getId();

    String getName();

    Set<PetView> getPets();

}
