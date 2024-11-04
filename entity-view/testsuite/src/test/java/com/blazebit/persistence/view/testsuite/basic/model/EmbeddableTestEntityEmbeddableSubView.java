/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntityEmbeddable2;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(EmbeddableTestEntityEmbeddable2.class)
public abstract class EmbeddableTestEntityEmbeddableSubView {

    @Mapping("name")
    public abstract String getName();
    
}
