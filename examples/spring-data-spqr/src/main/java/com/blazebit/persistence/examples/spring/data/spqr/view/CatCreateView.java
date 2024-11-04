/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.view;

import com.blazebit.persistence.examples.spring.data.spqr.model.Cat;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@CreatableEntityView
@EntityView(Cat.class)
public interface CatCreateView extends CatSimpleCreateView {

    Set<CatSimpleCreateView> getKittens();
    void setKittens(Set<CatSimpleCreateView> kittens);
}
