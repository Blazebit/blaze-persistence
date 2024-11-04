/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.multiset.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(PersonForCollections.class)
public interface SubviewPersonForCollectionsMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    public Set<SubviewPersonForCollectionsView> getSomeCollection();

}
