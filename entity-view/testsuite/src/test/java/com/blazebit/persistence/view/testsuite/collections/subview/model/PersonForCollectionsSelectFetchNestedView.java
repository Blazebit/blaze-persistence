/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsMasterView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@EntityView(PersonForCollections.class)
public interface PersonForCollectionsSelectFetchNestedView {

    @IdMapping
    public Long getId();

    public String getName();

    @Mapping(fetch = FetchStrategy.SELECT)
    public Set<SubviewDocumentSelectFetchView> getOwnedDocuments();
}
