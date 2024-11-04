/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.multiset.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(DocumentForCollections.class)
public interface SubviewDocumentMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    @MappingParameter("test")
    public Locale getTest();

    public Set<? extends SubviewPersonForCollectionsMultisetFetchView> getPartners();

    public List<? extends SubviewPersonForCollectionsMultisetFetchView> getPersonList();

    public Map<Integer, ? extends SubviewPersonForCollectionsMultisetFetchView> getContacts();
}
