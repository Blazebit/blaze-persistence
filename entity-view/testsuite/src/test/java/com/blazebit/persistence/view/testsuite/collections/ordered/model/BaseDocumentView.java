/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.ordered.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(DocumentForCollections.class)
public interface BaseDocumentView {
    
    @IdMapping
    public Long getId();

    public String getName();

    public PersonWithSetAsListView getOwner();
}
