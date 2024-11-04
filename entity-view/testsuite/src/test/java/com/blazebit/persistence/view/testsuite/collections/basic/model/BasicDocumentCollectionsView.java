/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.basic.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(DocumentForCollections.class)
public interface BasicDocumentCollectionsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("contacts")
    public Map<Integer, PersonForCollections> getContacts();

    @Mapping("partners")
    public Set<PersonForCollections> getPartners();

    public List<PersonForCollections> getPersonList();
}
