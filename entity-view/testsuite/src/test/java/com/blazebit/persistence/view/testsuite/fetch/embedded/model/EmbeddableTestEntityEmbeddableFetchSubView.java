/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.embedded.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntitySimpleEmbeddable2;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(EmbeddableTestEntitySimpleEmbeddable2.class)
public interface EmbeddableTestEntityEmbeddableFetchSubView {
    
    public String getName();
}
