/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.view;

import com.blazebit.persistence.examples.spring.data.spqr.model.Cat;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@EntityView(Cat.class)
public interface CatSimpleView {

    @IdMapping
    Long getId();

    String getName();
}
