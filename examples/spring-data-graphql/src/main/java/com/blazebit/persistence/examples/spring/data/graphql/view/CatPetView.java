/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.view;

import java.util.List;

import com.blazebit.persistence.examples.spring.data.graphql.model.Cat;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSingular;

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@EntityView(Cat.class)
public interface CatPetView extends PetView {

    @MappingSingular
    List<String> getNicknames();

}
