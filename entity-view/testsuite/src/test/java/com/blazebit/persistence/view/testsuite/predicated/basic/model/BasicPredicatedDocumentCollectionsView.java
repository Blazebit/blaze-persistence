/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.predicated.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(DocumentForCollections.class)
public interface BasicPredicatedDocumentCollectionsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("COALESCE(contacts[0].name, contacts[1].name)")
    public String getDefaultContactName();

    @Mapping("contacts[1].name")
    public String getSecondContactName();

    @Mapping("COALESCE(personList[0].name, personList[1].name)")
    public String getDefaultPersonName();

    @Mapping("personList[1].name")
    public String getSecondPersonName();
}
