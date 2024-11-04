/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(DocumentForCollections.class)
public interface SubviewDocumentCollectionsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("contacts")
    public Map<Integer, SubviewPersonForCollectionsView> getContacts();

    @Mapping("partners")
    public Set<SubviewPersonForCollectionsView> getPartners();

    public List<SubviewPersonForCollectionsView> getPersonList();
}
