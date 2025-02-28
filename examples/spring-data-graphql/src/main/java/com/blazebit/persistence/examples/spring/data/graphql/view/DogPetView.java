/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.view;

import com.blazebit.persistence.examples.spring.data.graphql.model.Dog;
import com.blazebit.persistence.integration.graphql.GraphQLDefaultFetch;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@EntityView(Dog.class)
public interface DogPetView extends PetView {

    @GraphQLDefaultFetch
    int getBarkCount();

}
