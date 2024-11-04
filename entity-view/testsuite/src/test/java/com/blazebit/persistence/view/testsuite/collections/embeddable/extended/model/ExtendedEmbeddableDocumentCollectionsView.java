/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.extended.ExtendedDocumentForElementCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.extended.ExtendedPersonForElementCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(ExtendedDocumentForElementCollections.class)
public interface ExtendedEmbeddableDocumentCollectionsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("contacts")
    public Map<Integer, ExtendedPersonForElementCollections> getContacts();

    @Mapping("partners")
    public Set<ExtendedPersonForElementCollections> getPartners();

    public List<ExtendedPersonForElementCollections> getPersonList();
}
