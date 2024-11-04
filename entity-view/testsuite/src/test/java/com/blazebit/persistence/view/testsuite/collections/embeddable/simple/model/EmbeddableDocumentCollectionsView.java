/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.embeddable.simple.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForElementCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForElementCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(DocumentForElementCollections.class)
public interface EmbeddableDocumentCollectionsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("contacts")
    public Map<Integer, PersonForElementCollections> getContacts();

    @Mapping("partners")
    public Set<PersonForElementCollections> getPartners();

    public List<PersonForElementCollections> getPersonList();
}
