/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.view;

import com.blazebit.persistence.examples.microprofile.graphql.model.Cat;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@CreatableEntityView
@EntityView(Cat.class)
public interface CatCreateView extends CatSimpleCreateView {

    Set<CatSimpleCreateView> getKittens();
    void setKittens(Set<CatSimpleCreateView> kittens);
}
