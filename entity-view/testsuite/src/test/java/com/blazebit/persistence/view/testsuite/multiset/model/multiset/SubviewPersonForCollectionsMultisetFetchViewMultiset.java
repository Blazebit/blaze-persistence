/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.multiset.model.multiset;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.multiset.model.SubviewPersonForCollectionsMultisetFetchView;
import com.blazebit.persistence.view.testsuite.multiset.model.SubviewPersonForCollectionsView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(PersonForCollections.class)
public interface SubviewPersonForCollectionsMultisetFetchViewMultiset extends SubviewPersonForCollectionsMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    @Mapping(fetch = FetchStrategy.MULTISET)
    public Set<SubviewPersonForCollectionsView> getSomeCollection();

}
