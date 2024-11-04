/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.multiset.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(PersonForCollections.class)
public interface PersonForCollectionsMultisetFetchNestedView {

    @IdMapping
    public Long getId();

    public String getName();

    // We use this field to simulate a wide row, which makes join fetching more expensive
    @Mapping("REPEAT('*', 4000)")
    public String getBigField();

    public Set<? extends SubviewDocumentMultisetFetchView> getOwnedDocuments();
}
