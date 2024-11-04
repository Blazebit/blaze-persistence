/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.multiset.model.multiset;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.multiset.model.SubviewDocumentMultisetFetchView;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(DocumentForCollections.class)
public interface SubviewDocumentMultisetFetchViewMultiset extends SubviewDocumentMultisetFetchView {

    @Mapping(fetch = FetchStrategy.MULTISET)
    public Set<SubviewPersonForCollectionsMultisetFetchViewMultiset> getPartners();

//    @Mapping(fetch = FetchStrategy.MULTISET)
    public List<SubviewPersonForCollectionsMultisetFetchViewMultiset> getPersonList();

//    @Mapping(fetch = FetchStrategy.MULTISET)
    public Map<Integer, SubviewPersonForCollectionsMultisetFetchViewMultiset> getContacts();
}
