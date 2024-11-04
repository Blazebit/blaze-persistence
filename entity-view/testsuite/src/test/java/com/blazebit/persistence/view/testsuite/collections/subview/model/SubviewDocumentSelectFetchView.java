/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@EntityView(DocumentForCollections.class)
public interface SubviewDocumentSelectFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("partners")
    public Set<SubviewPersonForCollectionsSelectFetchView> getPartners();
}
