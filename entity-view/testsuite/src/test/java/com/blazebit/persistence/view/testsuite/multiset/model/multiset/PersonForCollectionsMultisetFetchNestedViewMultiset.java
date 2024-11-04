/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.multiset.model.multiset;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.multiset.model.PersonForCollectionsMultisetFetchNestedView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(PersonForCollections.class)
public interface PersonForCollectionsMultisetFetchNestedViewMultiset extends PersonForCollectionsMultisetFetchNestedView {

    @Mapping(fetch = FetchStrategy.MULTISET)
    public Set<SubviewDocumentMultisetFetchViewMultiset> getOwnedDocuments();
}
