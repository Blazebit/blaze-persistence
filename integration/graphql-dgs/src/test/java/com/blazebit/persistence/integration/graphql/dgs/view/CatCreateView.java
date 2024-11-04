/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.view;

import com.blazebit.persistence.integration.graphql.dgs.model.Cat;
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
