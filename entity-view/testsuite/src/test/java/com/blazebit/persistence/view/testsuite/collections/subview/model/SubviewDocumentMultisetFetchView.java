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
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

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
public interface SubviewDocumentMultisetFetchView extends SubviewSimpleDocumentMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    @Mapping(value = "partners", fetch = FetchStrategy.MULTISET)
    public Set<SubviewPersonForCollectionsMultisetFetchView> getMultisetPartners();

    @Mapping(value = "partners", fetch = FetchStrategy.JOIN)
    public Set<SubviewPersonForCollectionsMultisetFetchView> getJoinedPartners();

    @Mapping(value = "partners", fetch = FetchStrategy.SELECT)
    public Set<SubviewPersonForCollectionsMultisetFetchView> getSelectPartners();

    @Mapping(value = "partners", fetch = FetchStrategy.SUBSELECT)
    public Set<SubviewPersonForCollectionsMultisetFetchView> getSubselectPartners();

    @Mapping
    public List<SubviewPersonForCollectionsMultisetFetchView> getPersonList();

    @Mapping
    public Map<Integer, SubviewPersonForCollectionsMultisetFetchView> getContacts();
}
