/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.ordered.model;

import java.util.List;

import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(PersonForCollections.class)
public interface PersonWithSetAsListView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @CollectionMapping(ordered = true)
    public List<DocumentWithSetAsListView> getOwnedDocuments();
}
